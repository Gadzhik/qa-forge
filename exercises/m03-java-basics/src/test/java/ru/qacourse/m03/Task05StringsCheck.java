package ru.qacourse.m03;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/** Проверка задания Task05Strings. Не изменяйте этот файл. */
class Task05StringsCheck {
    @Test
    @DisplayName("normalizeName")
    void check1() {
        assertThat(Task05Strings.normalizeName("  аННА ")).isEqualTo("Анна");
        assertThat(Task05Strings.normalizeName("борис")).isEqualTo("Борис");
        assertThat(Task05Strings.normalizeName("   ")).isEqualTo("");
    }

    @Test
    @DisplayName("countChar")
    void check2() {
        assertThat(Task05Strings.countChar("бургер", 'р')).isEqualTo(2);
        assertThat(Task05Strings.countChar("кола", 'z')).isZero();
    }

    @Test
    @DisplayName("maskCard")
    void check3() {
        assertThat(Task05Strings.maskCard("1234567812345678")).isEqualTo("**** **** **** 5678");
    }

    @Test
    @DisplayName("isPalindrome")
    void check4() {
        assertThat(Task05Strings.isPalindrome("Шалаш")).isTrue();
        assertThat(Task05Strings.isPalindrome("Бургер")).isFalse();
    }
}
