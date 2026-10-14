package ru.qacourse.gradle;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PriceCalculatorTest {

    @Test
    @Tag("smoke")
    @DisplayName("Скидка 10% от 200 = 180")
    void tenPercent() {
        assertThat(PriceCalculator.withDiscount(200, 10)).isEqualTo(180.0);
    }

    @Test
    @DisplayName("Скидка 0% не меняет цену")
    void zeroPercent() {
        assertThat(PriceCalculator.withDiscount(99, 0)).isEqualTo(99.0);
    }

    @Test
    @DisplayName("Скидка больше 100% — ошибка")
    void tooBig() {
        assertThatThrownBy(() -> PriceCalculator.withDiscount(99, 101))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @Tag("slow")
    @DisplayName("Медленный тест (исключён из test, запускается отдельно)")
    void slow() throws InterruptedException {
        Thread.sleep(2000);
        assertThat(System.getProperty("shop.url")).startsWith("http");
    }
}
