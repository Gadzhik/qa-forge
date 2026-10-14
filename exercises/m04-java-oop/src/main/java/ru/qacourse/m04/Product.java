package ru.qacourse.m04;

/**
 * Задание 4.1: товар. Поля, конструктор, геттеры и метод поведения.
 */
public class Product {
    private String name;
    private double price;

    public Product(String name, double price) {
        // TODO: сохраните параметры в поля (подсказка: this.name = name;)
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    /** Дорогой ли товар: цена строго больше 300. */
    public boolean isExpensive() {
        throw new UnsupportedOperationException("TODO: напишите решение");
    }

    /** Стоимость n штук. */
    public double costOf(int quantity) {
        throw new UnsupportedOperationException("TODO: напишите решение");
    }
}
