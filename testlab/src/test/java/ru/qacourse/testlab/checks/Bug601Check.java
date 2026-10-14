package ru.qacourse.testlab.checks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.qacourse.support.BugCatcher;
import ru.qacourse.testlab.student.Bug601Test;


/** Проверка сценария BUG-601. Не изменяйте этот файл. */
class Bug601Check {
    @Test
    @DisplayName("BUG-601: тест проходит без бага и падает с багом")
    void studentTestCatchesBug() {
        BugCatcher.assertCatches(Bug601Test.class, "BUG-601");
    }
}
