package ru.qacourse.m10.checks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.qacourse.support.BugCatcher;

/** Проверка задания. Не изменяйте этот файл. */
class PageObjectCheck {

    @Test
    @DisplayName("Ваши Page Object работают в тесте-образце")
    void works() {
        BugCatcher.assertPassesOnCleanApp(PageObjectSample.class);
    }

    @Test
    @DisplayName("Тест на Page Object ловит BUG-702 (почта вместо имени в шапке)")
    void catches() {
        BugCatcher.assertCatches(PageObjectSample.class, "BUG-702");
    }
}
