package ru.qacourse.testlab.checks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.qacourse.support.BugCatcher;
import ru.qacourse.testlab.student.Bug201Test;


/** Проверка сценария BUG-201. Не изменяйте этот файл. */
class Bug201Check {
    @Test
    @DisplayName("BUG-201: тест проходит без бага и падает с багом")
    void studentTestCatchesBug() {
        BugCatcher.assertCatches(Bug201Test.class, "BUG-201");
    }
}
