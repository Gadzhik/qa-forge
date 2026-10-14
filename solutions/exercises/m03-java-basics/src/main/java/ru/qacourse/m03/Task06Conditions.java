package ru.qacourse.m03;

/**
 * Задание 3.6: условия.
 */
public class Task06Conditions {
    /**
     * Скидка по промокоду: SALE10 → 10, BURGER20 → 20, остальные (и null) → 0. Используйте switch.
     */
    public static int discountPercent(String code) {
        if (code == null) return 0;
        return switch (code) {
            case "SALE10" -> 10;
            case "BURGER20" -> 20;
            default -> 0;
        };
    }

    /**
     * Цена доставки: заказ от 1000 — бесплатно (0), иначе 99 + 25 за каждый км. Отрицательное расстояние → -1.
     */
    public static int deliveryCost(int orderAmount, int distanceKm) {
        if (distanceKm < 0) return -1;
        if (orderAmount >= 1000) return 0;
        return 99 + 25 * distanceKm;
    }

    /**
     * Текст статуса заказа: NEW → «Новый», PAID → «Оплачен», COOKING → «Готовится», READY → «Готов»,
     * CANCELLED → «Отменён», всё остальное → «Неизвестно».
     */
    public static String statusText(String status) {
        return switch (status) {
            case "NEW" -> "Новый";
            case "PAID" -> "Оплачен";
            case "COOKING" -> "Готовится";
            case "READY" -> "Готов";
            case "CANCELLED" -> "Отменён";
            default -> "Неизвестно";
        };
    }
}
