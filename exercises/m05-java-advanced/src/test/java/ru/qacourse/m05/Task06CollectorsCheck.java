package ru.qacourse.m05;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/** Проверка задания. Не изменяйте этот файл. */
class Task06CollectorsCheck {

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
    @DisplayName("countByCategory")
    void count() {
        assertThat(Task06Collectors.countByCategory(ITEMS))
                .containsExactlyInAnyOrderEntriesOf(Map.of("BURGER", 2L, "SIDE", 1L, "DRINK", 4L, "DESSERT", 2L));
    }

    @Test
    @DisplayName("averagePriceByCategory")
    void average() {
        Map<String, Double> avg = Task06Collectors.averagePriceByCategory(ITEMS);
        assertThat(avg.get("DESSERT")).isCloseTo(154.0, within(1e-9));
        assertThat(avg.get("BURGER")).isCloseTo(334.9, within(1e-9));
    }

    @Test
    @DisplayName("joinNames")
    void join() {
        assertThat(Task06Collectors.joinNames(ITEMS.subList(3, 5))).isEqualTo("Кола 0.5, Вода без газа");
        assertThat(Task06Collectors.joinNames(List.of())).isEmpty();
    }

    @Test
    @DisplayName("priceByName")
    void priceByName() {
        assertThat(Task06Collectors.priceByName(ITEMS)).hasSize(9).containsEntry("Чизкейк", 229.0);
    }

    @Test
    @DisplayName("cheapestIn")
    void cheapest() {
        assertThat(Task06Collectors.cheapestIn(ITEMS, "DRINK")).map(Item::name).hasValue("Вода без газа");
        assertThat(Task06Collectors.cheapestIn(ITEMS, "PIZZA")).isEmpty();
    }
}
