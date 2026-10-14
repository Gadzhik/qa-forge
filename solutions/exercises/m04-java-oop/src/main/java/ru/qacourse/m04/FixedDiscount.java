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
        return Math.max(0, amountKopecks - discountKopecks);
    }

    /** Например: "Скидка 150 ₽" (только рубли, копейки скидки всегда 0). */
    @Override
    public String title() {
        return "Скидка " + discountKopecks / 100 + " ₽";
    }
}
