package ru.qacourse.m05;

import java.util.List;

/**
 * Задание 5.5: Stream API — основы. Каждый метод — одна цепочка stream().
 */
public class Task05Streams {

    /** Названия товаров категории category, отсортированные по алфавиту. */
    public static List<String> namesInCategory(List<Item> items, String category) {
        return items.stream()
                .filter(i -> i.category().equals(category))
                .map(Item::name)
                .sorted()
                .toList();
    }

    /** Суммарный остаток на складе. */
    public static int totalStock(List<Item> items) {
        return items.stream().mapToInt(Item::stock).sum();
    }

    /** Есть ли хоть один товар, которого нет в наличии. */
    public static boolean anyOutOfStock(List<Item> items) {
        return items.stream().anyMatch(i -> i.stock() == 0);
    }

    /** Все ли цены положительные. */
    public static boolean allPricesPositive(List<Item> items) {
        return items.stream().allMatch(i -> i.price() > 0);
    }

    /** Три самых дорогих товара (названия, от дорогого к дешёвому). */
    public static List<String> top3Expensive(List<Item> items) {
        return items.stream()
                .sorted((a, b) -> Double.compare(b.price(), a.price()))
                .limit(3)
                .map(Item::name)
                .toList();
    }
}
