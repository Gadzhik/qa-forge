package ru.qacourse.m04;

/**
 * Задание 4.5: наследование.
 * Администратор — это пользователь, который может удалять товары.
 */
public class Admin extends User {

    public Admin(String username) {
        super(username);
    }

    @Override
    public boolean canDeleteProducts() {
        return true;
    }

    @Override
    public String describe() {
        return super.describe() + " (администратор)";
    }
}
