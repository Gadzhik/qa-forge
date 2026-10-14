package ru.qacourse.testlab.checks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.qacourse.support.BugCatcher;
import ru.qacourse.testlab.student.Bug101Test;


/** Проверка сценария BUG-101. Не изменяйте этот файл. */
class Bug101Check {
    @Test
    @DisplayName("BUG-101: тест проходит без бага и падает с багом")
    void studentTestCatchesBug() {
        BugCatcher.assertCatches(Bug101Test.class, "BUG-101");
    }
}
