package ru.qacourse.m09.checks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.qacourse.support.BugCatcher;

/** Проверка задания. Не изменяйте этот файл. */
class AdminProductsApiTestCheck {

    @Test
    @DisplayName("CRUD-тесты проходят на исправном приложении")
    void passes() {
        BugCatcher.assertPassesOnCleanApp(ru.qacourse.m09.tasks.AdminProductsApiTest.class);
    }
}
