package ru.qacourse.m03;

/**
 * Задание 3.3: примитивы, ссылки, приведение типов.
 */
public class Task03Types {
    /**
     * Среднее арифметическое двух целых — с дробной частью. average(3, 4) = 3.5.
     */
    public static double average(int a, int b) {
        return (a + b) / 2.0;
    }

    /**
     * Совпадает ли текст двух строк? Сравнивайте правильно (не через ==).
     */
    public static boolean sameText(String a, String b) {
        return a.equals(b);
    }

    /**
     * Превращает строку с числом в int: "42" → 42.
     */
    public static int parse(String number) {
        return Integer.parseInt(number);
    }

    /**
     * Возвращает длину строки или 0, если строка null.
     */
    public static int safeLength(String s) {
        return s == null ? 0 : s.length();
    }
}
