package ru.qacourse.m04;

/**
 * Задание 4.4: equals, hashCode, toString.
 * Деньги храним в копейках (long), чтобы не было ошибок округления double.
 */
public class Money {
    private final long kopecks;

    public Money(long kopecks) {
        this.kopecks = kopecks;
    }

    /** Удобный способ создать из рублей и копеек: Money.of(289, 90). */
    public static Money of(long rubles, int kopecks) {
        return new Money(rubles * 100 + kopecks);
    }

    public long getKopecks() {
        return kopecks;
    }

    public Money plus(Money other) {
        return new Money(kopecks + other.kopecks);
    }

    public Money times(int n) {
        return new Money(kopecks * n);
    }

    // TODO: переопределите equals(Object) — деньги равны, если равны копейки
    // TODO: переопределите hashCode() — согласованно с equals
    // TODO: переопределите toString() — формат "289.90 ₽" (всегда две цифры копеек: 5 копеек → "0.05 ₽")
}
