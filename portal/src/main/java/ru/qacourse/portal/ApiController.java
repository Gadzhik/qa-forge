package ru.qacourse.portal;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/** REST API портала, которым пользуется страница (static/js/app.js). */
@RestController
@RequestMapping("/api")
public class ApiController {

    public record LessonMark(String key, boolean done) {
    }

    public record QuizScore(String key, int percent) {
    }

    public record GameScore(String game, int score) {
    }

    public record CheckRequest(String module, String test, String taskKey) {
    }

    public record PojoRequest(String code, String json, String rootType) {
    }

    private static final Pattern GAME_NAME = Pattern.compile("[a-z0-9-]+");

    private final CourseService course;
    private final ProgressService progress;
    private final CheckService checks;
    private final EnvService env;
    private final PojoChecker pojo;
    private final ProjectPaths paths;

    public ApiController(CourseService course, ProgressService progress, CheckService checks, EnvService env,
                         PojoChecker pojo, ProjectPaths paths) {
        this.course = course;
        this.progress = progress;
        this.checks = checks;
        this.env = env;
        this.pojo = pojo;
        this.paths = paths;
    }

    @GetMapping("/course")
    public List<CourseService.ModuleInfo> course() {
        return course.modules();
    }

    @GetMapping("/lesson/{module}/{lesson}")
    public ResponseEntity<CourseService.Lesson> lesson(@PathVariable String module, @PathVariable String lesson) {
        return course.lesson(module, lesson).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/progress")
    public ProgressService.Progress progress() {
        return progress.get();
    }

    @PostMapping("/progress/lesson")
    public ProgressService.Progress lessonDone(@RequestBody LessonMark body) {
        return body.done() ? progress.lessonDone(body.key()) : progress.lessonUndone(body.key());
    }

    @PostMapping("/progress/quiz")
    public ProgressService.Progress quiz(@RequestBody QuizScore body) {
        return progress.quiz(body.key(), Math.max(0, Math.min(100, body.percent())));
    }

    @PostMapping("/progress/game")
    public ProgressService.Progress game(@RequestBody GameScore body) {
        return progress.game(body.game(), Math.max(0, body.score()));
    }

    @PostMapping("/progress/reset")
    public ProgressService.Progress reset() {
        return progress.reset();
    }

    @PostMapping("/check")
    public CheckService.Result check(@RequestBody CheckRequest body) {
        CheckService.Result result = checks.run(body.module(), body.test());
        if (result.status() == CheckService.Status.PASSED && body.taskKey() != null && !body.taskKey().isBlank()) {
            progress.taskPassed(body.taskKey());
        }
        return result;
    }

    @GetMapping("/env")
    public List<EnvService.Item> env() {
        return env.check();
    }

    @GetMapping("/testlab")
    public Object testlab() throws IOException {
        Path file = paths.root().resolve("testlab/scenarios.yaml");
        if (!Files.exists(file)) return Map.of("scenarios", List.of());
        return new Yaml().load(Files.readString(file, StandardCharsets.UTF_8));
    }

    /** Данные тренажёра: games/<имя>.yaml из ресурсов портала, отдаём как JSON. */
    @GetMapping("/games/{name}")
    public ResponseEntity<Object> gameData(@PathVariable String name) throws IOException {
        if (!GAME_NAME.matcher(name).matches()) return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        ClassPathResource res = new ClassPathResource("games/" + name + ".yaml");
        if (!res.exists()) return ResponseEntity.notFound().build();
        try (InputStream in = res.getInputStream()) {
            return ResponseEntity.ok(new Yaml().load(new InputStreamReader(in, StandardCharsets.UTF_8)));
        }
    }

    @PostMapping("/games/pojo/check")
    public PojoChecker.Result pojo(@RequestBody PojoRequest body) {
        return pojo.check(body.code(), body.json(), body.rootType());
    }

    /** Эталонное решение задания. Отдаём только файлы из папки solutions/. */
    @GetMapping(value = "/solution", produces = "text/plain;charset=UTF-8")
    public ResponseEntity<String> solution(@RequestParam String path) throws IOException {
        Path file = paths.inside(path);
        if (!file.startsWith(paths.root().resolve("solutions")) || !Files.isRegularFile(file)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Files.readString(file, StandardCharsets.UTF_8));
    }

    /** Открыть файл задания: отдаём путь, чтобы его было удобно скопировать в IDE. */
    @GetMapping("/root")
    public Map<String, String> root() {
        return Map.of("root", paths.root().toString(), "os", System.getProperty("os.name"));
    }
}
