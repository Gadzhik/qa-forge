package ru.qacourse.m03;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/** Проверка задания Task07Loops. Не изменяйте этот файл. */
class Task07LoopsCheck {
    @Test
    @DisplayName("sumTo")
    void check1() {
        assertThat(Task07Loops.sumTo(10)).isEqualTo(55);
        assertThat(Task07Loops.sumTo(1)).isEqualTo(1);
        assertThat(Task07Loops.sumTo(0)).isZero();
    }

    @Test
    @DisplayName("factorial")
    void check2() {
        assertThat(Task07Loops.factorial(5)).isEqualTo(120);
        assertThat(Task07Loops.factorial(0)).isEqualTo(1);
        assertThat(Task07Loops.factorial(20)).isEqualTo(2_432_902_008_176_640_000L);
    }

    @Test
    @DisplayName("fizzBuzz")
    void check3() {
        assertThat(Task07Loops.fizzBuzz(5)).isEqualTo("1 2 Fizz 4 Buzz");
        assertThat(Task07Loops.fizzBuzz(15)).endsWith("13 14 FizzBuzz");
    }

    @Test
    @DisplayName("countDigits")
    void check4() {
        assertThat(Task07Loops.countDigits(12345)).isEqualTo(5);
        assertThat(Task07Loops.countDigits(0)).isEqualTo(1);
        assertThat(Task07Loops.countDigits(-77)).isEqualTo(2);
    }
}
