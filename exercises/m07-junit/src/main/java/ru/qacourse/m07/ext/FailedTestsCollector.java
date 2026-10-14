package ru.qacourse.m07.ext;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Задание 7.7: расширение, которое записывает упавшие тесты в файл, чтобы потом перезапустить только их.
 * Формат строки — как понимает Maven: ИмяКласса#имяМетода (простое имя класса, без пакета).
 * Файл: target/failed-tests.txt (путь можно переопределить системным свойством failed.tests.file).
 */
public class FailedTestsCollector implements TestWatcher {

    public static Path file() {
        return Path.of(System.getProperty("failed.tests.file", "target/failed-tests.txt"));
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        // TODO: допишите в файл строку "ИмяКласса#имяМетода"
    }
}
