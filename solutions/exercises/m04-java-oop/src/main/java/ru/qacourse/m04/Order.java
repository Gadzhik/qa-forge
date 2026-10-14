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
        createdCount++;
        this.number = createdCount;
        this.promoCode = promoCode;
    }

    /** Заказ без промокода: вызовите первый конструктор через this(...) с promoCode = null. */
    public Order() {
        this(null);
    }

    public int getNumber() {
        return number;
    }

    public String getPromoCode() {
        return promoCode;
    }

    public boolean hasPromo() {
        return promoCode != null && !promoCode.isBlank();
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
