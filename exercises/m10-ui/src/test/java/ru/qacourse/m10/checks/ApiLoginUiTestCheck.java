package ru.qacourse.m10.checks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.qacourse.support.BugCatcher;

/** Проверка задания. Не изменяйте этот файл. */
class ApiLoginUiTestCheck {

    @Test
    @DisplayName("Ловит BUG-702 (почта вместо имени в шапке)")
    void catches() {
        BugCatcher.assertCatches(ru.qacourse.m10.tasks.ApiLoginUiTest.class, "BUG-702");
    }
}
