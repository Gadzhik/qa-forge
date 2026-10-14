package ru.qacourse.testlab.checks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.qacourse.support.BugCatcher;
import ru.qacourse.testlab.student.Bug102Test;


/** Проверка сценария BUG-102. Не изменяйте этот файл. */
class Bug102Check {
    @Test
    @DisplayName("BUG-102: тест проходит без бага и падает с багом")
    void studentTestCatchesBug() {
        BugCatcher.assertCatches(Bug102Test.class, "BUG-102");
    }
}
