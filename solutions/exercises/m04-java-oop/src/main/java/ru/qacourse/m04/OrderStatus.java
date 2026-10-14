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
        return switch (this) {
            case NEW -> next == PAID || next == PAYMENT_FAILED || next == CANCELLED;
            case PAYMENT_FAILED -> next == PAID || next == CANCELLED;
            case PAID -> next == COOKING;
            case COOKING -> next == READY;
            case READY, CANCELLED -> false;
        };
    }

    /** Конечный ли статус (из него никуда нельзя перейти). */
    public boolean isFinal() {
        return this == READY || this == CANCELLED;
    }
}
