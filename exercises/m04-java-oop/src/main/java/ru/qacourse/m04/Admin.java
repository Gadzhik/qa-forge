package ru.qacourse.m04;

/**
 * Задание 4.5: наследование.
 * Администратор — это пользователь, который может удалять товары.
 */
public class Admin extends User {

    public Admin(String username) {
        super(username);
    }

    // TODO: переопределите canDeleteProducts() — администратор может удалять товары (@Override!)

    // TODO: переопределите describe(): верните описание родителя + " (администратор)",
    //       используя super.describe()
}
