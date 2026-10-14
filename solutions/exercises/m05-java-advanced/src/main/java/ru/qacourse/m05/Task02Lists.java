package ru.qacourse.m05;

import java.util.ArrayList;
import java.util.List;

/**
 * Задание 5.2: List.
 */
public class Task02Lists {

    /** Добавляет name в список, только если его там ещё нет. Возвращает true, если добавили. */
    public static boolean addIfAbsent(List<String> names, String name) {
        if (names.contains(name)) {
            return false;
        }
        names.add(name);
        return true;
    }

    /** НОВЫЙ список с ценами не дороже max (исходный не менять). */
    public static List<Double> cheaperThan(List<Double> prices, double max) {
        List<Double> result = new ArrayList<>();
        for (Double price : prices) {
            if (price <= max) {
                result.add(price);
            }
        }
        return result;
    }

    /** Удаляет из списка (прямо в нём) все пустые и состоящие из пробелов строки. */
    public static void removeBlank(List<String> lines) {
        lines.removeIf(String::isBlank);
    }

    /** Первые n элементов (или весь список, если элементов меньше). Новый список. */
    public static List<String> firstN(List<String> items, int n) {
        return new ArrayList<>(items.subList(0, Math.min(n, items.size())));
    }
}
