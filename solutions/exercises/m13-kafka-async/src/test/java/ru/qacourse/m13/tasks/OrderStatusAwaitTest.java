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
import static org.awaitility.Awaitility.await;

/**
 * Задание 13.1: ожидание асинхронного результата с Awaitility (без Thread.sleep!).
 * 1) Оформить и оплатить заказ (user / user123) → дождаться статуса READY (до 20 с, опрос раз в 500 мс).
 * 2) Дождаться, что в журнале событий GET /api/debug/events?orderId={id} появились по порядку
 *    ORDER_CREATED, ORDER_PAID, ORDER_COOKING, ORDER_READY (поле type).
 */
public class OrderStatusAwaitTest {
    private RequestSpecification user;

    @BeforeEach
    void setUp() {
        String token = given().baseUri(ShopConfig.baseUrl()).contentType(ContentType.JSON)
                .body(Map.of("username", "user", "password", "user123"))
                .post("/api/auth/login").then().statusCode(200).extract().path("token");
        user = given().baseUri(ShopConfig.baseUrl()).contentType(ContentType.JSON).auth().oauth2(token);
    }

    private int paidOrder() {
        given().spec(user).delete("/api/cart").then().statusCode(204);
        given().spec(user).body(Map.of("productId", 12, "quantity", 1)).post("/api/cart/items").then().statusCode(200);
        int id = given().spec(user).body("{}").post("/api/orders").then().statusCode(201).extract().path("id");
        given().spec(user).post("/api/orders/" + id + "/pay").then().statusCode(200);
        return id;
    }

    @Test
    void orderBecomesReady() {
        int id = paidOrder();
        await().atMost(Duration.ofSeconds(20))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    String status = given().spec(user).get("/api/orders/" + id).then().statusCode(200).extract().path("status");
                    assertThat(status).isEqualTo("READY");
                });
    }

    @Test
    void allEventsArePublished() {
        int id = paidOrder();
        await().atMost(Duration.ofSeconds(20))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    List<String> types = given().baseUri(ShopConfig.baseUrl()).queryParam("orderId", id)
                            .get("/api/debug/events").then().statusCode(200).extract().path("type");
                    assertThat(types).containsExactly("ORDER_CREATED", "ORDER_PAID", "ORDER_COOKING", "ORDER_READY");
                });
    }
}
