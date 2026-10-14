package ru.qacourse.m07.checks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.qacourse.m07.sut.Mutant;
import ru.qacourse.support.StudentTests;
import ru.qacourse.support.TestRunResult;

import static org.assertj.core.api.Assertions.*;

/** Проверка задания. Не изменяйте этот файл. */
class RandomCustomerResolverCheck {

    @Test
    @DisplayName("Расширение подставляет уникального TestCustomer в параметр теста")
    void resolves() {
        ResolverSample.SEEN.clear();
        TestRunResult r = StudentTests.run(ResolverSample.class);
        assertThat(r.allPassed()).as(r.describe()).isTrue();
    }
}
