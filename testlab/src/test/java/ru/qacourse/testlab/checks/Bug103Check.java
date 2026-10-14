package ru.qacourse.testlab.checks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.qacourse.support.BugCatcher;
import ru.qacourse.testlab.student.Bug103Test;


/** Проверка сценария BUG-103. Не изменяйте этот файл. */
class Bug103Check {
    @Test
    @DisplayName("BUG-103: тест проходит без бага и падает с багом")
    void studentTestCatchesBug() {
        BugCatcher.assertCatches(Bug103Test.class, "BUG-103");
    }
}
