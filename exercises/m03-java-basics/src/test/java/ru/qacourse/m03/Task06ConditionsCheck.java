package ru.qacourse.m03;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/** Проверка задания Task06Conditions. Не изменяйте этот файл. */
class Task06ConditionsCheck {
    @Test
    @DisplayName("discountPercent")
    void check1() {
        assertThat(Task06Conditions.discountPercent("SALE10")).isEqualTo(10);
        assertThat(Task06Conditions.discountPercent("BURGER20")).isEqualTo(20);
        assertThat(Task06Conditions.discountPercent("NOPE")).isZero();
        assertThat(Task06Conditions.discountPercent(null)).isZero();
    }

    @Test
    @DisplayName("deliveryCost")
    void check2() {
        assertThat(Task06Conditions.deliveryCost(500, 4)).isEqualTo(199);
        assertThat(Task06Conditions.deliveryCost(1000, 10)).isZero();
        assertThat(Task06Conditions.deliveryCost(999, 0)).isEqualTo(99);
        assertThat(Task06Conditions.deliveryCost(500, -1)).isEqualTo(-1);
    }

    @Test
    @DisplayName("statusText")
    void check3() {
        assertThat(Task06Conditions.statusText("PAID")).isEqualTo("Оплачен");
        assertThat(Task06Conditions.statusText("READY")).isEqualTo("Готов");
        assertThat(Task06Conditions.statusText("???")).isEqualTo("Неизвестно");
    }
}
