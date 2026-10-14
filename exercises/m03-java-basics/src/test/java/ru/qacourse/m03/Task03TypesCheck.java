package ru.qacourse.m03;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/** Проверка задания Task03Types. Не изменяйте этот файл. */
class Task03TypesCheck {
    @Test
    @DisplayName("average(3, 4) = 3.5")
    void check1() {
        assertThat(Task03Types.average(3, 4)).isEqualTo(3.5);
    }

    @Test
    @DisplayName("average(-1, 2) = 0.5")
    void check2() {
        assertThat(Task03Types.average(-1, 2)).isEqualTo(0.5);
    }

    @Test
    @DisplayName("sameText сравнивает содержимое строк")
    void check3() {
        assertThat(Task03Types.sameText(new String("QA"), new String("QA"))).isTrue();
        assertThat(Task03Types.sameText("QA", "qa")).isFalse();
    }

    @Test
    @DisplayName("parse(\"42\") = 42")
    void check4() {
        assertThat(Task03Types.parse("42")).isEqualTo(42);
        assertThat(Task03Types.parse("-7")).isEqualTo(-7);
    }

    @Test
    @DisplayName("safeLength(null) = 0")
    void check5() {
        assertThat(Task03Types.safeLength(null)).isZero();
        assertThat(Task03Types.safeLength("abc")).isEqualTo(3);
    }
}
