package ru.qacourse.testlab.checks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.qacourse.support.BugCatcher;
import ru.qacourse.testlab.student.Bug901Test;


/** Проверка сценария BUG-901. Не изменяйте этот файл. */
class Bug901Check {
    @Test
    @DisplayName("BUG-901: тест проходит без бага и падает с багом")
    void studentTestCatchesBug() {
        BugCatcher.assertCatches(Bug901Test.class, "BUG-901");
    }
}
