package ru.qacourse.m09.tasks;

import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.qacourse.m09.Specs;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.assertj.core.api.Assertions.fail;

/**
 * Задание 9.5: спецификации запросов.
 * В @BeforeEach получите спецификацию с авторизацией (Specs.auth("user", "user123")) и очистите корзину (DELETE /api/cart).
 * 1) Добавить товар 1 дважды (2 шт. + 1 шт.) → в корзине items[0].quantity = 3, totalQuantity = 3.
 * 2) Добавить 3 шт. товара 12 (Кола 0.5, 99.00) → total = 297.0.
 */
public class CartApiTest {

    @Test
    void cart() {
        fail("TODO: напишите тест");
    }
}
