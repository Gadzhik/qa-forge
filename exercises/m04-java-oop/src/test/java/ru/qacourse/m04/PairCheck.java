package ru.qacourse.m04;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/** Проверка задания. Не изменяйте этот файл. */
class PairCheck {

    @Test
    @DisplayName("Pair хранит значения разных типов")
    void stores() {
        Pair<String, Integer> p = new Pair<>("Кола", 2);
        String name = p.getFirst();
        int qty = p.getSecond();
        assertThat(name).isEqualTo("Кола");
        assertThat(qty).isEqualTo(2);
    }

    @Test
    @DisplayName("swap меняет местами и типы, и значения")
    void swap() {
        Pair<Integer, String> swapped = new Pair<>("Кола", 2).swap();
        assertThat(swapped.getFirst()).isEqualTo(2);
        assertThat(swapped.getSecond()).isEqualTo("Кола");
    }

    @Test
    @DisplayName("firstNonNull")
    void firstNonNull() {
        assertThat(Pair.firstNonNull("a", "b")).isEqualTo("a");
        assertThat(Pair.firstNonNull(null, "b")).isEqualTo("b");
        Integer n = Pair.firstNonNull(null, 5);
        assertThat(n).isEqualTo(5);
    }
}
