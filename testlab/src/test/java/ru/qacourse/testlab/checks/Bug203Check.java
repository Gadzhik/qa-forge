package ru.qacourse.testlab.checks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.qacourse.support.BugCatcher;
import ru.qacourse.testlab.student.Bug203Test;


/** Проверка сценария BUG-203. Не изменяйте этот файл. */
class Bug203Check {
    @Test
    @DisplayName("BUG-203: тест проходит без бага и падает с багом")
    void studentTestCatchesBug() {
        BugCatcher.assertCatches(Bug203Test.class, "BUG-203");
    }
}
