package ru.qacourse.m10.checks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.qacourse.support.BugCatcher;

/** Проверка задания. Не изменяйте этот файл. */
class CatalogSelenideTestCheck {

    @Test
    @DisplayName("Ловит BUG-105 (фильтр по категории) через интерфейс")
    void filter() {
        BugCatcher.assertCatches(ru.qacourse.m10.tasks.CatalogSelenideTest.class, "BUG-105");
    }

    @Test
    @DisplayName("Ловит BUG-102 (сортировка по убыванию цены) через интерфейс")
    void sort() {
        BugCatcher.assertCatches(ru.qacourse.m10.tasks.CatalogSelenideTest.class, "BUG-102");
    }
}
