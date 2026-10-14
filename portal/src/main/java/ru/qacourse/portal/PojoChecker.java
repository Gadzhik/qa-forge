package ru.qacourse.portal;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.stereotype.Service;

import javax.tools.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.regex.Matcher;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

/**
 * Тренажёр POJO Builder: компилирует класс, который написал студент, прямо в памяти
 * (javax.tools.JavaCompiler — тот же компилятор, что и javac), затем просит Jackson
 * превратить JSON в объект этого класса и обратно. Если JSON "пережил" путешествие
 * туда-обратно без потерь — модель написана правильно.
 */
@Service
public class PojoChecker {

    public record Result(boolean ok, String message, List<String> details) {
    }

    private static final Pattern PACKAGE = Pattern.compile("^\\s*package\\s+([\\w.]+)\\s*;", Pattern.MULTILINE);
    private static final Pattern TYPE = Pattern.compile("(?:public\\s+)?(?:final\\s+)?(?:class|record)\\s+(\\w+)");

    public Result check(String code, String json, String rootType) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            return new Result(false, "Портал запущен на JRE без компилятора. Нужен JDK 21.", List.of());
        }
        Matcher pm = PACKAGE.matcher(code);
        String pkg = pm.find() ? pm.group(1) + "." : "";
        Matcher tm = TYPE.matcher(code);
        if (!tm.find()) {
            return new Result(false, "Не нашёл объявление класса: нужно что-то вроде «public class Order {»", List.of());
        }
        String mainName = rootType != null && !rootType.isBlank() ? rootType : tm.group(1);
        String fileName = tm.group(1);

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        MemoryFileManager fm = new MemoryFileManager(compiler.getStandardFileManager(diagnostics, null, StandardCharsets.UTF_8));
        JavaFileObject source = new SimpleJavaFileObject(URI.create("string:///" + fileName + ".java"), JavaFileObject.Kind.SOURCE) {
            @Override
            public CharSequence getCharContent(boolean ignoreEncodingErrors) {
                // Импорты, которые обычно нужны для моделей, добавляем сами, чтобы новичок не отвлекался
                return code.contains("import java.util.") ? code : addImports(code);
            }
        };
        List<String> options = List.of("-classpath", compileClasspath(), "--release", "21", "-proc:none");
        boolean compiled = compiler.getTask(null, fm, diagnostics, options, null, List.of(source)).call();
        if (!compiled) {
            List<String> errors = diagnostics.getDiagnostics().stream()
                    .filter(d -> d.getKind() == Diagnostic.Kind.ERROR)
                    .map(d -> "Строка " + d.getLineNumber() + ": " + d.getMessage(new Locale("ru")))
                    .toList();
            return new Result(false, "Ошибка компиляции", errors);
        }
        try {
            ClassLoader loader = new ClassLoader(getClass().getClassLoader()) {
                @Override
                protected Class<?> findClass(String name) throws ClassNotFoundException {
                    byte[] bytes = fm.classes.get(name);
                    if (bytes == null) throw new ClassNotFoundException(name);
                    return defineClass(name, bytes, 0, bytes.length);
                }
            };
            Class<?> type = loader.loadClass(pkg + mainName);
            ObjectMapper strict = new ObjectMapper()
                    .findAndRegisterModules()
                    .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                    .enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
                    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // даты обратно — строками ISO-8601, как в исходном JSON
            Object value = strict.readValue(json, type);
            JsonNode expected = strict.readTree(json);
            JsonNode actual = strict.valueToTree(value);
            List<String> diff = new ArrayList<>();
            compare("", expected, actual, diff);
            if (!diff.isEmpty()) {
                return new Result(false, "JSON прочитался, но при обратном превращении данные отличаются", diff);
            }
            return new Result(true, "Отлично! Jackson прочитал JSON в " + type.getSimpleName() + " и записал обратно без потерь.", List.of());
        } catch (Exception e) {
            String msg = Optional.ofNullable(e.getMessage()).orElse(e.toString()).lines().findFirst().orElse("");
            return new Result(false, "Jackson не смог разобрать JSON в ваш класс", List.of(msg));
        }
    }

    private String classpath;

    /**
     * Classpath для компилятора. При запуске из IDE это обычный java.class.path. При запуске
     * «java -jar portal.jar» библиотеки лежат внутри jar (BOOT-INF/lib), а javac не умеет читать
     * вложенные jar — поэтому один раз копируем jackson-*.jar во временную папку.
     */
    private synchronized String compileClasspath() {
        if (classpath != null) return classpath;
        String cp = System.getProperty("java.class.path");
        List<String> parts = new ArrayList<>(List.of(cp.split(File.pathSeparator)));
        if (parts.size() == 1 && cp.endsWith(".jar")) {
            try (JarFile jar = new JarFile(cp)) {
                Path dir = Files.createTempDirectory("pojo-builder-libs");
                dir.toFile().deleteOnExit();
                for (JarEntry e : Collections.list(jar.entries())) {
                    String name = e.getName();
                    if (name.startsWith("BOOT-INF/lib/jackson-") && name.endsWith(".jar")) {
                        Path target = dir.resolve(name.substring(name.lastIndexOf('/') + 1));
                        try (InputStream in = jar.getInputStream(e)) {
                            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
                        }
                        target.toFile().deleteOnExit();
                        parts.add(target.toString());
                    }
                }
            } catch (IOException e) {
                // не получилось — компилируем без аннотаций Jackson, ошибка компиляции подскажет причину
            }
        }
        classpath = String.join(File.pathSeparator, parts);
        return classpath;
    }

    private static String addImports(String code) {
        String imports = "import java.util.*;\nimport java.math.*;\nimport java.time.*;\n";
        Matcher pm = PACKAGE.matcher(code);
        if (pm.find()) {
            return code.substring(0, pm.end()) + "\n" + imports + code.substring(pm.end());
        }
        return imports + code;
    }

    private static void compare(String path, JsonNode expected, JsonNode actual, List<String> diff) {
        if (expected.isObject()) {
            expected.fieldNames().forEachRemaining(f -> {
                String p = path.isEmpty() ? f : path + "." + f;
                if (actual == null || !actual.has(f)) diff.add("Поле «" + p + "» потерялось");
                else compare(p, expected.get(f), actual.get(f), diff);
            });
        } else if (expected.isArray()) {
            if (actual == null || !actual.isArray() || actual.size() != expected.size()) {
                diff.add("Массив «" + path + "» изменился");
                return;
            }
            for (int i = 0; i < expected.size(); i++) compare(path + "[" + i + "]", expected.get(i), actual.get(i), diff);
        } else if (expected.isNumber() && actual != null && actual.isNumber()) {
            if (expected.decimalValue().compareTo(actual.decimalValue()) != 0) {
                diff.add("«" + path + "»: было " + expected + ", стало " + actual);
            }
        } else if (!expected.equals(actual)) {
            diff.add("«" + path + "»: было " + expected + ", стало " + actual + " (проверьте тип поля)");
        }
    }

    /** Хранит скомпилированные .class прямо в памяти, без записи на диск. */
    private static final class MemoryFileManager extends ForwardingJavaFileManager<StandardJavaFileManager> {
        final Map<String, byte[]> classes = new HashMap<>();

        MemoryFileManager(StandardJavaFileManager fm) {
            super(fm);
        }

        @Override
        public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) {
            return new SimpleJavaFileObject(URI.create("mem:///" + className.replace('.', '/') + kind.extension), kind) {
                @Override
                public OutputStream openOutputStream() {
                    return new ByteArrayOutputStream() {
                        @Override
                        public void close() {
                            classes.put(className, toByteArray());
                        }
                    };
                }
            };
        }
    }
}
