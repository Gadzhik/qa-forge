package ru.qacourse.m04;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/** Проверка задания. Не изменяйте этот файл. */
class DiscountCalculatorCheck {

    private final DiscountRule[] rules = {new NoDiscount(), new PercentDiscount(10), new FixedDiscount(15_000)};

    @Test
    @DisplayName("Для 1000 ₽ выгоднее фиксированная скидка 150 ₽")
    void smallOrder() {
        assertThat(DiscountCalculator.best(100_000, rules)).isInstanceOf(FixedDiscount.class);
        assertThat(DiscountCalculator.bestPrice(100_000, rules)).isEqualTo(85_000);
    }

    @Test
    @DisplayName("Для 3000 ₽ выгоднее 10%")
    void bigOrder() {
        assertThat(DiscountCalculator.best(300_000, rules)).isInstanceOf(PercentDiscount.class);
        assertThat(DiscountCalculator.bestPrice(300_000, rules)).isEqualTo(270_000);
    }

    @Test
    @DisplayName("Работает с любым новым правилом (анонимный класс)")
    void anyRule() {
        DiscountRule free = new DiscountRule() {
            public long apply(long amount) { return 0; }
            public String title() { return "Бесплатно"; }
        };
        assertThat(DiscountCalculator.best(100, new DiscountRule[]{new NoDiscount(), free}).title()).isEqualTo("Бесплатно");
    }
}
