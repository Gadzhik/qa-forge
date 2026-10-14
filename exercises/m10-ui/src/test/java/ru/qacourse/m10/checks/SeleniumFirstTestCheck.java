package ru.qacourse.m10.checks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.qacourse.support.BugCatcher;

/** Проверка задания. Не изменяйте этот файл. */
class SeleniumFirstTestCheck {

    @Test
    @DisplayName("Ловит BUG-103 (поиск с учётом регистра) через интерфейс")
    void catches() {
        BugCatcher.assertCatches(ru.qacourse.m10.tasks.SeleniumFirstTest.class, "BUG-103");
    }
}
