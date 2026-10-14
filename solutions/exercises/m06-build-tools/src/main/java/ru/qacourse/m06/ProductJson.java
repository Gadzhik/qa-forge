package ru.qacourse.m06;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Задание 6.6: модель товара для JSON вида
 * {"id": 12, "name": "Кола 0.5", "price": 99.0, "category": "DRINK", "in_stock": true}
 * Обратите внимание: в JSON поле называется in_stock, а в Java — inStock.
 */
public record ProductJson(
        long id,
        String name,
        double price,
        String category,
        @JsonProperty("in_stock") boolean inStock) {
}
