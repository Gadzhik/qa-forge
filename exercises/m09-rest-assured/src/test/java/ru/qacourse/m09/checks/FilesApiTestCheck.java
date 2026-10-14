package ru.qacourse.m09.checks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.qacourse.support.BugCatcher;

/** Проверка задания. Не изменяйте этот файл. */
class FilesApiTestCheck {

    @Test
    @DisplayName("Ловит BUG-801 (в PDF-чеке сумма без скидки)")
    void pdf() {
        BugCatcher.assertCatches(ru.qacourse.m09.tasks.FilesApiTest.class, "BUG-801");
    }
}
