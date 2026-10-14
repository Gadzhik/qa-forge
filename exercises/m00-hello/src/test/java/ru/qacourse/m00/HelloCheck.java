package ru.qacourse.m00;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** Проверка задания 0.4. Не изменяйте этот файл. */
class HelloCheck {

    @Test
    @DisplayName("greeting() возвращает «Привет, QA!»")
    void greetingIsCorrect() {
        assertThat(Hello.greeting())
                .as("Метод greeting() должен вернуть ровно «Привет, QA!» — с запятой, пробелом и восклицательным знаком")
                .isEqualTo("Привет, QA!");
    }
}
