package ru.qacourse.testlab.checks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.qacourse.support.BugCatcher;
import ru.qacourse.testlab.student.Bug105Test;


/** Проверка сценария BUG-105. Не изменяйте этот файл. */
class Bug105Check {
    @Test
    @DisplayName("BUG-105: тест проходит без бага и падает с багом")
    void studentTestCatchesBug() {
        BugCatcher.assertCatches(Bug105Test.class, "BUG-105");
    }
}
