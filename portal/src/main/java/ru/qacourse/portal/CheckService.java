package ru.qacourse.portal;

import org.springframework.stereotype.Service;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Кнопка «Проверить»: запускает Maven-тест задания так же, как вы сделали бы это в терминале:
 * <pre>./mvnw -B -f exercises/m03-java-basics/pom.xml test -Dtest=Task01Check</pre>
 * а потом читает отчёт Surefire (target/surefire-reports/TEST-*.xml).
 */
@Service
public class CheckService {

    public enum Status { PASSED, FAILED, COMPILE_ERROR, ERROR }

    public record TestCase(String name, boolean passed, String message) {
    }

    public record Result(Status status, String summary, List<TestCase> tests, List<String> errors, String command,
                         long millis) {
    }

    private static final Pattern SAFE_TEST = Pattern.compile("[A-Za-z0-9_*#+,.]+");

    private final ProjectPaths paths;

    public CheckService(ProjectPaths paths) {
        this.paths = paths;
    }

    /** Одновременно идёт только одна проверка: Maven не любит параллельные сборки одного модуля. */
    public synchronized Result run(String module, String test) {
        long start = System.currentTimeMillis();
        Path moduleDir = paths.inside(module);
        Path pom = moduleDir.resolve("pom.xml");
        if (!Files.exists(pom)) {
            return new Result(Status.ERROR, "Не найден модуль " + module, List.of(), List.of(), "", 0);
        }
        if (!SAFE_TEST.matcher(test).matches()) {
            return new Result(Status.ERROR, "Некорректное имя теста", List.of(), List.of(), "", 0);
        }
        List<String> cmd = new ArrayList<>();
        if (ProjectPaths.isWindows()) {
            cmd.addAll(List.of("cmd.exe", "/c", "mvnw.cmd"));
        } else {
            cmd.add("./mvnw");
        }
        String relPom = paths.root().relativize(pom).toString();
        cmd.addAll(List.of("-B", "-f", relPom, "test", "-Dtest=" + test, "-Dsurefire.failIfNoSpecifiedTests=false"));
        String shown = (ProjectPaths.isWindows() ? "mvnw.cmd" : "./mvnw") + " -f " + relPom + " test -Dtest=" + test;

        clearReports(moduleDir.resolve("target/surefire-reports"));
        List<String> output = new ArrayList<>();
        try {
            ProcessBuilder pb = new ProcessBuilder(cmd).directory(paths.root().toFile()).redirectErrorStream(true);
            Process process = pb.start();
            try (BufferedReader r = new BufferedReader(new InputStreamReader(process.getInputStream(), Charset.defaultCharset()))) {
                String line;
                while ((line = r.readLine()) != null) output.add(line);
            }
            if (!process.waitFor(15, TimeUnit.MINUTES)) {
                process.destroyForcibly();
                return new Result(Status.ERROR, "Проверка заняла больше 15 минут и была остановлена", List.of(), tail(output), shown, ms(start));
            }
        } catch (Exception e) {
            return new Result(Status.ERROR, "Не удалось запустить Maven: " + e.getMessage(), List.of(), tail(output), shown, ms(start));
        }

        List<String> compileErrors = output.stream()
                .filter(l -> l.startsWith("[ERROR]") && (l.contains(".java:[") || l.contains("COMPILATION ERROR")))
                .map(l -> l.replace(paths.root().toString(), "").replaceFirst("^\\[ERROR] ", ""))
                .distinct().limit(20).toList();
        if (!compileErrors.isEmpty()) {
            return new Result(Status.COMPILE_ERROR, "Код не компилируется — исправьте ошибки ниже", List.of(), compileErrors, shown, ms(start));
        }

        List<TestCase> tests = readReports(moduleDir.resolve("target/surefire-reports"));
        if (tests.isEmpty()) {
            return new Result(Status.ERROR, "Тесты не запустились. Последние строки вывода Maven ниже.", List.of(), tail(output), shown, ms(start));
        }
        long failed = tests.stream().filter(t -> !t.passed()).count();
        Status status = failed == 0 ? Status.PASSED : Status.FAILED;
        String summary = failed == 0
                ? "Все проверки пройдены (" + tests.size() + ") 🎉"
                : "Не пройдено проверок: " + failed + " из " + tests.size();
        return new Result(status, summary, tests, List.of(), shown, ms(start));
    }

    private static List<TestCase> readReports(Path dir) {
        List<TestCase> result = new ArrayList<>();
        if (!Files.isDirectory(dir)) return result;
        try (Stream<Path> files = Files.list(dir)) {
            for (Path f : files.filter(p -> p.getFileName().toString().startsWith("TEST-") && p.toString().endsWith(".xml")).sorted().toList()) {
                var factory = DocumentBuilderFactory.newInstance();
                factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
                var doc = factory.newDocumentBuilder().parse(f.toFile());
                NodeList cases = doc.getElementsByTagName("testcase");
                for (int i = 0; i < cases.getLength(); i++) {
                    Element tc = (Element) cases.item(i);
                    String message = problem(tc, "failure");
                    if (message == null) message = problem(tc, "error");
                    boolean skipped = tc.getElementsByTagName("skipped").getLength() > 0;
                    if (skipped) continue;
                    result.add(new TestCase(tc.getAttribute("name"), message == null, message));
                }
            }
        } catch (Exception e) {
            result.add(new TestCase("Чтение отчёта", false, e.getMessage()));
        }
        return result;
    }

    private static String problem(Element tc, String tag) {
        NodeList list = tc.getElementsByTagName(tag);
        if (list.getLength() == 0) return null;
        Element el = (Element) list.item(0);
        String msg = el.getAttribute("message");
        if (msg == null || msg.isBlank()) {
            String text = el.getTextContent();
            msg = text == null ? tag : text.lines().limit(5).reduce((a, b) -> a + "\n" + b).orElse(tag);
        }
        return msg.length() > 2000 ? msg.substring(0, 2000) + "…" : msg;
    }

    private static void clearReports(Path dir) {
        if (!Files.isDirectory(dir)) return;
        try (Stream<Path> files = Files.list(dir)) {
            files.forEach(f -> {
                try {
                    Files.deleteIfExists(f);
                } catch (Exception ignored) {
                    // файл может быть занят — не страшно
                }
            });
        } catch (Exception ignored) {
            // нет папки — нечего чистить
        }
    }

    private static List<String> tail(List<String> output) {
        return output.subList(Math.max(0, output.size() - 40), output.size());
    }

    private static long ms(long start) {
        return System.currentTimeMillis() - start;
    }
}
