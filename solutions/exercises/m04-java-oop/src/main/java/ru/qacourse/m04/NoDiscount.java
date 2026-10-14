package ru.qacourse.m04;

/** Пример реализации интерфейса: скидки нет. */
public class NoDiscount implements DiscountRule {
    @Override
    public long apply(long amountKopecks) {
        return amountKopecks;
    }

    @Override
    public String title() {
        return "Без скидки";
    }
}
