package ru.qacourse.m04;

/**
 * Задание 4.3: конструкторы, this и static.
 * Каждый новый заказ получает следующий номер: 1, 2, 3...
 */
public class Order {
    /** Общий для всех заказов счётчик созданных заказов. */
    private static int createdCount = 0;

    private final int number;
    private final String promoCode;

    /** Заказ с промокодом. Номер = следующий по счёту. */
    public Order(String promoCode) {
        // TODO: увеличьте createdCount, присвойте number и promoCode
        this.number = 0;
        this.promoCode = null;
    }

    /** Заказ без промокода: вызовите первый конструктор через this(...) с promoCode = null. */
    public Order() {
        this("TODO");
    }

    public int getNumber() {
        return number;
    }

    public String getPromoCode() {
        return promoCode;
    }

    public boolean hasPromo() {
        throw new UnsupportedOperationException("TODO: напишите решение");
    }

    /** Сколько заказов создано всего. */
    public static int getCreatedCount() {
        return createdCount;
    }

    /** Сбросить счётчик (нужно проверкам). */
    public static void resetCounter() {
        createdCount = 0;
    }
}
