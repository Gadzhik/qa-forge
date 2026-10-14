package ru.qacourse.m09.checks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.qacourse.support.BugCatcher;

/** Проверка задания. Не изменяйте этот файл. */
class FakerSoftTestCheck {

    @Test
    @DisplayName("Ловит BUG-203 (пароль без учёта регистра)")
    void caseSensitive() {
        BugCatcher.assertCatches(ru.qacourse.m09.tasks.FakerSoftTest.class, "BUG-203");
    }
}
