package ru.qacourse.m13.tasks;

import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.qacourse.support.ShopConfig;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.*;

/**
 * Задание 13.1: ожидание асинхронного результата с Awaitility (без Thread.sleep!).
 * 1) Оформить и оплатить заказ (user / user123) → дождаться статуса READY (до 20 с, опрос раз в 500 мс).
 * 2) Дождаться, что в журнале событий GET /api/debug/events?orderId={id} появились по порядку
 *    ORDER_CREATED, ORDER_PAID, ORDER_COOKING, ORDER_READY (поле type).
 */
public class OrderStatusAwaitTest {
    @Test
    void await() {
        fail("TODO: напишите тест");
    }
}
