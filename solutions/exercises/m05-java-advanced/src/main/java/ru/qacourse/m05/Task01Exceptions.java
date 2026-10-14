package ru.qacourse.m05;

/**
 * Задание 5.1: исключения.
 */
public class Task01Exceptions {

    /**
     * Проверяет количество. Если оно не от 1 до 99 — бросает InvalidQuantityException
     * с сообщением "Количество должно быть от 1 до 99, а передано: X".
     */
    public static void validate(int quantity) {
        if (quantity < 1 || quantity > 99) {
            throw new InvalidQuantityException("Количество должно быть от 1 до 99, а передано: " + quantity);
        }
    }

    /**
     * Превращает строку в количество. Если строка — не число, бросает InvalidQuantityException
     * с сообщением "Не число: X" и ПРИЧИНОЙ (cause) — исходным NumberFormatException.
     * Затем проверяет диапазон через validate.
     */
    public static int parseQuantity(String text) {
        int value;
        try {
            value = Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            throw new InvalidQuantityException("Не число: " + text, e);
        }
        validate(value);
        return value;
    }

    /** Как parseQuantity, но вместо исключения возвращает defaultValue. */
    public static int parseOrDefault(String text, int defaultValue) {
        try {
            return parseQuantity(text);
        } catch (InvalidQuantityException e) {
            return defaultValue;
        }
    }
}
