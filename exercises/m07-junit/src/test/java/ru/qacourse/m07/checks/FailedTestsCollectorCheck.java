package ru.qacourse.m07.checks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.qacourse.m07.sut.Mutant;
import org.junit.jupiter.api.io.TempDir;
import ru.qacourse.support.StudentTests;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

/** Проверка задания. Не изменяйте этот файл. */
class FailedTestsCollectorCheck {

    @Test
    @DisplayName("Упавшие тесты записываются в файл в формате Класс#метод")
    void collects(@TempDir Path tmp) throws Exception {
        Path file = tmp.resolve("failed.txt");
        System.setProperty("failed.tests.file", file.toString());
        try {
            StudentTests.run(CollectorSample.class);
        } finally {
            System.clearProperty("failed.tests.file");
        }
        assertThat(file).exists();
        assertThat(Files.readAllLines(file)).containsExactlyInAnyOrder("CollectorSample#failsOne", "CollectorSample#failsTwo");
    }
}
