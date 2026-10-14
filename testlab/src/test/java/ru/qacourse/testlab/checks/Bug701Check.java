package ru.qacourse.testlab.checks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.qacourse.support.BugCatcher;
import ru.qacourse.testlab.student.Bug701Test;


/** Проверка сценария BUG-701. Не изменяйте этот файл. */
class Bug701Check {
    @Test
    @DisplayName("BUG-701: тест проходит без бага и падает с багом")
    void studentTestCatchesBug() {
        BugCatcher.assertCatches(Bug701Test.class, "BUG-701");
    }
}
