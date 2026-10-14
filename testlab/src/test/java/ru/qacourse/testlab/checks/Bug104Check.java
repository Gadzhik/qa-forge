package ru.qacourse.testlab.checks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.qacourse.support.BugCatcher;
import ru.qacourse.testlab.student.Bug104Test;


/** Проверка сценария BUG-104. Не изменяйте этот файл. */
class Bug104Check {
    @Test
    @DisplayName("BUG-104: тест проходит без бага и падает с багом")
    void studentTestCatchesBug() {
        BugCatcher.assertCatches(Bug104Test.class, "BUG-104");
    }
}
