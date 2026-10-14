package ru.qacourse.m05;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Задание 5.9 (практика): отчёт по логу QA Burger.
 * Строка лога: "2024-05-12T12:01:15Z INFO [http-1] OrderService - order created id=1 user=user total=521.82"
 * Формат: время, уровень, [поток], логгер, " - ", сообщение.
 */
public class Task09LogReport {

    /** Разобранная строка лога. */
    public record LogLine(String level, String logger, String message) {
    }

    private static final Pattern LINE = Pattern.compile("^\\S+ (\\w+) \\[[^]]+] (\\S+) - (.*)$");
    private static final Pattern ORDER = Pattern.compile("order created id=(\\d+) user=(\\S+) total=([\\d.]+)");

    /** Разбирает строку. Строки, не подходящие под формат, — бросить IllegalArgumentException. */
    public static LogLine parse(String line) {
        Matcher m = LINE.matcher(line);
        if (!m.matches()) {
            throw new IllegalArgumentException("Не строка лога: " + line);
        }
        return new LogLine(m.group(1), m.group(2), m.group(3));
    }

    /** Количество строк каждого уровня: {INFO=6, WARN=1, ERROR=3}. */
    public static Map<String, Long> countByLevel(List<String> lines) {
        throw new UnsupportedOperationException("TODO: напишите решение");
    }

    /** Сообщения всех ERROR-строк в порядке появления. */
    public static List<String> errorMessages(List<String> lines) {
        throw new UnsupportedOperationException("TODO: напишите решение");
    }

    /**
     * Сумма заказов по пользователям из строк "order created id=.. user=.. total=..".
     * Используйте шаблон ORDER: group(2) — пользователь, group(3) — сумма.
     */
    public static Map<String, Double> revenueByUser(List<String> lines) {
        throw new UnsupportedOperationException("TODO: напишите решение");
    }
}
