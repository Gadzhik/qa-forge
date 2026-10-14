package ru.qacourse.testlab.checks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.qacourse.support.BugCatcher;
import ru.qacourse.testlab.student.Bug202Test;


/** Проверка сценария BUG-202. Не изменяйте этот файл. */
class Bug202Check {
    @Test
    @DisplayName("BUG-202: тест проходит без бага и падает с багом")
    void studentTestCatchesBug() {
        BugCatcher.assertCatches(Bug202Test.class, "BUG-202");
    }
}
