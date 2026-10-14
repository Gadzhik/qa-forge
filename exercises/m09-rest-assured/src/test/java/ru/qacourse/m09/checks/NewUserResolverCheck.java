package ru.qacourse.m09.checks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.qacourse.support.BugCatcher;

/** Проверка задания. Не изменяйте этот файл. */
class NewUserResolverCheck {

    @Test
    @DisplayName("@NewUser подставляет нового зарегистрированного пользователя с токеном")
    void works() {
        BugCatcher.assertPassesOnCleanApp(NewUserSample.class);
    }
}
