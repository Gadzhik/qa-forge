package ru.qacourse.m03;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/** Проверка задания Task01Greeting. Не изменяйте этот файл. */
class Task01GreetingCheck {
    @Test
    @DisplayName("hello(\"Анна\") = «Привет, Анна!»")
    void check1() {
        assertThat(Task01Greeting.hello("Анна")).isEqualTo("Привет, Анна!");
    }

    @Test
    @DisplayName("hello(\"QA\") = «Привет, QA!»")
    void check2() {
        assertThat(Task01Greeting.hello("QA")).isEqualTo("Привет, QA!");
    }

    @Test
    @DisplayName("menu() — три строки через \\n")
    void check3() {
        assertThat(Task01Greeting.menu()).isEqualTo("QA Burger\nМеню\nКорзина");
    }
}
