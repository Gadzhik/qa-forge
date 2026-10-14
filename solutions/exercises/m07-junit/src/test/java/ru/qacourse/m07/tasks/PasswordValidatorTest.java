package ru.qacourse.m07.tasks;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import ru.qacourse.m07.sut.PasswordValidator;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Задание 7.4: параметризованные тесты.
 * Требования к паролю — в комментарии к PasswordValidator. Примените граничные значения (урок 1.5).
 * Используйте @ParameterizedTest: один метод для корректных паролей, один — для некорректных.
 */
public class PasswordValidatorTest {

    @ParameterizedTest(name = "«{0}» — корректный")
    @ValueSource(strings = {"abcde1", "secret12", "123456789012345678901234567890"})
    void valid(String password) {
        assertThat(PasswordValidator.isValid(password)).isTrue();
    }

    @ParameterizedTest(name = "«{0}» — некорректный")
    @NullSource
    @ValueSource(strings = {"", "abcd1", "abcdefgh", "1234567890123456789012345678901"})
    void invalid(String password) {
        assertThat(PasswordValidator.isValid(password)).isFalse();
    }
}
