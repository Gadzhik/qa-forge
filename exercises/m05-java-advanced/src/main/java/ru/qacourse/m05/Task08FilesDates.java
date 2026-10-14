package ru.qacourse.m05;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Задание 5.8: файлы (java.nio.file) и даты (java.time).
 */
public class Task08FilesDates {

    /** Записывает строки в файл (UTF-8), создавая недостающие папки. */
    public static void writeLines(Path file, List<String> lines) throws IOException {
        throw new UnsupportedOperationException("TODO: напишите решение");
    }

    /** Сколько строк файла содержат слово ERROR. */
    public static long countErrors(Path logFile) throws IOException {
        throw new UnsupportedOperationException("TODO: напишите решение");
    }

    /** Действует ли промокод в день today: validUntil включительно. */
    public static boolean isPromoValid(LocalDate validUntil, LocalDate today) {
        throw new UnsupportedOperationException("TODO: напишите решение");
    }

    /** Сколько дней от from до to (to позже — положительное число). */
    public static long daysBetween(LocalDate from, LocalDate to) {
        throw new UnsupportedOperationException("TODO: напишите решение");
    }

    /** Дата в формате дд.мм.гггг: 2020-08-31 → "31.08.2020". */
    public static String format(LocalDate date) {
        throw new UnsupportedOperationException("TODO: напишите решение");
    }

    /** Разбирает дату из формата дд.мм.гггг. */
    public static LocalDate parse(String text) {
        throw new UnsupportedOperationException("TODO: напишите решение");
    }
}
