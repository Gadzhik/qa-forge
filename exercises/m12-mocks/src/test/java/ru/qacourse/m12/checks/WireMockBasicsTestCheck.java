package ru.qacourse.m12.checks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.qacourse.support.StudentTests;
import ru.qacourse.support.TestRunResult;

import static org.assertj.core.api.Assertions.assertThat;

/** Проверка задания 12.2. Не изменяйте. */
class WireMockBasicsTestCheck {
    @Test
    @DisplayName("Тесты WireMock проходят (минимум 3)")
    void passes() {
        TestRunResult r = StudentTests.run(ru.qacourse.m12.tasks.WireMockBasicsTest.class);
        assertThat(r.found()).as("нужно минимум 3 теста").isGreaterThanOrEqualTo(3);
        assertThat(r.allPassed()).as(r.describe()).isTrue();
    }
}
