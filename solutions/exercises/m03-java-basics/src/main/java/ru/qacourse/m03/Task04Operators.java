package ru.qacourse.m03;

/**
 * Задание 3.4: операторы.
 */
public class Task04Operators {
    /**
     * Чётное ли число? Подсказка: оператор % (остаток от деления).
     */
    public static boolean isEven(int n) {
        return n % 2 == 0;
    }

    /**
     * Последняя цифра числа: 1234 → 4, -56 → 6.
     */
    public static int lastDigit(int n) {
        return Math.abs(n % 10);
    }

    /**
     * Високосный ли год: делится на 4, но не на 100, либо делится на 400.
     */
    public static boolean isLeapYear(int year) {
        return (year % 4 == 0 && year % 100 != 0) || year % 400 == 0;
    }

    /**
     * Проходит ли количество по правилу корзины: от 1 до 99 включительно.
     */
    public static boolean isValidQuantity(int quantity) {
        return quantity >= 1 && quantity <= 99;
    }
}
