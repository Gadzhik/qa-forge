package ru.qacourse.m05;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/** Проверка задания. Не изменяйте этот файл. */
class Task04LambdasCheck {

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
    @DisplayName("cheaperThan + filter")
    void filter() {
        assertThat(Task04Lambdas.filter(ITEMS, Task04Lambdas.cheaperThan(100)))
                .extracting(Item::name).containsExactly("Кола 0.5", "Вода без газа", "Мороженое рожок", "Морс клюквенный");
    }

    @Test
    @DisplayName("filter с лямбдой из теста")
    void filterLambda() {
        assertThat(Task04Lambdas.filter(ITEMS, i -> i.stock() == 0)).extracting(Item::name)
                .containsExactly("Вода без газа", "Чизкейк");
    }

    @Test
    @DisplayName("sortByPriceThenName")
    void sort() {
        assertThat(Task04Lambdas.sortByPriceThenName(ITEMS)).extracting(Item::name)
                .startsWith("Вода без газа", "Мороженое рожок", "Кола 0.5", "Морс клюквенный", "Картофель фри")
                .endsWith("Чизбургер двойной");
    }

    @Test
    @DisplayName("map")
    void map() {
        assertThat(Task04Lambdas.map(List.of("a", "b"), String::toUpperCase)).containsExactly("A", "B");
        assertThat(Task04Lambdas.map(ITEMS.subList(0, 2), Item::price)).containsExactly(289.9, 379.9);
    }
}
