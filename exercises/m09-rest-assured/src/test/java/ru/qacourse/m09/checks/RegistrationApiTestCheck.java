package ru.qacourse.m09.checks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.qacourse.support.BugCatcher;

/** Проверка задания. Не изменяйте этот файл. */
class RegistrationApiTestCheck {

    @Test
    @DisplayName("Ловит BUG-201 (дубликат email)")
    void duplicate() {
        BugCatcher.assertCatches(ru.qacourse.m09.tasks.RegistrationApiTest.class, "BUG-201");
    }

    @Test
    @DisplayName("Ловит BUG-202 (короткий пароль)")
    void shortPassword() {
        BugCatcher.assertCatches(ru.qacourse.m09.tasks.RegistrationApiTest.class, "BUG-202");
    }
}
