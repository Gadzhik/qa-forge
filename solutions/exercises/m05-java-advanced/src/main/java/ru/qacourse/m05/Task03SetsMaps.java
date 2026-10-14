package ru.qacourse.m05;

import java.util.*;

/**
 * Задание 5.3: Set и Map.
 */
public class Task03SetsMaps {

    /** Уникальные категории в порядке первого появления. */
    public static Set<String> uniqueCategories(List<String> categories) {
        return new LinkedHashSet<>(categories);
    }

    /** Сколько раз встречается каждое слово: ["кола","фри","кола"] → {кола=2, фри=1}. */
    public static Map<String, Integer> countWords(List<String> words) {
        Map<String, Integer> counts = new HashMap<>();
        for (String w : words) {
            counts.put(w, counts.getOrDefault(w, 0) + 1);
        }
        return counts;
    }

    /** Остаток товара по названию; если товара нет в map — 0. */
    public static int stockOf(Map<String, Integer> stock, String name) {
        return stock.getOrDefault(name, 0);
    }

    /** Названия товаров, которых нет в наличии (остаток 0), отсортированные по алфавиту. */
    public static List<String> outOfStock(Map<String, Integer> stock) {
        List<String> result = new ArrayList<>();
        for (Map.Entry<String, Integer> e : stock.entrySet()) {
            if (e.getValue() == 0) {
                result.add(e.getKey());
            }
        }
        Collections.sort(result);
        return result;
    }
}
