package ru.qacourse.m04;

/**
 * Задание 4.8: record — компактный неизменяемый класс данных.
 * Так в автотестах описывают JSON-ответы API (модуль 9).
 */
public record ProductDto(long id, String name, double price, String category) {

    /** Новый объект с другой ценой (record неизменяем, поэтому создаём копию). */
    public ProductDto withPrice(double newPrice) {
        throw new UnsupportedOperationException("TODO: напишите решение");
    }

    /** Напиток ли это (category равна "DRINK"). */
    public boolean isDrink() {
        throw new UnsupportedOperationException("TODO: напишите решение");
    }
}
