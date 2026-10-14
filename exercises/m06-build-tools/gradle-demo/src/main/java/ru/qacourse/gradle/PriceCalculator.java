package ru.qacourse.gradle;

/** Простой класс, который тестируют демонстрационные тесты. */
public class PriceCalculator {

    public static double withDiscount(double price, int percent) {
        if (percent < 0 || percent > 100) {
            throw new IllegalArgumentException("Скидка должна быть от 0 до 100%");
        }
        return price - price * percent / 100.0;
    }
}
