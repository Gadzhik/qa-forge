package ru.qacourse.m05;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Задание 5.4: лямбды и функциональные интерфейсы.
 */
public class Task04Lambdas {

    /** Условие «товар дешевле maxPrice» в виде Predicate. */
    public static Predicate<Item> cheaperThan(double maxPrice) {
        return item -> item.price() < maxPrice;
    }

    /** Отбирает товары, подходящие под условие (без Stream API — циклом). */
    public static List<Item> filter(List<Item> items, Predicate<Item> condition) {
        List<Item> result = new ArrayList<>();
        for (Item item : items) {
            if (condition.test(item)) {
                result.add(item);
            }
        }
        return result;
    }

    /** Сортировка: по цене по возрастанию, при равной цене — по названию. Возвращает новый список. */
    public static List<Item> sortByPriceThenName(List<Item> items) {
        List<Item> copy = new ArrayList<>(items);
        copy.sort(Comparator.comparingDouble(Item::price).thenComparing(Item::name));
        return copy;
    }

    /** Применяет функцию к каждому элементу: map(["a","b"], String::toUpperCase) → ["A","B"]. */
    public static <T, R> List<R> map(List<T> source, Function<T, R> f) {
        List<R> result = new ArrayList<>();
        for (T t : source) {
            result.add(f.apply(t));
        }
        return result;
    }
}
