package ru.qacourse.m04;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/** Проверка задания. Не изменяйте этот файл. */
class ProductCheck {

    @Test
    @DisplayName("Конструктор сохраняет имя и цену")
    void constructor() {
        Product p = new Product("Кола", 99.0);
        assertThat(p.getName()).isEqualTo("Кола");
        assertThat(p.getPrice()).isEqualTo(99.0);
    }

    @Test
    @DisplayName("Два объекта не зависят друг от друга")
    void independentObjects() {
        Product a = new Product("Кола", 99.0);
        Product b = new Product("Бургер", 289.9);
        assertThat(a.getName()).isEqualTo("Кола");
        assertThat(b.getName()).isEqualTo("Бургер");
    }

    @Test
    @DisplayName("isExpensive: граница 300")
    void expensive() {
        assertThat(new Product("A", 300.0).isExpensive()).isFalse();
        assertThat(new Product("B", 300.01).isExpensive()).isTrue();
    }

    @Test
    @DisplayName("costOf(3) = цена × 3")
    void cost() {
        assertThat(new Product("Фри", 129.0).costOf(3)).isEqualTo(387.0);
    }
}
