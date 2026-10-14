package ru.qacourse.m04;

/**
 * Задание 4.6: скидка в процентах. new PercentDiscount(10).apply(100_000) == 90_000.
 * Округление: отбрасываем дробные копейки (целочисленное деление).
 */
public class PercentDiscount implements DiscountRule {
    private final int percent;

    public PercentDiscount(int percent) {
        this.percent = percent;
    }

    @Override
    public long apply(long amountKopecks) {
        throw new UnsupportedOperationException("TODO: напишите решение");
    }

    /** Например: "Скидка 10%". */
    @Override
    public String title() {
        throw new UnsupportedOperationException("TODO: напишите решение");
    }
}
