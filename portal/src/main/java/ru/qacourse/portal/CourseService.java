package ru.qacourse.portal;

import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

/**
 * Читает курс с диска:
 * <pre>
 * course/
 *   03-java-basics/
 *     module.yaml          title, summary, icon
 *     01-variables.md      Markdown урока; в начале — блок "---" с title и minutes
 * </pre>
 * Курс читается при каждом запросе, поэтому правки в .md видны после обновления страницы.
 */
@Service
public class CourseService {

    public record LessonInfo(String id, String title, int minutes) {
    }

    public record ModuleInfo(String id, String title, String summary, String icon, List<LessonInfo> lessons) {
    }

    public record Lesson(String moduleId, String id, String title, int minutes, String markdown,
                         LessonInfo prev, LessonInfo next, String prevModule, String nextModule) {
    }

    private final ProjectPaths paths;

    public CourseService(ProjectPaths paths) {
        this.paths = paths;
    }

    public List<ModuleInfo> modules() {
        try (Stream<Path> dirs = Files.list(paths.course())) {
            return dirs.filter(Files::isDirectory)
                    .filter(d -> Files.exists(d.resolve("module.yaml")))
                    .sorted()
                    .map(this::module)
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private ModuleInfo module(Path dir) {
        Map<String, Object> meta = yaml(dir.resolve("module.yaml"));
        List<LessonInfo> lessons;
        try (Stream<Path> files = Files.list(dir)) {
            lessons = files.filter(f -> f.getFileName().toString().endsWith(".md"))
                    .sorted()
                    .map(f -> {
                        Map<String, Object> fm = frontMatter(read(f));
                        return new LessonInfo(stripExt(f), String.valueOf(fm.getOrDefault("title", stripExt(f))),
                                ((Number) fm.getOrDefault("minutes", 15)).intValue());
                    })
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return new ModuleInfo(dir.getFileName().toString(),
                String.valueOf(meta.getOrDefault("title", dir.getFileName().toString())),
                String.valueOf(meta.getOrDefault("summary", "")),
                String.valueOf(meta.getOrDefault("icon", "📘")),
                lessons);
    }

    public Optional<Lesson> lesson(String moduleId, String lessonId) {
        List<ModuleInfo> all = modules();
        for (int m = 0; m < all.size(); m++) {
            ModuleInfo module = all.get(m);
            if (!module.id().equals(moduleId)) continue;
            List<LessonInfo> lessons = module.lessons();
            for (int i = 0; i < lessons.size(); i++) {
                LessonInfo info = lessons.get(i);
                if (!info.id().equals(lessonId)) continue;
                String text = read(paths.course().resolve(moduleId).resolve(lessonId + ".md"));
                LessonInfo prev = i > 0 ? lessons.get(i - 1) : null;
                LessonInfo next = i < lessons.size() - 1 ? lessons.get(i + 1) : null;
                String prevModule = moduleId;
                String nextModule = moduleId;
                if (prev == null && m > 0 && !all.get(m - 1).lessons().isEmpty()) {
                    List<LessonInfo> pl = all.get(m - 1).lessons();
                    prev = pl.get(pl.size() - 1);
                    prevModule = all.get(m - 1).id();
                }
                if (next == null && m < all.size() - 1 && !all.get(m + 1).lessons().isEmpty()) {
                    next = all.get(m + 1).lessons().get(0);
                    nextModule = all.get(m + 1).id();
                }
                return Optional.of(new Lesson(moduleId, lessonId, info.title(), info.minutes(), body(text),
                        prev, next, prevModule, nextModule));
            }
        }
        return Optional.empty();
    }

    /** Всего уроков — для процента прохождения. */
    public int lessonCount() {
        return modules().stream().mapToInt(m -> m.lessons().size()).sum();
    }

    static Map<String, Object> frontMatter(String text) {
        if (!text.startsWith("---")) return Map.of();
        int end = text.indexOf("\n---", 3);
        if (end < 0) return Map.of();
        Object parsed = new Yaml().load(text.substring(3, end));
        return parsed instanceof Map<?, ?> map ? castMap(map) : Map.of();
    }

    static String body(String text) {
        if (!text.startsWith("---")) return text;
        int end = text.indexOf("\n---", 3);
        if (end < 0) return text;
        int lineEnd = text.indexOf('\n', end + 4);
        return lineEnd < 0 ? "" : text.substring(lineEnd + 1);
    }

    private static Map<String, Object> yaml(Path file) {
        Object parsed = new Yaml().load(read(file));
        return parsed instanceof Map<?, ?> map ? castMap(map) : Map.of();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> castMap(Map<?, ?> map) {
        return (Map<String, Object>) map;
    }

    private static String read(Path file) {
        try {
            // \r\n → \n: файлы могли быть сохранены в Windows
            return Files.readString(file, StandardCharsets.UTF_8).replace("\r\n", "\n");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String stripExt(Path f) {
        String n = f.getFileName().toString();
        return n.substring(0, n.length() - 3);
    }
}
