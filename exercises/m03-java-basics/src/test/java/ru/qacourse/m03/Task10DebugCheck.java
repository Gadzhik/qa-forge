package ru.qacourse.m03;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/** Проверка задания Task10Debug. Не изменяйте этот файл. */
class Task10DebugCheck {
    @Test
    @DisplayName("countVowels(\"Аня ест бургер\") = 5")
    void check1() {
        assertThat(Task10Debug.countVowels("Аня ест бургер")).isEqualTo(5);
    }

    @Test
    @DisplayName("countVowels(\"Ура\") = 2")
    void check2() {
        assertThat(Task10Debug.countVowels("Ура")).isEqualTo(2);
    }

    @Test
    @DisplayName("minPrice")
    void check3() {
        assertThat(Task10Debug.minPrice(new double[]{289.9, 99, 129})).isEqualTo(99.0);
    }
}
