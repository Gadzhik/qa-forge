package ru.qacourse.fw.checks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.qacourse.support.BugCatcher;

/** Проверка задания 14.5. Не изменяйте. */
@org.junit.jupiter.api.parallel.Isolated
class PromoTestCheck {
    @Test
    @DisplayName("Ловит BUG-401 (двойная скидка)")
    void doubleDiscount() {
        BugCatcher.assertCatches(ru.qacourse.fw.tests.PromoTest.class, "BUG-401");
    }

    @Test
    @DisplayName("Ловит BUG-402 (истёкший промокод)")
    void expired() {
        BugCatcher.assertCatches(ru.qacourse.fw.tests.PromoTest.class, "BUG-402");
    }
}
