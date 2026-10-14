package ru.qacourse.m03;

/**
 * Задание 3.10: код написан, но работает неправильно. Найдите ошибки с помощью отладчика.
 */
public class Task10Debug {
    /**
     * Количество гласных (а, е, ё, и, о, у, ы, э, ю, я) в строке без учёта регистра.
     */
    public static int countVowels(String s) {
        String vowels = "аеёиоуыэюя";
        String lower = s.toLowerCase();
        int count = 0;
        for (int i = 0; i < lower.length(); i++) {
            if (vowels.indexOf(lower.charAt(i)) >= 0) {
                count++;
            }
        }
        return count;
    }

    /**
     * Минимальная цена в массиве (массив не пустой).
     */
    public static double minPrice(double[] prices) {
        double min = prices[0];
        for (double p : prices) {
            if (p < min) {
                min = p;
            }
        }
        return min;
    }
}
