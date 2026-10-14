package ru.qacourse.m04;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/** Проверка задания. Не изменяйте этот файл. */
class EnumRecordCheck {

    @Test
    @DisplayName("Разрешённые переходы статусов")
    void allowed() {
        assertThat(OrderStatus.NEW.canTransitionTo(OrderStatus.PAID)).isTrue();
        assertThat(OrderStatus.NEW.canTransitionTo(OrderStatus.CANCELLED)).isTrue();
        assertThat(OrderStatus.PAYMENT_FAILED.canTransitionTo(OrderStatus.PAID)).isTrue();
        assertThat(OrderStatus.PAID.canTransitionTo(OrderStatus.COOKING)).isTrue();
        assertThat(OrderStatus.COOKING.canTransitionTo(OrderStatus.READY)).isTrue();
    }

    @Test
    @DisplayName("Запрещённые переходы статусов (BUG-405, BUG-502)")
    void forbidden() {
        assertThat(OrderStatus.PAID.canTransitionTo(OrderStatus.CANCELLED)).isFalse();
        assertThat(OrderStatus.PAID.canTransitionTo(OrderStatus.PAID)).isFalse();
        assertThat(OrderStatus.READY.canTransitionTo(OrderStatus.CANCELLED)).isFalse();
        assertThat(OrderStatus.CANCELLED.canTransitionTo(OrderStatus.PAID)).isFalse();
    }

    @Test
    @DisplayName("isFinal: только READY и CANCELLED")
    void finals() {
        for (OrderStatus s : OrderStatus.values()) {
            assertThat(s.isFinal()).as(s.name()).isEqualTo(s == OrderStatus.READY || s == OrderStatus.CANCELLED);
        }
    }

    @Test
    @DisplayName("record: withPrice создаёт копию, исходный объект не меняется")
    void recordCopy() {
        ProductDto cola = new ProductDto(12, "Кола", 99.0, "DRINK");
        ProductDto cheaper = cola.withPrice(79.0);
        assertThat(cheaper).isEqualTo(new ProductDto(12, "Кола", 79.0, "DRINK"));
        assertThat(cola.price()).isEqualTo(99.0);
    }

    @Test
    @DisplayName("record: isDrink")
    void isDrink() {
        assertThat(new ProductDto(12, "Кола", 99.0, "DRINK").isDrink()).isTrue();
        assertThat(new ProductDto(1, "Бургер", 289.9, "BURGER").isDrink()).isFalse();
        assertThat(new ProductDto(2, "?", 1, null).isDrink()).isFalse();
    }
}
