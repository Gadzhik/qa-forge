package ru.qacourse.m05;

import java.util.List;
import java.util.Optional;

/**
 * Задание 5.7: Optional — «может быть, значение есть, а может, нет».
 */
public class Task07Optional {

    /** Ищет товар по точному названию. */
    public static Optional<Item> findByName(List<Item> items, String name) {
        return items.stream().filter(i -> i.name().equals(name)).findFirst();
    }

    /** Цена товара по названию или 0.0, если товара нет. */
    public static double priceOrZero(List<Item> items, String name) {
        return findByName(items, name).map(Item::price).orElse(0.0);
    }

    /** Категория товара по названию; если товара нет — бросить IllegalArgumentException("Нет товара: X"). */
    public static String categoryOrThrow(List<Item> items, String name) {
        return findByName(items, name)
                .map(Item::category)
                .orElseThrow(() -> new IllegalArgumentException("Нет товара: " + name));
    }

    /** Превращает строку в Optional: null и пустые/пробельные строки → Optional.empty(), иначе обрезанная строка. */
    public static Optional<String> nonBlank(String value) {
        return Optional.ofNullable(value).map(String::trim).filter(s -> !s.isEmpty());
    }
}
