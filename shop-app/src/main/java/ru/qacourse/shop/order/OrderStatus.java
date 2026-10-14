package ru.qacourse.shop.order;

/**
 * Жизненный цикл заказа:
 * NEW → (оплата) → PAID → (кухня, асинхронно) → COOKING → READY.
 * NEW → PAYMENT_FAILED (оплата не прошла, можно повторить).
 * NEW / PAYMENT_FAILED → CANCELLED.
 */
public enum OrderStatus { NEW, PAYMENT_FAILED, PAID, COOKING, READY, CANCELLED }
