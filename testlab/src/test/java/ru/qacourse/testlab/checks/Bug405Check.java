package ru.qacourse.testlab.checks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.qacourse.support.BugCatcher;
import ru.qacourse.testlab.student.Bug405Test;


/** Проверка сценария BUG-405. Не изменяйте этот файл. */
class Bug405Check {
    @Test
    @DisplayName("BUG-405: тест проходит без бага и падает с багом")
    void studentTestCatchesBug() {
        BugCatcher.assertCatches(Bug405Test.class, "BUG-405");
    }
}
