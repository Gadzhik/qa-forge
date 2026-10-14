package ru.qacourse.m07.sut;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Каталог — тестируемый класс для задания 7.3.
 * search(text) — товары, в названии которых есть text без учёта регистра, отсортированные по цене ПО УБЫВАНИЮ.
 * all() — все товары в исходном порядке.
 */
public class Catalog {

    public record Product(String name, String category, double price) {
    }

    private final List<Product> products = List.of(
            new Product("Классический бургер", "BURGER", 289.9),
            new Product("Чизбургер двойной", "BURGER", 379.9),
            new Product("Бургер Острый", "BURGER", 339.5),
            new Product("Картофель фри", "SIDE", 129.0),
            new Product("Кола 0.5", "DRINK", 99.0),
            new Product("Лимонад домашний", "DRINK", 149.9),
            new Product("Чизкейк", "DESSERT", 229.0));

    public List<Product> all() {
        return products;
    }

    public List<Product> search(String text) {
        Comparator<Product> byPrice = Comparator.comparingDouble(Product::price);
        if (!Mutant.on(Mutant.SORT_ASCENDING)) byPrice = byPrice.reversed();
        List<Product> found = products.stream()
                .filter(p -> Mutant.on(Mutant.SEARCH_CASE_SENSITIVE)
                        ? p.name().contains(text)
                        : p.name().toLowerCase(Locale.ROOT).contains(text.toLowerCase(Locale.ROOT)))
                .sorted(byPrice)
                .toList();
        if (Mutant.on(Mutant.SEARCH_LOSES_LAST) && found.size() > 1) {
            return found.subList(0, found.size() - 1);
        }
        return found;
    }
}
