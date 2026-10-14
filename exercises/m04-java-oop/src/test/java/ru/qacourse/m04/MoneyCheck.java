package ru.qacourse.m04;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/** Проверка задания. Не изменяйте этот файл. */
class MoneyCheck {

    @Test
    @DisplayName("equals: одинаковые суммы равны")
    void equalsWorks() {
        assertThat(Money.of(289, 90)).isEqualTo(new Money(28_990));
        assertThat(Money.of(289, 90)).isNotEqualTo(Money.of(289, 91));
        assertThat(Money.of(1, 0)).isNotEqualTo("1.00 ₽");
        assertThat(Money.of(1, 0).equals(null)).isFalse();
    }

    @Test
    @DisplayName("hashCode согласован с equals")
    void hashCodeWorks() {
        assertThat(Money.of(99, 0).hashCode()).isEqualTo(new Money(9_900).hashCode());
        java.util.Set<Money> set = new java.util.HashSet<>();
        set.add(Money.of(10, 0));
        set.add(new Money(1000));
        assertThat(set).hasSize(1);
    }

    @Test
    @DisplayName("toString: «289.90 ₽», «0.05 ₽»")
    void toStringWorks() {
        assertThat(Money.of(289, 90)).hasToString("289.90 ₽");
        assertThat(new Money(5)).hasToString("0.05 ₽");
        assertThat(Money.of(289, 90).times(2).plus(Money.of(0, 20))).hasToString("580.00 ₽");
    }
}
