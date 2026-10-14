package ru.qacourse.m09.checks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.qacourse.support.BugCatcher;

/** Проверка задания. Не изменяйте этот файл. */
class ProductApiTestCheck {

    @Test
    @DisplayName("ProductApiTest проходит и ловит BUG-104 (500 вместо 404)")
    void catches() {
        BugCatcher.assertCatches(ru.qacourse.m09.tasks.ProductApiTest.class, "BUG-104");
    }
}
