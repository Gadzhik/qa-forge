package ru.qacourse.m05;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/** Проверка задания. Не изменяйте этот файл. */
class Task07OptionalCheck {

    static final List<Item> ITEMS = List.of(
            new Item("Классический бургер", "BURGER", 289.9, 50),
            new Item("Чизбургер двойной", "BURGER", 379.9, 40),
            new Item("Картофель фри", "SIDE", 129.0, 100),
            new Item("Кола 0.5", "DRINK", 99.0, 200),
            new Item("Вода без газа", "DRINK", 69.0, 0),
            new Item("Лимонад домашний", "DRINK", 149.9, 50),
            new Item("Мороженое рожок", "DESSERT", 79.0, 100),
            new Item("Чизкейк", "DESSERT", 229.0, 0),
            new Item("Морс клюквенный", "DRINK", 99.0, 40));

    @Test
    @DisplayName("findByName")
    void find() {
        assertThat(Task07Optional.findByName(ITEMS, "Чизкейк")).isPresent();
        assertThat(Task07Optional.findByName(ITEMS, "Пицца")).isEmpty();
    }

    @Test
    @DisplayName("priceOrZero")
    void price() {
        assertThat(Task07Optional.priceOrZero(ITEMS, "Кола 0.5")).isEqualTo(99.0);
        assertThat(Task07Optional.priceOrZero(ITEMS, "Пицца")).isZero();
    }

    @Test
    @DisplayName("categoryOrThrow")
    void orThrow() {
        assertThat(Task07Optional.categoryOrThrow(ITEMS, "Чизкейк")).isEqualTo("DESSERT");
        assertThatThrownBy(() -> Task07Optional.categoryOrThrow(ITEMS, "Пицца"))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("Нет товара: Пицца");
    }

    @Test
    @DisplayName("nonBlank")
    void nonBlank() {
        assertThat(Task07Optional.nonBlank(null)).isEmpty();
        assertThat(Task07Optional.nonBlank("   ")).isEmpty();
        assertThat(Task07Optional.nonBlank("  SALE10 ")).hasValue("SALE10");
    }
}
