package ru.qacourse.m09.checks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.qacourse.support.BugCatcher;

/** Проверка задания. Не изменяйте этот файл. */
class AssertableResponseCheck {

    @Test
    @DisplayName("Обёртка работает в тесте-образце WrapperSample")
    void works() {
        BugCatcher.assertPassesOnCleanApp(WrapperSample.class);
    }
}
