package ru.qacourse.m03;

/**
 * Задание 3.10: код написан, но работает неправильно. Найдите ошибки с помощью отладчика.
 */
public class Task10Debug {
    /**
     * Количество гласных (а, е, ё, и, о, у, ы, э, ю, я) в строке без учёта регистра.
     */
    public static int countVowels(String s) {
        // Здесь две ошибки. Поставьте точку останова и пройдите цикл по шагам.
        String vowels = "аеёиоуыэюя";
        int count = 0;
        for (int i = 1; i < s.length(); i++) {
            if (vowels.indexOf(s.charAt(i)) >= 0) {
                count++;
            }
        }
        return count;
    }

    /**
     * Минимальная цена в массиве (массив не пустой).
     */
    public static double minPrice(double[] prices) {
        // Здесь одна ошибка. Посмотрите в отладчике значение min после первой итерации.
        double min = 0;
        for (double p : prices) {
            if (p < min) {
                min = p;
            }
        }
        return min;
    }
}
