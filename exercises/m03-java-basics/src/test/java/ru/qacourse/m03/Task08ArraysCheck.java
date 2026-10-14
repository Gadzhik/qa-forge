package ru.qacourse.m03;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/** Проверка задания Task08Arrays. Не изменяйте этот файл. */
class Task08ArraysCheck {
    @Test
    @DisplayName("max")
    void check1() {
        assertThat(Task08Arrays.max(new int[]{3, 9, -1, 9, 2})).isEqualTo(9);
        assertThat(Task08Arrays.max(new int[]{-5, -2, -8})).isEqualTo(-2);
    }

    @Test
    @DisplayName("reversed и исходный массив не изменён")
    void check2() {
        int[] source = {1, 2, 3};
        assertThat(Task08Arrays.reversed(source)).containsExactly(3, 2, 1);
        assertThat(source).containsExactly(1, 2, 3);
    }

    @Test
    @DisplayName("isSorted")
    void check3() {
        assertThat(Task08Arrays.isSorted(new int[]{1, 2, 2, 5})).isTrue();
        assertThat(Task08Arrays.isSorted(new int[]{1, 3, 2})).isFalse();
        assertThat(Task08Arrays.isSorted(new int[]{})).isTrue();
    }

    @Test
    @DisplayName("countAbove")
    void check4() {
        assertThat(Task08Arrays.countAbove(new double[]{99, 289.9, 129, 399}, 200)).isEqualTo(2);
    }
}
