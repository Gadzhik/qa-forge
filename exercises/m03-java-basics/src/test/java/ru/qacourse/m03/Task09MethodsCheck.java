package ru.qacourse.m03;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/** Проверка задания Task09Methods. Не изменяйте этот файл. */
class Task09MethodsCheck {
    @Test
    @DisplayName("cartTotal")
    void check1() {
        assertThat(Task09Methods.cartTotal(new double[]{100, 50.5}, new int[]{2, 3})).isCloseTo(351.5, within(1e-9));
    }

    @Test
    @DisplayName("lineTotal")
    void check2() {
        assertThat(Task09Methods.lineTotal(99, 3)).isEqualTo(297.0);
    }

    @Test
    @DisplayName("priceWithDiscount(200, 10) = 180")
    void check3() {
        assertThat(Task09Methods.priceWithDiscount(200, 10)).isEqualTo(180.0);
        assertThat(Task09Methods.priceWithDiscount(99, 50)).isEqualTo(49.5);
    }

    @Test
    @DisplayName("priceWithDiscount(200) = 200")
    void check4() {
        assertThat(Task09Methods.priceWithDiscount(200)).isEqualTo(200.0);
    }
}
