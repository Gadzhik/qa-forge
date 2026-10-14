package ru.qacourse.m04;

/**
 * Задание 4.7: полиморфизм. Метод работает с любыми правилами через интерфейс DiscountRule —
 * ему всё равно, процентная это скидка, фиксированная или любая другая.
 */
public class DiscountCalculator {

    /** Возвращает правило, дающее самую маленькую итоговую сумму. Массив rules не пустой. */
    public static DiscountRule best(long amountKopecks, DiscountRule[] rules) {
        DiscountRule best = rules[0];
        for (DiscountRule rule : rules) {
            if (rule.apply(amountKopecks) < best.apply(amountKopecks)) {
                best = rule;
            }
        }
        return best;
    }

    /** Итоговая сумма с лучшим правилом. */
    public static long bestPrice(long amountKopecks, DiscountRule[] rules) {
        return best(amountKopecks, rules).apply(amountKopecks);
    }
}
