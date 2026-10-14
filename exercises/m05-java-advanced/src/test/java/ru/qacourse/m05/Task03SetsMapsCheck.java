package ru.qacourse.m05;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

/** Проверка задания. Не изменяйте этот файл. */
class Task03SetsMapsCheck {

    @Test
    @DisplayName("uniqueCategories сохраняет порядок первого появления")
    void unique() {
        assertThat(Task03SetsMaps.uniqueCategories(List.of("DRINK", "BURGER", "DRINK", "SIDE", "BURGER")))
                .containsExactly("DRINK", "BURGER", "SIDE");
    }

    @Test
    @DisplayName("countWords")
    void count() {
        assertThat(Task03SetsMaps.countWords(List.of("кола", "фри", "кола")))
                .containsExactlyInAnyOrderEntriesOf(Map.of("кола", 2, "фри", 1));
        assertThat(Task03SetsMaps.countWords(List.of())).isEmpty();
    }

    @Test
    @DisplayName("stockOf: нет товара → 0")
    void stockOf() {
        Map<String, Integer> stock = Map.of("Кола", 200, "Вода", 0);
        assertThat(Task03SetsMaps.stockOf(stock, "Кола")).isEqualTo(200);
        assertThat(Task03SetsMaps.stockOf(stock, "Пиво")).isZero();
    }

    @Test
    @DisplayName("outOfStock — по алфавиту")
    void outOfStock() {
        Map<String, Integer> stock = new HashMap<>(Map.of("Чизкейк", 0, "Кола", 5, "Вода", 0));
        assertThat(Task03SetsMaps.outOfStock(stock)).containsExactly("Вода", "Чизкейк");
    }
}
