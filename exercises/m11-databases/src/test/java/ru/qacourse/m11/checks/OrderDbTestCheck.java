package ru.qacourse.m11.checks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.qacourse.support.BugCatcher;

/** Проверка задания 11.7. Не изменяйте этот файл. */
class OrderDbTestCheck {
    @Test
    @DisplayName("Проверка в БД ловит BUG-404 (сумма заказа округляется вниз)")
    void catches() {
        BugCatcher.assertCatches(ru.qacourse.m11.tasks.OrderDbTest.class, "BUG-404");
    }
}
