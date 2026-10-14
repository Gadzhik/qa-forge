package ru.qacourse.m10.checks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.qacourse.support.BugCatcher;

/** Проверка задания. Не изменяйте этот файл. */
class DownloadFilesTestCheck {

    @Test
    @DisplayName("Скачивание и чтение TXT, PDF, XLSX работает")
    void passes() {
        BugCatcher.assertPassesOnCleanApp(ru.qacourse.m10.tasks.DownloadFilesTest.class);
    }
}
