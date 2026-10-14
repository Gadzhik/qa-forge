package ru.qacourse.m07.sut;

import java.util.EnumSet;
import java.util.Set;

/**
 * Заложенные ошибки («мутанты») в тестируемых классах. Проверки заданий включают их по очереди,
 * чтобы убедиться, что ваши тесты их ловят. Вручную включать не нужно (но можно — для эксперимента).
 */
public enum Mutant {
    TOTAL_IGNORES_QUANTITY,
    SAME_ITEM_REPLACES,
    REMOVE_DOES_NOTHING,
    PROMO_TWICE,
    SEARCH_CASE_SENSITIVE,
    SORT_ASCENDING,
    SEARCH_LOSES_LAST,
    PASSWORD_MIN_5,
    PASSWORD_MAX_31,
    PASSWORD_NO_DIGIT_NEEDED;

    private static final Set<Mutant> ACTIVE = EnumSet.noneOf(Mutant.class);

    public static synchronized void activate(Mutant... mutants) {
        ACTIVE.clear();
        ACTIVE.addAll(Set.of(mutants));
    }

    public static synchronized void reset() {
        ACTIVE.clear();
    }

    public static synchronized boolean on(Mutant m) {
        return ACTIVE.contains(m);
    }
}
