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

    private RequestSpecification user;

    @BeforeEach
    void setUp() {
        user = Specs.auth("user", "user123");
        given(user).delete("/api/cart").then().statusCode(204);
    }

    private void add(int productId, int quantity) {
        given(user).body(Map.of("productId", productId, "quantity", quantity))
                .post("/api/cart/items")
                .then().statusCode(200);
    }

    @Test
    void sameProductIsSummed() {
        add(1, 2);
        add(1, 1);
        given(user).get("/api/cart")
                .then().spec(Specs.ok200())
                .body("items", hasSize(1))
                .body("items[0].quantity", equalTo(3))
                .body("totalQuantity", equalTo(3));
    }

    @Test
    void totalCountsQuantity() {
        add(12, 3);
        given(user).get("/api/cart")
                .then().spec(Specs.ok200())
                .body("total", equalTo(297.0f));
    }
}
