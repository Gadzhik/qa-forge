package ru.qacourse.testlab.checks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.qacourse.support.BugCatcher;
import ru.qacourse.testlab.student.Bug801Test;


/** Проверка сценария BUG-801. Не изменяйте этот файл. */
class Bug801Check {
    @Test
    @DisplayName("BUG-801: тест проходит без бага и падает с багом")
    void studentTestCatchesBug() {
        BugCatcher.assertCatches(Bug801Test.class, "BUG-801");
    }
}
