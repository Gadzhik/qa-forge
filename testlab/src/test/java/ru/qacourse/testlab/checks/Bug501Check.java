package ru.qacourse.testlab.checks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.qacourse.support.BugCatcher;
import ru.qacourse.testlab.student.Bug501Test;


/** Проверка сценария BUG-501. Не изменяйте этот файл. */
class Bug501Check {
    @Test
    @DisplayName("BUG-501: тест проходит без бага и падает с багом")
    void studentTestCatchesBug() {
        BugCatcher.assertCatches(Bug501Test.class, "BUG-501");
    }
}
