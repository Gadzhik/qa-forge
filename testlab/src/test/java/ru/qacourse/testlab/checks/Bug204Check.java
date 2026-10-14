package ru.qacourse.testlab.checks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.qacourse.support.BugCatcher;
import ru.qacourse.testlab.student.Bug204Test;


/** Проверка сценария BUG-204. Не изменяйте этот файл. */
class Bug204Check {
    @Test
    @DisplayName("BUG-204: тест проходит без бага и падает с багом")
    void studentTestCatchesBug() {
        BugCatcher.assertCatches(Bug204Test.class, "BUG-204");
    }
}
