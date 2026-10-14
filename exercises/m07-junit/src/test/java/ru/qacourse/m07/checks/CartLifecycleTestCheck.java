package ru.qacourse.m07.checks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.qacourse.m07.sut.Mutant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.*;

/** Проверка задания. Не изменяйте этот файл. */
class CartLifecycleTestCheck {

    private final Class<?> test = ru.qacourse.m07.tasks.CartLifecycleTest.class;

    @Test
    @DisplayName("Есть метод с @BeforeEach")
    void hasBeforeEach() {
        assertThat(Arrays.stream(test.getDeclaredMethods()).anyMatch(m -> m.isAnnotationPresent(BeforeEach.class)))
                .as("метод с @BeforeEach в CartLifecycleTest").isTrue();
    }

    @Test
    @DisplayName("Есть вложенный класс @Nested")
    void hasNested() {
        assertThat(Arrays.stream(test.getDeclaredClasses()).anyMatch(c -> c.isAnnotationPresent(Nested.class)))
                .as("внутренний класс с @Nested").isTrue();
    }

    @Test
    @DisplayName("Ловит ошибки remove() и двойной скидки")
    void kills() {
        MutationCheck.assertKills(test, Mutant.REMOVE_DOES_NOTHING, Mutant.PROMO_TWICE);
    }
}
