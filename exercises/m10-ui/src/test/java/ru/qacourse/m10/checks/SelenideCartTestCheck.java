package ru.qacourse.m10.checks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.qacourse.support.BugCatcher;

/** Проверка задания. Не изменяйте этот файл. */
class SelenideCartTestCheck {

    @Test
    @DisplayName("Ловит BUG-703 (кнопка «В корзину» ничего не добавляет)")
    void button() {
        BugCatcher.assertCatches(ru.qacourse.m10.tasks.SelenideCartTest.class, "BUG-703");
    }

    @Test
    @DisplayName("Ловит BUG-701 (счётчик корзины показывает позиции, а не штуки)")
    void counter() {
        BugCatcher.assertCatches(ru.qacourse.m10.tasks.SelenideCartTest.class, "BUG-701");
    }
}
