package ru.qacourse.testlab.checks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.qacourse.support.BugCatcher;
import ru.qacourse.testlab.student.Bug902Test;


/** Проверка сценария BUG-902. Не изменяйте этот файл. */
class Bug902Check {
    @Test
    @DisplayName("BUG-902: тест проходит без бага и падает с багом")
    void studentTestCatchesBug() {
        BugCatcher.assertCatches(Bug902Test.class, "BUG-902");
    }
}
