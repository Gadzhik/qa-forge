package ru.qacourse.m04;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/** Проверка задания. Не изменяйте этот файл. */
class OrderCheck {

    @org.junit.jupiter.api.BeforeEach
    void reset() {
        Order.resetCounter();
    }

    @Test
    @DisplayName("Номера заказов идут по порядку: 1, 2, 3")
    void numbers() {
        assertThat(new Order("SALE10").getNumber()).isEqualTo(1);
        assertThat(new Order().getNumber()).isEqualTo(2);
        assertThat(new Order("X").getNumber()).isEqualTo(3);
        assertThat(Order.getCreatedCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("Конструктор без параметров — заказ без промокода")
    void noPromo() {
        Order o = new Order();
        assertThat(o.getPromoCode()).isNull();
        assertThat(o.hasPromo()).isFalse();
    }

    @Test
    @DisplayName("hasPromo для заказа с промокодом и с пустой строкой")
    void promo() {
        assertThat(new Order("SALE10").hasPromo()).isTrue();
        assertThat(new Order("  ").hasPromo()).isFalse();
    }
}
