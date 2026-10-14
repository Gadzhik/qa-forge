package ru.qacourse.testlab.checks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.qacourse.support.BugCatcher;
import ru.qacourse.testlab.student.Bug303Test;


/** Проверка сценария BUG-303. Не изменяйте этот файл. */
class Bug303Check {
    @Test
    @DisplayName("BUG-303: тест проходит без бага и падает с багом")
    void studentTestCatchesBug() {
        BugCatcher.assertCatches(Bug303Test.class, "BUG-303");
    }
}
