package ru.qacourse.testlab.checks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.qacourse.support.BugCatcher;
import ru.qacourse.testlab.student.Bug502Test;


/** Проверка сценария BUG-502. Не изменяйте этот файл. */
class Bug502Check {
    @Test
    @DisplayName("BUG-502: тест проходит без бага и падает с багом")
    void studentTestCatchesBug() {
        BugCatcher.assertCatches(Bug502Test.class, "BUG-502");
    }
}
