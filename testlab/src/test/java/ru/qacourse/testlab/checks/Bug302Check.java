package ru.qacourse.testlab.checks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.qacourse.support.BugCatcher;
import ru.qacourse.testlab.student.Bug302Test;


/** Проверка сценария BUG-302. Не изменяйте этот файл. */
class Bug302Check {
    @Test
    @DisplayName("BUG-302: тест проходит без бага и падает с багом")
    void studentTestCatchesBug() {
        BugCatcher.assertCatches(Bug302Test.class, "BUG-302");
    }
}
