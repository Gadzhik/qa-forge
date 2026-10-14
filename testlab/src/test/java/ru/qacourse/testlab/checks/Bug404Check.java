package ru.qacourse.testlab.checks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.qacourse.support.BugCatcher;
import ru.qacourse.testlab.student.Bug404Test;


/** Проверка сценария BUG-404. Не изменяйте этот файл. */
class Bug404Check {
    @Test
    @DisplayName("BUG-404: тест проходит без бага и падает с багом")
    void studentTestCatchesBug() {
        BugCatcher.assertCatches(Bug404Test.class, "BUG-404");
    }
}
