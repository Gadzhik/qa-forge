package ru.qacourse.m03;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/** Проверка задания Task04Operators. Не изменяйте этот файл. */
class Task04OperatorsCheck {
    @Test
    @DisplayName("isEven")
    void check1() {
        assertThat(Task04Operators.isEven(4)).isTrue();
        assertThat(Task04Operators.isEven(7)).isFalse();
        assertThat(Task04Operators.isEven(0)).isTrue();
        assertThat(Task04Operators.isEven(-2)).isTrue();
    }

    @Test
    @DisplayName("lastDigit")
    void check2() {
        assertThat(Task04Operators.lastDigit(1234)).isEqualTo(4);
        assertThat(Task04Operators.lastDigit(-56)).isEqualTo(6);
        assertThat(Task04Operators.lastDigit(0)).isZero();
    }

    @Test
    @DisplayName("isLeapYear: 2024 да, 2023 нет, 1900 нет, 2000 да")
    void check3() {
        assertThat(Task04Operators.isLeapYear(2024)).isTrue();
        assertThat(Task04Operators.isLeapYear(2023)).isFalse();
        assertThat(Task04Operators.isLeapYear(1900)).isFalse();
        assertThat(Task04Operators.isLeapYear(2000)).isTrue();
    }

    @Test
    @DisplayName("isValidQuantity — граничные значения 0, 1, 99, 100")
    void check4() {
        assertThat(Task04Operators.isValidQuantity(0)).isFalse();
        assertThat(Task04Operators.isValidQuantity(1)).isTrue();
        assertThat(Task04Operators.isValidQuantity(99)).isTrue();
        assertThat(Task04Operators.isValidQuantity(100)).isFalse();
    }
}
