package ru.qacourse.m09.tasks;

import org.junit.jupiter.api.Test;
import ru.qacourse.m09.Specs;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.fail;

/**
 * Задание 9.6: авторизация.
 * 1) GET /api/orders без токена → 401; с испорченным токеном (токен + "x") → 401.
 * 2) admin создаёт заказ (корзина → POST /api/orders), а user пытается открыть его по id → 404.
 */
public class OrdersAuthApiTest {

    @Test
    void auth() {
        fail("TODO: напишите тест");
    }
}
