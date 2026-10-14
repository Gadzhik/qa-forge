package ru.qacourse.m09.checks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.qacourse.support.BugCatcher;

/** Проверка задания. Не изменяйте этот файл. */
class CatalogApiTestCheck {

    @Test
    @DisplayName("Ловит BUG-102 (сортировка по убыванию цены)")
    void sort() {
        BugCatcher.assertCatches(ru.qacourse.m09.tasks.CatalogApiTest.class, "BUG-102");
    }

    @Test
    @DisplayName("Ловит BUG-105 (фильтр по категории)")
    void filter() {
        BugCatcher.assertCatches(ru.qacourse.m09.tasks.CatalogApiTest.class, "BUG-105");
    }
}
