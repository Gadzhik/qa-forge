package ru.qacourse.m04;

/**
 * Задание 4.2: инкапсуляция. Количество нельзя сделать некорректным снаружи класса.
 */
public class CartItem {
    private final String productName;
    private int quantity;

    /** Начальное количество — 1. */
    public CartItem(String productName) {
        this.productName = productName;
        this.quantity = 1;
    }

    public String getProductName() {
        return productName;
    }

    public int getQuantity() {
        return quantity;
    }

    /**
     * Меняет количество, только если новое значение от 1 до 99 включительно.
     * Возвращает true, если количество изменено, и false, если значение отклонено (тогда количество не меняется).
     */
    public boolean changeQuantity(int newQuantity) {
        throw new UnsupportedOperationException("TODO: напишите решение");
    }

    /** Увеличивает количество на 1, но не больше 99. */
    public void increment() {
        throw new UnsupportedOperationException("TODO: напишите решение");
    }
}
