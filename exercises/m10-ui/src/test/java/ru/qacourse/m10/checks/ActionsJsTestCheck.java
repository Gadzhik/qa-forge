package ru.qacourse.m10.checks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.qacourse.support.BugCatcher;

/** Проверка задания. Не изменяйте этот файл. */
class ActionsJsTestCheck {

    @Test
    @DisplayName("Drag&drop, мышь и прокрутка — тесты проходят")
    void passes() {
        BugCatcher.assertPassesOnCleanApp(ru.qacourse.m10.tasks.ActionsJsTest.class);
    }
}
