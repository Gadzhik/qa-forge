package ru.qacourse.m10.checks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.qacourse.support.BugCatcher;

/** Проверка задания. Не изменяйте этот файл. */
class WaitsTestCheck {

    @Test
    @DisplayName("Тесты с ожиданиями проходят и не используют Thread.sleep")
    void passes() throws Exception {
        String src = java.nio.file.Files.readString(java.nio.file.Path.of("src/test/java/ru/qacourse/m10/tasks/WaitsTest.java"));
        org.assertj.core.api.Assertions.assertThat(src).as("не используйте Thread.sleep").doesNotContain("Thread.sleep(");
        BugCatcher.assertPassesOnCleanApp(ru.qacourse.m10.tasks.WaitsTest.class);
    }
}
