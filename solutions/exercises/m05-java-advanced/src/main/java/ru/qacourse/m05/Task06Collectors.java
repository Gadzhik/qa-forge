package ru.qacourse.m05;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Задание 5.6: Stream API — группировка и сборщики (Collectors).
 */
public class Task06Collectors {

    /** Количество товаров в каждой категории: {BURGER=2, DRINK=4, ...}. */
    public static Map<String, Long> countByCategory(List<Item> items) {
        return items.stream().collect(Collectors.groupingBy(Item::category, Collectors.counting()));
    }

    /** Средняя цена в каждой категории. */
    public static Map<String, Double> averagePriceByCategory(List<Item> items) {
        return items.stream().collect(Collectors.groupingBy(Item::category, Collectors.averagingDouble(Item::price)));
    }

    /** Названия через запятую с пробелом в исходном порядке: "Кола 0.5, Вода без газа". */
    public static String joinNames(List<Item> items) {
        return items.stream().map(Item::name).collect(Collectors.joining(", "));
    }

    /** Map «название → цена». */
    public static Map<String, Double> priceByName(List<Item> items) {
        return items.stream().collect(Collectors.toMap(Item::name, Item::price));
    }

    /** Самый дешёвый товар категории, если в ней есть товары. */
    public static Optional<Item> cheapestIn(List<Item> items, String category) {
        return items.stream()
                .filter(i -> i.category().equals(category))
                .min((a, b) -> Double.compare(a.price(), b.price()));
    }
}
