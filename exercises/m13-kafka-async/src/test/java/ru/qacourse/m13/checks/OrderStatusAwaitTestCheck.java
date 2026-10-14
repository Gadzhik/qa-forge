package ru.qacourse.m13.checks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.qacourse.support.BugCatcher;

import static org.assertj.core.api.Assertions.assertThat;

/** Проверка задания 13.1. Не изменяйте. */
class OrderStatusAwaitTestCheck {
    @Test
    @DisplayName("Ожидание без Thread.sleep и ловит BUG-501 (заказ не начинают готовить)")
    void catches() throws Exception {
        String src = java.nio.file.Files.readString(java.nio.file.Path.of("src/test/java/ru/qacourse/m13/tasks/OrderStatusAwaitTest.java"));
        assertThat(src).as("используйте Awaitility, а не Thread.sleep").doesNotContain("Thread.sleep(").contains("await()");
        BugCatcher.assertCatches(ru.qacourse.m13.tasks.OrderStatusAwaitTest.class, "BUG-501");
    }
}
