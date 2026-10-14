package ru.qacourse.testlab.checks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.qacourse.support.BugCatcher;
import ru.qacourse.testlab.student.Bug602Test;


/** Проверка сценария BUG-602. Не изменяйте этот файл. */
class Bug602Check {
    @Test
    @DisplayName("BUG-602: тест проходит без бага и падает с багом")
    void studentTestCatchesBug() {
        BugCatcher.assertCatches(Bug602Test.class, "BUG-602");
    }
}
