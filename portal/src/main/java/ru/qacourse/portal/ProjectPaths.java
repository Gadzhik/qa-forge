package ru.qacourse.portal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Находит корень проекта курса — папку, где лежат mvnw и course/.
 * Портал может быть запущен из корня или из папки portal, поэтому ищем вверх по дереву.
 */
@Component
public class ProjectPaths {
    private final Path root;

    public ProjectPaths(@Value("${course.root:}") String configured) {
        this.root = configured.isBlank() ? detect() : Path.of(configured).toAbsolutePath().normalize();
    }

    private static Path detect() {
        Path dir = Path.of("").toAbsolutePath();
        while (dir != null) {
            if (Files.isDirectory(dir.resolve("course")) && Files.exists(dir.resolve("mvnw"))) {
                return dir;
            }
            dir = dir.getParent();
        }
        throw new IllegalStateException(
                "Не найден корень курса (папка с mvnw и course/). Запустите портал из папки проекта или задайте --course.root=путь");
    }

    public Path root() {
        return root;
    }

    public Path course() {
        return root.resolve("course");
    }

    /** Безопасно получить путь внутри проекта (защита от "../../" в запросах). */
    public Path inside(String relative) {
        Path p = root.resolve(relative).normalize();
        if (!p.startsWith(root)) {
            throw new IllegalArgumentException("Путь вне проекта: " + relative);
        }
        return p;
    }

    public static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }
}
