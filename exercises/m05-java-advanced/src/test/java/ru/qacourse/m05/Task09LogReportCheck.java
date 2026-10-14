package ru.qacourse.m05;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/** Проверка задания. Не изменяйте этот файл. */
class Task09LogReportCheck {

    static List<String> lines() throws Exception {
        return Files.readAllLines(Path.of(Task09LogReportCheck.class.getResource("/shop-sample.log").toURI()), StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("parse (уже реализован) разбирает строку")
    void parse() {
        var l = Task09LogReport.parse("2024-05-12T12:03:02Z ERROR [http-4] OrderService - payment failed");
        assertThat(l.level()).isEqualTo("ERROR");
        assertThat(l.logger()).isEqualTo("OrderService");
        assertThat(l.message()).isEqualTo("payment failed");
    }

    @Test
    @DisplayName("countByLevel")
    void levels() throws Exception {
        assertThat(Task09LogReport.countByLevel(lines()))
                .containsExactlyInAnyOrderEntriesOf(Map.of("INFO", 6L, "WARN", 1L, "ERROR", 3L));
    }

    @Test
    @DisplayName("errorMessages")
    void errors() throws Exception {
        assertThat(Task09LogReport.errorMessages(lines())).containsExactly(
                "payment service error for order 2: Connection refused",
                "GET /api/products/999 -> 500",
                "kitchen failed for order 3");
    }

    @Test
    @DisplayName("revenueByUser")
    void revenue() throws Exception {
        Map<String, Double> r = Task09LogReport.revenueByUser(lines());
        assertThat(r).containsOnlyKeys("user", "anna");
        assertThat(r.get("user")).isCloseTo(620.82, within(1e-9));
        assertThat(r.get("anna")).isCloseTo(289.90, within(1e-9));
    }
}
