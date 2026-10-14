package ru.qacourse.m07.checks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.qacourse.m07.sut.Mutant;
import org.junit.jupiter.params.ParameterizedTest;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.*;

/** Проверка задания. Не изменяйте этот файл. */
class PasswordValidatorTestCheck {

    private final Class<?> test = ru.qacourse.m07.tasks.PasswordValidatorTest.class;

    @Test
    @DisplayName("Используется @ParameterizedTest")
    void parameterized() {
        assertThat(Arrays.stream(test.getDeclaredMethods()).filter(m -> m.isAnnotationPresent(ParameterizedTest.class)).count())
                .as("методов с @ParameterizedTest").isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("Ловит ошибки на границах длины и отсутствие проверки цифры")
    void kills() {
        MutationCheck.assertKills(test, Mutant.PASSWORD_MIN_5, Mutant.PASSWORD_MAX_31, Mutant.PASSWORD_NO_DIGIT_NEEDED);
    }
}
