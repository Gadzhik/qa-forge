package ru.qacourse.m06;

/**
 * Задание 6.5: покупатель для тестовых данных.
 * Добавьте аннотации Lombok, чтобы у класса появились:
 * геттеры и сеттеры, equals/hashCode, toString, конструктор без параметров,
 * конструктор со всеми полями и билдер (Customer.builder().name("Анна").build()).
 */
public class Customer {
    private String name;
    private String email;
    private String phone;
    private boolean vip;
}
