package ru.qacourse.m04;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/** Проверка задания. Не изменяйте этот файл. */
class DiscountCheck {

    @Test
    @DisplayName("PercentDiscount(10): 1000 ₽ → 900 ₽, название «Скидка 10%»")
    void percent() {
        PercentDiscount d = new PercentDiscount(10);
        assertThat(d.apply(100_000)).isEqualTo(90_000);
        assertThat(d.apply(999)).isEqualTo(900);
        assertThat(d.title()).isEqualTo("Скидка 10%");
    }

    @Test
    @DisplayName("FixedDiscount(150 ₽): не уходит в минус, название «Скидка 150 ₽»")
    void fixed() {
        FixedDiscount d = new FixedDiscount(15_000);
        assertThat(d.apply(100_000)).isEqualTo(85_000);
        assertThat(d.apply(10_000)).isZero();
        assertThat(d.title()).isEqualTo("Скидка 150 ₽");
    }

    @Test
    @DisplayName("Обе скидки реализуют интерфейс DiscountRule")
    void implementsInterface() {
        assertThat(DiscountRule.class).isAssignableFrom(PercentDiscount.class);
        assertThat(DiscountRule.class).isAssignableFrom(FixedDiscount.class);
    }
}
