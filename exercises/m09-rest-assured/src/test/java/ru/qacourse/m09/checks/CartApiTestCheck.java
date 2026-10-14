package ru.qacourse.m09.checks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.qacourse.support.BugCatcher;

/** Проверка задания. Не изменяйте этот файл. */
class CartApiTestCheck {

    @Test
    @DisplayName("Ловит BUG-302 (повтор сбрасывает количество)")
    void sum() {
        BugCatcher.assertCatches(ru.qacourse.m09.tasks.CartApiTest.class, "BUG-302");
    }

    @Test
    @DisplayName("Ловит BUG-304 (итог без количества)")
    void total() {
        BugCatcher.assertCatches(ru.qacourse.m09.tasks.CartApiTest.class, "BUG-304");
    }
}
