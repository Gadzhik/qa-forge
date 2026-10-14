package ru.qacourse.m04;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/** Проверка задания. Не изменяйте этот файл. */
class AdminCheck {

    @Test
    @DisplayName("Обычный пользователь не может удалять товары")
    void user() {
        assertThat(new User("user").canDeleteProducts()).isFalse();
    }

    @Test
    @DisplayName("Администратор может удалять товары")
    void admin() {
        assertThat(new Admin("admin").canDeleteProducts()).isTrue();
    }

    @Test
    @DisplayName("describe() администратора использует описание родителя")
    void describe() {
        assertThat(new Admin("boss").describe()).isEqualTo("Пользователь boss (администратор)");
    }

    @Test
    @DisplayName("Admin можно положить в переменную типа User (полиморфизм)")
    void polymorphism() {
        User u = new Admin("boss");
        assertThat(u.canDeleteProducts()).isTrue();
    }
}
