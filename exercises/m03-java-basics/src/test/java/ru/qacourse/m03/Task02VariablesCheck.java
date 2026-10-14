package ru.qacourse.m03;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/** Проверка задания Task02Variables. Не изменяйте этот файл. */
class Task02VariablesCheck {
    @Test
    @DisplayName("secondsInDay() = 86400")
    void check1() {
        assertThat(Task02Variables.secondsInDay()).isEqualTo(86_400);
    }

    @Test
    @DisplayName("lineTotal(289.9, 2) = 579.8")
    void check2() {
        assertThat(Task02Variables.lineTotal(289.9, 2)).isCloseTo(579.8, within(0.0001));
    }

    @Test
    @DisplayName("millisInYear() = 31 536 000 000 (без переполнения int)")
    void check3() {
        assertThat(Task02Variables.millisInYear()).isEqualTo(31_536_000_000L);
    }
}
