package ru.qacourse.m05;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/** Проверка задания. Не изменяйте этот файл. */
class Task08FilesDatesCheck {

    @TempDir
    Path tmp;

    @Test
    @DisplayName("writeLines создаёт папки и пишет UTF-8")
    void write() throws Exception {
        Path file = tmp.resolve("a/b/menu.txt");
        Task08FilesDates.writeLines(file, List.of("Бургер", "Кола"));
        assertThat(Files.readAllLines(file, StandardCharsets.UTF_8)).containsExactly("Бургер", "Кола");
    }

    @Test
    @DisplayName("countErrors по файлу лога из ресурсов теста")
    void countErrors() throws Exception {
        Path log = Path.of(getClass().getResource("/shop-sample.log").toURI());
        assertThat(Task08FilesDates.countErrors(log)).isEqualTo(3);
    }

    @Test
    @DisplayName("isPromoValid — граница включительно")
    void promo() {
        LocalDate until = LocalDate.of(2020, 8, 31);
        assertThat(Task08FilesDates.isPromoValid(until, LocalDate.of(2020, 8, 31))).isTrue();
        assertThat(Task08FilesDates.isPromoValid(until, LocalDate.of(2020, 9, 1))).isFalse();
        assertThat(Task08FilesDates.isPromoValid(until, LocalDate.of(2020, 1, 1))).isTrue();
    }

    @Test
    @DisplayName("daysBetween, в т.ч. високосный год")
    void days() {
        assertThat(Task08FilesDates.daysBetween(LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 1))).isEqualTo(29);
        assertThat(Task08FilesDates.daysBetween(LocalDate.of(2024, 1, 10), LocalDate.of(2024, 1, 1))).isEqualTo(-9);
    }

    @Test
    @DisplayName("format / parse")
    void formatParse() {
        assertThat(Task08FilesDates.format(LocalDate.of(2020, 8, 31))).isEqualTo("31.08.2020");
        assertThat(Task08FilesDates.parse("01.02.2024")).isEqualTo(LocalDate.of(2024, 2, 1));
    }
}
