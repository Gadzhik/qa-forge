package ru.qacourse.m04;

/**
 * Задание 4.5 (родительский класс — менять не нужно).
 */
public class User {
    protected final String username;

    public User(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public boolean canDeleteProducts() {
        return false;
    }

    public String describe() {
        return "Пользователь " + username;
    }
}
