package ru.qacourse.m07.tasks;

import org.junit.jupiter.api.Test;
import ru.qacourse.m07.sut.PasswordValidator;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Задание 7.4: параметризованные тесты.
 * Требования к паролю — в комментарии к PasswordValidator. Примените граничные значения (урок 1.5).
 * Используйте @ParameterizedTest: один метод для корректных паролей, один — для некорректных.
 */
public class PasswordValidatorTest {

    @Test
    void simpleValid() {
        assertThat(PasswordValidator.isValid("secret1")).isTrue();
    }
}
