package ru.qacourse.testlab.checks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.qacourse.support.BugCatcher;
import ru.qacourse.testlab.student.Bug703Test;


/** Проверка сценария BUG-703. Не изменяйте этот файл. */
class Bug703Check {
    @Test
    @DisplayName("BUG-703: тест проходит без бага и падает с багом")
    void studentTestCatchesBug() {
        BugCatcher.assertCatches(Bug703Test.class, "BUG-703");
    }
}
