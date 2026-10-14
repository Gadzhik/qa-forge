package ru.qacourse.m10.checks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.qacourse.support.BugCatcher;

/** Проверка задания. Не изменяйте этот файл. */
class UploadTestCheck {

    @Test
    @DisplayName("Загрузка файла через input type=file работает")
    void passes() {
        BugCatcher.assertPassesOnCleanApp(ru.qacourse.m10.tasks.UploadTest.class);
    }
}
