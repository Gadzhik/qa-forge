package ru.qacourse.m16.tasks;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.qacourse.support.ShopConfig;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Эталон задания 16.2. Исправлено:
 * 1) тесты делили одну корзину — теперь корзина очищается перед КАЖДЫМ тестом (@BeforeEach);
 * 2) BigDecimal сравнивался через equals (869.70 ≠ 869.7 из-за масштаба) — теперь isEqualByComparingTo;
 * 3) асинхронный статус проверялся сразу после оплаты — теперь ожидание Awaitility.
 */
public class BrokenOrderTests {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Cart(List<Map<String, Object>> items, int totalQuantity, BigDecimal total) {
    }

    private static RequestSpecification user;

    @BeforeAll
    static void login() {
        String token = given().baseUri(ShopConfig.baseUrl()).contentType(ContentType.JSON)
                .body(Map.of("username", "user", "password", "user123"))
                .post("/api/auth/login").then().statusCode(200).extract().path("token");
        user = given().baseUri(ShopConfig.baseUrl()).contentType(ContentType.JSON).auth().oauth2(token);
    }

    @BeforeEach
    void clearCart() {
        given().spec(user).delete("/api/cart").then().statusCode(204);
    }

    private void add(int productId, int quantity) {
        given().spec(user).body(Map.of("productId", productId, "quantity", quantity))
                .post("/api/cart/items").then().statusCode(200);
    }

    private Cart cart() {
        return given().spec(user).get("/api/cart").then().statusCode(200).extract().as(Cart.class);
    }

    @Test
    void cartTotalCountsQuantity() {
        add(1, 3);                                               // 3 × 289.90
        assertThat(cart().total()).isEqualByComparingTo(new BigDecimal("869.7"));
    }

    @Test
    void onlyColaInCart() {
        add(12, 1);
        assertThat(cart().items()).hasSize(1);
        assertThat(cart().items().get(0).get("name")).isEqualTo("Кола 0.5");
    }

    @Test
    void paidOrderBecomesReady() {
        add(12, 1);
        int id = given().spec(user).body("{}").post("/api/orders").then().statusCode(201).extract().path("id");
        given().spec(user).post("/api/orders/" + id + "/pay").then().statusCode(200);

        await().atMost(Duration.ofSeconds(20)).pollInterval(Duration.ofMillis(500)).untilAsserted(() -> {
            String status = given().spec(user).get("/api/orders/" + id).then().statusCode(200).extract().path("status");
            assertThat(status).isEqualTo("READY");
        });
    }
}
