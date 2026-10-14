package ru.qacourse.m16.checks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.qacourse.support.BugCatcher;
import ru.qacourse.support.StudentTests;
import ru.qacourse.support.TestRunResult;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/** Проверка задания 16.2. Не изменяйте. */
class BrokenOrderTestsCheck {
    private static final Class<?> TESTS = ru.qacourse.m16.tasks.BrokenOrderTests.class;

    @Test
    @DisplayName("Тесты стабильно проходят на исправном приложении (3 прогона подряд), без Thread.sleep")
    void stable() throws Exception {
        String src = Files.readString(Path.of("src/test/java/ru/qacourse/m16/tasks/BrokenOrderTests.java"));
        assertThat(src).as("ожидания — через Awaitility, не Thread.sleep").doesNotContain("Thread.sleep(");
        for (int i = 1; i <= 3; i++) {
            TestRunResult r = StudentTests.runAgainstShop(TESTS, "");
            assertThat(r.found()).as("тестов должно остаться 3").isEqualTo(3);
            assertThat(r.allPassed()).as("прогон №" + i + ": " + r.describe()).isTrue();
        }
    }

    @Test
    @DisplayName("Проверки не ослаблены: тесты ловят BUG-304 (итог корзины) и BUG-501 (кухня)")
    void stillCatchBugs() {
        BugCatcher.assertCatches(TESTS, "BUG-304");
        BugCatcher.assertCatches(TESTS, "BUG-501");
    }
}
