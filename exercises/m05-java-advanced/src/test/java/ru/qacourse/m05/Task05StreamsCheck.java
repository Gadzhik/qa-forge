package ru.qacourse.m05;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/** Проверка задания. Не изменяйте этот файл. */
class Task05StreamsCheck {

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
    @DisplayName("namesInCategory")
    void names() {
        assertThat(Task05Streams.namesInCategory(ITEMS, "DRINK"))
                .containsExactly("Вода без газа", "Кола 0.5", "Лимонад домашний", "Морс клюквенный");
        assertThat(Task05Streams.namesInCategory(ITEMS, "PIZZA")).isEmpty();
    }

    @Test
    @DisplayName("totalStock")
    void total() {
        assertThat(Task05Streams.totalStock(ITEMS)).isEqualTo(580);
    }

    @Test
    @DisplayName("anyOutOfStock / allPricesPositive")
    void matches() {
        assertThat(Task05Streams.anyOutOfStock(ITEMS)).isTrue();
        assertThat(Task05Streams.anyOutOfStock(ITEMS.subList(0, 3))).isFalse();
        assertThat(Task05Streams.allPricesPositive(ITEMS)).isTrue();
        assertThat(Task05Streams.allPricesPositive(List.of(new Item("x", "X", 0, 1)))).isFalse();
    }

    @Test
    @DisplayName("top3Expensive")
    void top3() {
        assertThat(Task05Streams.top3Expensive(ITEMS))
                .containsExactly("Чизбургер двойной", "Классический бургер", "Чизкейк");
    }
}
