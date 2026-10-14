package ru.qacourse.testlab.checks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.qacourse.support.BugCatcher;
import ru.qacourse.testlab.student.Bug402Test;


/** Проверка сценария BUG-402. Не изменяйте этот файл. */
class Bug402Check {
    @Test
    @DisplayName("BUG-402: тест проходит без бага и падает с багом")
    void studentTestCatchesBug() {
        BugCatcher.assertCatches(Bug402Test.class, "BUG-402");
    }
}
