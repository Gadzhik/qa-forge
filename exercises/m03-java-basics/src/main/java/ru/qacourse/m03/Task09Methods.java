package ru.qacourse.m03;

/**
 * Задание 3.9: методы, параметры, перегрузка.
 */
public class Task09Methods {
    /**
     * Итог корзины: сумма prices[i] × quantities[i]. Массивы одной длины.
     */
    public static double cartTotal(double[] prices, int[] quantities) {
        throw new UnsupportedOperationException("TODO: напишите решение");
    }

    /**
     * Стоимость одной строки корзины. Используйте этот метод внутри cartTotal.
     */
    public static double lineTotal(double price, int quantity) {
        throw new UnsupportedOperationException("TODO: напишите решение");
    }

    /**
     * Цена со скидкой в процентах: priceWithDiscount(200, 10) = 180.
     */
    public static double priceWithDiscount(double price, int percent) {
        throw new UnsupportedOperationException("TODO: напишите решение");
    }

    /**
     * Перегрузка: цена со скидкой по умолчанию 0% — вызовите версию с двумя параметрами.
     */
    public static double priceWithDiscount(double price) {
        throw new UnsupportedOperationException("TODO: напишите решение");
    }
}
