package ru.qacourse.m07.checks;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import ru.qacourse.m07.ext.RandomCustomerResolver;
import ru.qacourse.m07.ext.TestCustomer;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/** Тест-образец, который использует ваше расширение. Не изменяйте. */
@ExtendWith(RandomCustomerResolver.class)
class ResolverSample {
    static final Set<String> SEEN = new HashSet<>();

    @Test
    void first(TestCustomer c) {
        check(c);
    }

    @Test
    void second(TestCustomer c) {
        check(c);
    }

    private static void check(TestCustomer c) {
        assertThat(c).isNotNull();
        assertThat(c.username()).matches("user_[a-z0-9]{8}");
        assertThat(c.email()).isEqualTo(c.username() + "@test.local");
        assertThat(c.password()).isEqualTo("Secret1");
        assertThat(SEEN.add(c.username())).as("покупатели должны быть уникальными").isTrue();
    }
}
