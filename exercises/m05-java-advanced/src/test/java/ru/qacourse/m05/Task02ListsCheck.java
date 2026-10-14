package ru.qacourse.m05;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/** Проверка задания. Не изменяйте этот файл. */
class Task02ListsCheck {

    @Test
    @DisplayName("addIfAbsent не добавляет дубликаты")
    void addIfAbsent() {
        List<String> names = new ArrayList<>(List.of("Кола"));
        assertThat(Task02Lists.addIfAbsent(names, "Фри")).isTrue();
        assertThat(Task02Lists.addIfAbsent(names, "Кола")).isFalse();
        assertThat(names).containsExactly("Кола", "Фри");
    }

    @Test
    @DisplayName("cheaperThan возвращает новый список, исходный не меняется")
    void cheaper() {
        List<Double> prices = new ArrayList<>(List.of(99.0, 289.9, 129.0, 200.0));
        assertThat(Task02Lists.cheaperThan(prices, 200)).containsExactly(99.0, 129.0, 200.0);
        assertThat(prices).hasSize(4);
    }

    @Test
    @DisplayName("removeBlank удаляет пустые строки")
    void removeBlank() {
        List<String> lines = new ArrayList<>(List.of("a", "", "  ", "b"));
        Task02Lists.removeBlank(lines);
        assertThat(lines).containsExactly("a", "b");
    }

    @Test
    @DisplayName("firstN")
    void firstN() {
        assertThat(Task02Lists.firstN(List.of("a", "b", "c"), 2)).containsExactly("a", "b");
        assertThat(Task02Lists.firstN(List.of("a"), 5)).containsExactly("a");
    }
}
