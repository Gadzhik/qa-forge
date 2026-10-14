package ru.qacourse.m04;

/**
 * Задание 4.8: enum со свойствами и поведением. Статусы заказа QA Burger.
 */
public enum OrderStatus {
    NEW("Новый"),
    PAYMENT_FAILED("Ошибка оплаты"),
    PAID("Оплачен"),
    COOKING("Готовится"),
    READY("Готов"),
    CANCELLED("Отменён");

    private final String title;

    OrderStatus(String title) {
        this.title = title;
    }

    public String title() {
        return title;
    }

    /**
     * Разрешён ли переход в статус next (по диаграмме из урока 1.6):
     * NEW → PAID, PAYMENT_FAILED, CANCELLED;  PAYMENT_FAILED → PAID, CANCELLED;
     * PAID → COOKING;  COOKING → READY;  READY и CANCELLED — конечные.
     */
    public boolean canTransitionTo(OrderStatus next) {
        throw new UnsupportedOperationException("TODO: напишите решение");
    }

    /** Конечный ли статус (из него никуда нельзя перейти). */
    public boolean isFinal() {
        throw new UnsupportedOperationException("TODO: напишите решение");
    }
}
