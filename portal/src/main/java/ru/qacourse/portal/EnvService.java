package ru.qacourse.portal;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** Страница «Окружение»: проверяет, что всё нужное для курса установлено и запущено. */
@Service
public class EnvService {

    public record Item(String name, boolean ok, String details, String howToFix, boolean required) {
    }

    public List<Item> check() {
        List<Item> items = new ArrayList<>();
        String os = System.getProperty("os.name") + " " + System.getProperty("os.version") + " (" + System.getProperty("os.arch") + ")";
        items.add(new Item("Операционная система", true, os, "", true));

        int javaMajor = Runtime.version().feature();
        items.add(new Item("Java (JDK)", javaMajor >= 21, "Версия " + Runtime.version() + ", " + System.getProperty("java.home"),
                "Нужен JDK 21 или новее. Урок «Установка инструментов» в модуле 0.", true));

        Path m2 = Path.of(System.getProperty("user.home"), ".m2", "repository", "ru", "qacourse", "test-support");
        items.add(new Item("Библиотеки курса собраны", Files.isDirectory(m2),
                Files.isDirectory(m2) ? "Найдены в " + m2.getParent() : "Не найдены в ~/.m2",
                "Запустите start.sh / start.cmd ещё раз — при первом запуске он выполняет ./mvnw install", true));

        boolean shop = http("http://localhost:8081/api/products?size=1");
        items.add(new Item("Учебное приложение QA Burger (порт 8081)", shop,
                shop ? "Работает: http://localhost:8081" : "Не отвечает",
                "Запустите start.sh / start.cmd — он поднимает и портал, и приложение.", true));

        String chrome = findChrome();
        items.add(new Item("Браузер Chrome/Chromium", chrome != null, chrome != null ? chrome : "Не найден",
                "Нужен для UI-тестов (модуль 10). Установите Google Chrome: https://www.google.com/chrome/", false));

        String docker = run("docker", "info", "--format", "{{.ServerVersion}}");
        items.add(new Item("Docker", docker != null, docker != null ? "Docker Engine " + docker : "Не найден или не запущен",
                "Нужен с модуля 11 (БД, Kafka, CI). Установите Docker Desktop (Windows/macOS) или Docker Engine (Linux) и запустите его.", false));

        items.add(new Item("PostgreSQL (порт 5432)", port(5432), port(5432) ? "Порт открыт" : "Не запущен",
                "docker compose up -d postgres (модуль 11)", false));
        items.add(new Item("Kafka (порт 9092)", port(9092), port(9092) ? "Порт открыт" : "Не запущена",
                "docker compose up -d kafka (модуль 13)", false));

        String git = run("git", "--version");
        items.add(new Item("Git", git != null, git != null ? git : "Не найден",
                "Установите Git: https://git-scm.com/downloads (модуль 2)", false));
        return items;
    }

    private static boolean http(String url) {
        try {
            HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
            HttpResponse<Void> r = client.send(HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(3)).build(),
                    HttpResponse.BodyHandlers.discarding());
            return r.statusCode() < 500;
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean port(int port) {
        try (Socket s = new Socket()) {
            s.connect(new InetSocketAddress("localhost", port), 500);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private static String run(String... cmd) {
        try {
            Process p = new ProcessBuilder(cmd).redirectErrorStream(true).start();
            if (!p.waitFor(8, TimeUnit.SECONDS)) {
                p.destroyForcibly();
                return null;
            }
            String out = new String(p.getInputStream().readAllBytes()).trim();
            return p.exitValue() == 0 ? out.lines().findFirst().orElse("ok") : null;
        } catch (Exception e) {
            return null;
        }
    }

    private static String findChrome() {
        String os = System.getProperty("os.name").toLowerCase();
        List<String> candidates = new ArrayList<>();
        if (os.contains("win")) {
            for (String base : new String[]{System.getenv("ProgramFiles"), System.getenv("ProgramFiles(x86)"), System.getenv("LOCALAPPDATA")}) {
                if (base != null) candidates.add(base + "\\Google\\Chrome\\Application\\chrome.exe");
            }
        } else if (os.contains("mac")) {
            candidates.add("/Applications/Google Chrome.app/Contents/MacOS/Google Chrome");
            candidates.add("/Applications/Chromium.app/Contents/MacOS/Chromium");
        } else {
            for (String n : new String[]{"google-chrome", "google-chrome-stable", "chromium", "chromium-browser"}) {
                for (String dir : new String[]{"/usr/bin/", "/usr/local/bin/", "/snap/bin/"}) candidates.add(dir + n);
            }
        }
        return candidates.stream().filter(c -> Files.isExecutable(Path.of(c))).findFirst().orElse(null);
    }
}
