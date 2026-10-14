package ru.qacourse.testlab.checks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.qacourse.support.BugCatcher;
import ru.qacourse.testlab.student.Bug503Test;
import java.util.Map;

/** Проверка сценария BUG-503. Не изменяйте этот файл. */
class Bug503Check {
    @Test
    @DisplayName("BUG-503: тест проходит без бага и падает с багом")
    void studentTestCatchesBug() {
        BugCatcher.assertCatches(Bug503Test.class, "BUG-503", Map.of("shop.payment.url", "http://localhost:1"));
    }
}
