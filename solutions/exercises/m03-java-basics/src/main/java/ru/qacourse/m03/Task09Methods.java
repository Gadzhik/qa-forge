package ru.qacourse.m03;

/**
 * Задание 3.9: методы, параметры, перегрузка.
 */
public class Task09Methods {
    /**
     * Итог корзины: сумма prices[i] × quantities[i]. Массивы одной длины.
     */
    public static double cartTotal(double[] prices, int[] quantities) {
        double total = 0;
        for (int i = 0; i < prices.length; i++) {
            total += lineTotal(prices[i], quantities[i]);
        }
        return total;
    }

    /**
     * Стоимость одной строки корзины. Используйте этот метод внутри cartTotal.
     */
    public static double lineTotal(double price, int quantity) {
        return price * quantity;
    }

    /**
     * Цена со скидкой в процентах: priceWithDiscount(200, 10) = 180.
     */
    public static double priceWithDiscount(double price, int percent) {
        return price - price * percent / 100.0;
    }

    /**
     * Перегрузка: цена со скидкой по умолчанию 0% — вызовите версию с двумя параметрами.
     */
    public static double priceWithDiscount(double price) {
        return priceWithDiscount(price, 0);
    }
}
