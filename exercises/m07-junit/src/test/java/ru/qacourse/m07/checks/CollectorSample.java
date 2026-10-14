package ru.qacourse.m07.checks;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import ru.qacourse.m07.ext.FailedTestsCollector;

import static org.assertj.core.api.Assertions.assertThat;

/** Тест-образец: один тест проходит, два падают. Не изменяйте. */
@ExtendWith(FailedTestsCollector.class)
class CollectorSample {
    @Test
    void passes() {
        assertThat(1).isEqualTo(1);
    }

    @Test
    void failsOne() {
        assertThat(1).isEqualTo(2);
    }

    @Test
    void failsTwo() {
        throw new IllegalStateException("бум");
    }
}
