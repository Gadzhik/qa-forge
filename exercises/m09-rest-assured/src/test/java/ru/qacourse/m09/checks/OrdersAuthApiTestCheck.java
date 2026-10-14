package ru.qacourse.m09.checks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.qacourse.support.BugCatcher;

/** Проверка задания. Не изменяйте этот файл. */
class OrdersAuthApiTestCheck {

    @Test
    @DisplayName("Ловит BUG-204 (видны чужие заказы)")
    void idor() {
        BugCatcher.assertCatches(ru.qacourse.m09.tasks.OrdersAuthApiTest.class, "BUG-204");
    }
}
