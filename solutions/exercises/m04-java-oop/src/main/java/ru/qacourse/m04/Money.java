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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Money other)) return false;
        return kopecks == other.kopecks;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(kopecks);
    }

    @Override
    public String toString() {
        return String.format("%d.%02d ₽", kopecks / 100, kopecks % 100);
    }
}
