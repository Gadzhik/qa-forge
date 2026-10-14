package ru.qacourse.m04;

/**
 * Задание 4.9: обобщённый (generic) класс — пара значений любых типов.
 * Pair<String, Integer> pair = new Pair<>("Кола", 2);
 */
public class Pair<A, B> {
    private final A first;
    private final B second;

    public Pair(A first, B second) {
        this.first = first;
        this.second = second;
    }

    public A getFirst() {
        return first;
    }

    public B getSecond() {
        return second;
    }

    /** Новая пара с переставленными значениями: ("Кола", 2) → (2, "Кола"). */
    public Pair<B, A> swap() {
        return new Pair<>(second, first);
    }

    /** Обобщённый статический метод: первый аргумент, если он не null, иначе второй. */
    public static <T> T firstNonNull(T a, T b) {
        return a != null ? a : b;
    }
}
