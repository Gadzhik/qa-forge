package ru.qacourse.testlab.checks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.qacourse.support.BugCatcher;
import ru.qacourse.testlab.student.Bug301Test;


/** Проверка сценария BUG-301. Не изменяйте этот файл. */
class Bug301Check {
    @Test
    @DisplayName("BUG-301: тест проходит без бага и падает с багом")
    void studentTestCatchesBug() {
        BugCatcher.assertCatches(Bug301Test.class, "BUG-301");
    }
}
