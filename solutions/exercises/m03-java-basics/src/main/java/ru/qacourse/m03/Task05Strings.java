package ru.qacourse.m03;

/**
 * Задание 3.5: строки.
 */
public class Task05Strings {
    /**
     * Приводит имя к виду «Анна»: убирает пробелы по краям, первая буква заглавная, остальные строчные.
     * "  аННА " → "Анна". Пустую строку (после trim) вернуть как есть.
     */
    public static String normalizeName(String name) {
        String t = name.trim();
        if (t.isEmpty()) return t;
        return t.substring(0, 1).toUpperCase() + t.substring(1).toLowerCase();
    }

    /**
     * Сколько раз символ c встречается в строке s.
     */
    public static int countChar(String s, char c) {
        int count = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == c) count++;
        }
        return count;
    }

    /**
     * Маскирует номер карты из 16 цифр: "1234567812345678" → "**** **** **** 5678".
     */
    public static String maskCard(String card) {
        return "**** **** **** " + card.substring(12);
    }

    /**
     * Палиндром ли строка без учёта регистра: "Шалаш" → true.
     */
    public static boolean isPalindrome(String s) {
        String lower = s.toLowerCase();
        return new StringBuilder(lower).reverse().toString().equals(lower);
    }
}
