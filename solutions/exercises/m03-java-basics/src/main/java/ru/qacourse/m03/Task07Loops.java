package ru.qacourse.m03;

/**
 * Задание 3.7: циклы.
 */
public class Task07Loops {
    /**
     * Сумма чисел от 1 до n включительно. sumTo(0) = 0.
     */
    public static int sumTo(int n) {
        int sum = 0;
        for (int i = 1; i <= n; i++) {
            sum += i;
        }
        return sum;
    }

    /**
     * Факториал: 5! = 1·2·3·4·5 = 120, 0! = 1.
     */
    public static long factorial(int n) {
        long result = 1;
        for (int i = 2; i <= n; i++) {
            result *= i;
        }
        return result;
    }

    /**
     * FizzBuzz от 1 до n через пробел: кратные 3 → Fizz, кратные 5 → Buzz, кратные 15 → FizzBuzz.
     * fizzBuzz(5) = "1 2 Fizz 4 Buzz".
     */
    public static String fizzBuzz(int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= n; i++) {
            if (i > 1) sb.append(' ');
            if (i % 15 == 0) sb.append("FizzBuzz");
            else if (i % 3 == 0) sb.append("Fizz");
            else if (i % 5 == 0) sb.append("Buzz");
            else sb.append(i);
        }
        return sb.toString();
    }

    /**
     * Количество цифр в числе: 12345 → 5, 0 → 1, -77 → 2. Используйте while.
     */
    public static int countDigits(int n) {
        int value = Math.abs(n);
        int count = 1;
        while (value >= 10) {
            value /= 10;
            count++;
        }
        return count;
    }
}
