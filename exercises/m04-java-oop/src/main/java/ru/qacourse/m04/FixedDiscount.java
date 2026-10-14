package ru.qacourse.m04;

/**
 * Задание 4.6: фиксированная скидка в копейках. Сумма не может стать отрицательной.
 */
public class FixedDiscount implements DiscountRule {
    private final long discountKopecks;

    public FixedDiscount(long discountKopecks) {
        this.discountKopecks = discountKopecks;
    }

    @Override
    public long apply(long amountKopecks) {
        throw new UnsupportedOperationException("TODO: напишите решение");
    }

    /** Например: "Скидка 150 ₽" (только рубли, копейки скидки всегда 0). */
    @Override
    public String title() {
        throw new UnsupportedOperationException("TODO: напишите решение");
    }
}
