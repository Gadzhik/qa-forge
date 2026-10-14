package ru.qacourse.portal;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.*;

/**
 * Прогресс студента хранится в обычном JSON-файле в домашней папке:
 * ~/.qa-course/progress.json (Windows: C:\Users\Имя\.qa-course\progress.json).
 * Так прогресс не теряется при перезапуске и обновлении курса.
 * Рядом лежит резервная копия progress.json.bak — предыдущая версия файла.
 */
@Service
public class ProgressService {

    public static class Progress {
        public Set<String> lessonsDone = new LinkedHashSet<>();
        public Map<String, Integer> quizBest = new LinkedHashMap<>();
        public Map<String, String> tasksPassed = new LinkedHashMap<>();
        public Map<String, Integer> gameBest = new LinkedHashMap<>();
        public int xp;
        public int coins;
        public Set<String> achievements = new LinkedHashSet<>();
    }

    private final Path file;
    private final Path backup;
    private final ObjectMapper mapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES); // файл от более новой версии курса тоже прочитается

    public ProgressService(@Value("${course.progress-file:}") String configured) {
        this.file = configured.isBlank()
                ? Path.of(System.getProperty("user.home"), ".qa-course", "progress.json")
                : Path.of(configured);
        this.backup = file.resolveSibling(file.getFileName() + ".bak");
    }

    public synchronized Progress get() {
        if (!Files.exists(file)) return new Progress();
        try {
            return mapper.readValue(file.toFile(), Progress.class);
        } catch (IOException e) {
            // Файл повреждён (например, компьютер выключился во время записи).
            // Не начинаем с нуля молча: откладываем сломанный файл и пробуем резервную копию.
            try {
                Files.move(file, file.resolveSibling("progress.broken-" + Instant.now().toEpochMilli() + ".json"));
            } catch (IOException ignored) {
                // не получилось переименовать — ничего страшного, просто читаем копию
            }
            try {
                // Восстанавливаем из копии и сразу записываем обратно в progress.json
                return Files.exists(backup) ? save(mapper.readValue(backup.toFile(), Progress.class)) : new Progress();
            } catch (IOException e2) {
                return new Progress();
            }
        }
    }

    public synchronized Progress lessonDone(String lessonKey) {
        Progress p = get();
        if (p.lessonsDone.add(lessonKey)) {
            p.xp += 10;
            p.coins += 1;
        }
        return achievements(p);
    }

    public synchronized Progress lessonUndone(String lessonKey) {
        Progress p = get();
        p.lessonsDone.remove(lessonKey);
        return save(p);
    }

    public synchronized Progress quiz(String quizKey, int percent) {
        Progress p = get();
        int before = p.quizBest.getOrDefault(quizKey, 0);
        if (percent > before) {
            p.quizBest.put(quizKey, percent);
            p.xp += (percent - before) / 10;
        }
        return achievements(p);
    }

    public synchronized Progress taskPassed(String taskKey) {
        Progress p = get();
        if (!p.tasksPassed.containsKey(taskKey)) {
            p.tasksPassed.put(taskKey, Instant.now().toString());
            p.xp += 30;
            p.coins += 5;
        }
        return achievements(p);
    }

    public synchronized Progress game(String game, int score) {
        Progress p = get();
        int before = p.gameBest.getOrDefault(game, 0);
        if (score > before) {
            p.gameBest.put(game, score);
            p.xp += Math.max(1, (score - before) / 10);
            p.coins += 1;
        }
        return achievements(p);
    }

    public synchronized Progress reset() {
        return save(new Progress());
    }

    private Progress achievements(Progress p) {
        if (!p.lessonsDone.isEmpty()) p.achievements.add("Первый шаг");
        if (p.lessonsDone.size() >= 10) p.achievements.add("Книжный червь");
        if (p.lessonsDone.size() >= 50) p.achievements.add("Полпути");
        if (!p.tasksPassed.isEmpty()) p.achievements.add("Первый зелёный тест");
        if (p.tasksPassed.keySet().stream().filter(k -> k.startsWith("testlab:")).count() >= 5) p.achievements.add("Охотник на баги");
        if (p.tasksPassed.keySet().stream().filter(k -> k.startsWith("testlab:")).count() >= 30) p.achievements.add("Гроза багов");
        if (p.quizBest.values().stream().filter(v -> v == 100).count() >= 5) p.achievements.add("Отличник");
        if (p.gameBest.size() >= 5) p.achievements.add("Игрок");
        if (p.xp >= 1000) p.achievements.add("Тысячник");
        return save(p);
    }

    /**
     * Запись «атомарно»: сначала во временный файл, потом подменяем им старый одной операцией.
     * Если курс закроют посреди записи, старый progress.json останется целым.
     */
    private Progress save(Progress p) {
        try {
            Files.createDirectories(file.getParent());
            Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
            mapper.writeValue(tmp.toFile(), p);
            if (Files.exists(file)) {
                Files.copy(file, backup, StandardCopyOption.REPLACE_EXISTING);
            }
            try {
                Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
            }
            return p;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
