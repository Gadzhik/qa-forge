package ru.qacourse.testlab.checks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.qacourse.support.BugCatcher;
import ru.qacourse.testlab.student.Bug802Test;


/** Проверка сценария BUG-802. Не изменяйте этот файл. */
class Bug802Check {
    @Test
    @DisplayName("BUG-802: тест проходит без бага и падает с багом")
    void studentTestCatchesBug() {
        BugCatcher.assertCatches(Bug802Test.class, "BUG-802");
    }
}
