package ru.qacourse.m05;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/** Проверка задания. Не изменяйте этот файл. */
class Task01ExceptionsCheck {

    @Test
    @DisplayName("validate пропускает 1 и 99")
    void validOk() {
        assertThatCode(() -> Task01Exceptions.validate(1)).doesNotThrowAnyException();
        assertThatCode(() -> Task01Exceptions.validate(99)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validate бросает InvalidQuantityException с понятным сообщением")
    void validThrows() {
        assertThatThrownBy(() -> Task01Exceptions.validate(0))
                .isInstanceOf(InvalidQuantityException.class)
                .hasMessage("Количество должно быть от 1 до 99, а передано: 0");
        assertThatThrownBy(() -> Task01Exceptions.validate(100)).isInstanceOf(InvalidQuantityException.class);
    }

    @Test
    @DisplayName("parseQuantity: число, пробелы, проверка диапазона")
    void parse() {
        assertThat(Task01Exceptions.parseQuantity(" 7 ")).isEqualTo(7);
        assertThatThrownBy(() -> Task01Exceptions.parseQuantity("150")).isInstanceOf(InvalidQuantityException.class);
    }

    @Test
    @DisplayName("parseQuantity: не число → InvalidQuantityException с причиной NumberFormatException")
    void parseNotNumber() {
        assertThatThrownBy(() -> Task01Exceptions.parseQuantity("два"))
                .isInstanceOf(InvalidQuantityException.class)
                .hasMessage("Не число: два")
                .hasCauseInstanceOf(NumberFormatException.class);
    }

    @Test
    @DisplayName("parseOrDefault")
    void orDefault() {
        assertThat(Task01Exceptions.parseOrDefault("5", -1)).isEqualTo(5);
        assertThat(Task01Exceptions.parseOrDefault("abc", -1)).isEqualTo(-1);
        assertThat(Task01Exceptions.parseOrDefault("0", -1)).isEqualTo(-1);
    }
}
