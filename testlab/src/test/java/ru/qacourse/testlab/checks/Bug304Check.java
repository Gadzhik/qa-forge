package ru.qacourse.testlab.checks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.qacourse.support.BugCatcher;
import ru.qacourse.testlab.student.Bug304Test;


/** Проверка сценария BUG-304. Не изменяйте этот файл. */
class Bug304Check {
    @Test
    @DisplayName("BUG-304: тест проходит без бага и падает с багом")
    void studentTestCatchesBug() {
        BugCatcher.assertCatches(Bug304Test.class, "BUG-304");
    }
}
