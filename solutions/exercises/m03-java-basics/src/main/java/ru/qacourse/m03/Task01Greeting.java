package ru.qacourse.m03;

/**
 * Задание 3.1: первые методы.
 */
public class Task01Greeting {
    /**
     * Возвращает приветствие вида «Привет, Анна!» для имени name.
     */
    public static String hello(String name) {
        return "Привет, " + name + "!";
    }

    /**
     * Возвращает строку из трёх строк (разделитель — символ перевода строки \n):
     * «QA Burger», «Меню», «Корзина».
     */
    public static String menu() {
        return "QA Burger\nМеню\nКорзина";
    }
}
