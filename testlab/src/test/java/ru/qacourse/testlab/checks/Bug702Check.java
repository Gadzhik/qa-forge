package ru.qacourse.testlab.checks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.qacourse.support.BugCatcher;
import ru.qacourse.testlab.student.Bug702Test;


/** Проверка сценария BUG-702. Не изменяйте этот файл. */
class Bug702Check {
    @Test
    @DisplayName("BUG-702: тест проходит без бага и падает с багом")
    void studentTestCatchesBug() {
        BugCatcher.assertCatches(Bug702Test.class, "BUG-702");
    }
}
