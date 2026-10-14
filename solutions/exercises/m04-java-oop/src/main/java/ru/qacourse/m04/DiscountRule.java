package ru.qacourse.m04;

/**
 * Правило скидки (задания 4.6–4.7). Интерфейс менять не нужно.
 * Суммы — в копейках.
 */
public interface DiscountRule {
    /** Возвращает сумму ПОСЛЕ скидки. Результат не может быть меньше 0. */
    long apply(long amountKopecks);

    /** Название правила для чека. */
    String title();
}
