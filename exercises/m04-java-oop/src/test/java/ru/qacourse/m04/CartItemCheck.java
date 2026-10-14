package ru.qacourse.m04;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/** Проверка задания. Не изменяйте этот файл. */
class CartItemCheck {

    @Test
    @DisplayName("Новая позиция — 1 шт.")
    void initial() {
        assertThat(new CartItem("Кола").getQuantity()).isEqualTo(1);
    }

    @Test
    @DisplayName("changeQuantity принимает 1..99")
    void valid() {
        CartItem item = new CartItem("Кола");
        assertThat(item.changeQuantity(99)).isTrue();
        assertThat(item.getQuantity()).isEqualTo(99);
        assertThat(item.changeQuantity(1)).isTrue();
        assertThat(item.getQuantity()).isEqualTo(1);
    }

    @Test
    @DisplayName("changeQuantity отклоняет 0, -1, 100 и не меняет количество")
    void invalid() {
        CartItem item = new CartItem("Кола");
        item.changeQuantity(5);
        assertThat(item.changeQuantity(0)).isFalse();
        assertThat(item.changeQuantity(-1)).isFalse();
        assertThat(item.changeQuantity(100)).isFalse();
        assertThat(item.getQuantity()).isEqualTo(5);
    }

    @Test
    @DisplayName("increment не превышает 99")
    void incrementLimit() {
        CartItem item = new CartItem("Кола");
        item.increment();
        assertThat(item.getQuantity()).isEqualTo(2);
        item.changeQuantity(99);
        item.increment();
        assertThat(item.getQuantity()).isEqualTo(99);
    }

    @Test
    @DisplayName("Поле quantity приватное")
    void privateField() throws Exception {
        assertThat(java.lang.reflect.Modifier.isPrivate(CartItem.class.getDeclaredField("quantity").getModifiers())).isTrue();
    }
}
