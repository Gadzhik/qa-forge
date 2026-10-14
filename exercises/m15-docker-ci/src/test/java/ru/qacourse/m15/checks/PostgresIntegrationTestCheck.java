package ru.qacourse.m15.checks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.DockerClientFactory;
import ru.qacourse.support.StudentTests;
import ru.qacourse.support.TestRunResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/** Проверка задания 15.3: нужен запущенный Docker. Не изменяйте. */
class PostgresIntegrationTestCheck {
    @Test
    @DisplayName("Тест поднимает PostgreSQL в Docker, запускает на нём QA Burger и проверяет заказ в базе")
    void passes() {
        if (!DockerClientFactory.instance().isDockerAvailable()) {
            fail("Docker не запущен. Запустите Docker Desktop (или службу docker) и повторите проверку.");
        }
        TestRunResult r = StudentTests.run(ru.qacourse.m15.tasks.PostgresIntegrationTest.class);
        assertThat(r.found()).as("тестов в классе").isPositive();
        assertThat(r.allPassed()).as(r.describe()).isTrue();
    }
}
