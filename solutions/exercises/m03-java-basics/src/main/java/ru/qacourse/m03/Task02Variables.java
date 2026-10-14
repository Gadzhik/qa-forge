package ru.qacourse.m03;

/**
 * Задание 3.2: переменные и типы.
 */
public class Task02Variables {
    /**
     * Сколько секунд в сутках? Посчитайте через переменные hours, minutes, seconds.
     */
    public static int secondsInDay() {
        int hours = 24;
        int minutes = 60;
        int seconds = 60;
        return hours * minutes * seconds;
    }

    /**
     * Стоимость строки корзины: цена × количество.
     */
    public static double lineTotal(double price, int quantity) {
        return price * quantity;
    }

    /**
     * Сколько миллисекунд в году (365 дней)? Внимание: результат не помещается в int!
     */
    public static long millisInYear() {
        long days = 365;
        return days * 24 * 60 * 60 * 1000;
    }
}
