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
    void withoutTokenIs401() {
        given(Specs.base()).get("/api/orders").then().statusCode(401);
    }

    @Test
    void brokenTokenIs401() {
        String broken = Specs.token("user", "user123") + "x";
        given(Specs.base()).header("Authorization", "Bearer " + broken)
                .get("/api/orders").then().statusCode(401);
    }

    @Test
    void otherUsersOrderIsHidden() {
        var admin = Specs.auth("admin", "admin123");
        given(admin).body(Map.of("productId", 12, "quantity", 1)).post("/api/cart/items").then().statusCode(200);
        int orderId = given(admin).body("{}").post("/api/orders").then().statusCode(201).extract().path("id");

        given(Specs.auth("user", "user123")).get("/api/orders/" + orderId).then().statusCode(404);
    }
}
