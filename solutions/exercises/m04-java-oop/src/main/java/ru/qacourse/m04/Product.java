package ru.qacourse.m04;

/**
 * Задание 4.1: товар. Поля, конструктор, геттеры и метод поведения.
 */
public class Product {
    private String name;
    private double price;

    public Product(String name, double price) {
        this.name = name;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    /** Дорогой ли товар: цена строго больше 300. */
    public boolean isExpensive() {
        return price > 300;
    }

    /** Стоимость n штук. */
    public double costOf(int quantity) {
        return price * quantity;
    }
}
