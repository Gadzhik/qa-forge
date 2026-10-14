package ru.qacourse.testlab.checks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.qacourse.support.BugCatcher;
import ru.qacourse.testlab.student.Bug403Test;


/** Проверка сценария BUG-403. Не изменяйте этот файл. */
class Bug403Check {
    @Test
    @DisplayName("BUG-403: тест проходит без бага и падает с багом")
    void studentTestCatchesBug() {
        BugCatcher.assertCatches(Bug403Test.class, "BUG-403");
    }
}
