package ru.qacourse.m03;

/**
 * Задание 3.8: массивы.
 */
public class Task08Arrays {
    /**
     * Максимальный элемент массива (массив не пустой).
     */
    public static int max(int[] numbers) {
        int max = numbers[0];
        for (int n : numbers) {
            if (n > max) max = n;
        }
        return max;
    }

    /**
     * Новый массив с элементами в обратном порядке (исходный не менять).
     */
    public static int[] reversed(int[] numbers) {
        int[] result = new int[numbers.length];
        for (int i = 0; i < numbers.length; i++) {
            result[i] = numbers[numbers.length - 1 - i];
        }
        return result;
    }

    /**
     * Отсортирован ли массив по неубыванию (каждый следующий ≥ предыдущего). Пустой — да.
     */
    public static boolean isSorted(int[] numbers) {
        for (int i = 1; i < numbers.length; i++) {
            if (numbers[i] < numbers[i - 1]) return false;
        }
        return true;
    }

    /**
     * Сколько цен строго больше порога.
     */
    public static int countAbove(double[] prices, double threshold) {
        int count = 0;
        for (double p : prices) {
            if (p > threshold) count++;
        }
        return count;
    }
}
