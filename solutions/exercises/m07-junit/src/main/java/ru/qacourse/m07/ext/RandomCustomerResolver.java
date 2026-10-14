package ru.qacourse.m07.ext;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import java.util.UUID;

/**
 * Задание 7.6: своё расширение JUnit 5 — подставляет в тест параметр типа TestCustomer:
 *
 *   @ExtendWith(RandomCustomerResolver.class)
 *   class MyTest {
 *       @Test void test(TestCustomer customer) { ... }
 *   }
 *
 * Каждый вызов — новый уникальный покупатель: username "user_" + 8 случайных символов,
 * email username + "@test.local", пароль "Secret1".
 */
public class RandomCustomerResolver implements ParameterResolver {

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return parameterContext.getParameter().getType() == TestCustomer.class;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        String username = "user_" + UUID.randomUUID().toString().substring(0, 8);
        return new TestCustomer(username, username + "@test.local", "Secret1");
    }
}
