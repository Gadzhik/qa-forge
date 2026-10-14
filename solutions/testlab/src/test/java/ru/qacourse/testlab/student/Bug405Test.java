package ru.qacourse.testlab.student;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.qacourse.support.ShopConfig;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class Bug405Test {
    private final String base = ShopConfig.baseUrl();

    private String login(String username, String password) {
        return given().baseUri(base).contentType(ContentType.JSON)
                .body(Map.of("username", username, "password", password))
                .post("/api/auth/login").then().statusCode(200).extract().path("token");
    }

    private int orderOf(String token, int productId, int quantity, String promo) {
        given().baseUri(base).auth().oauth2(token).contentType(ContentType.JSON)
                .body(Map.of("productId", productId, "quantity", quantity)).post("/api/cart/items").then().statusCode(200);
        return given().baseUri(base).auth().oauth2(token).contentType(ContentType.JSON)
                .body(promo == null ? Map.of() : Map.of("promoCode", promo))
                .post("/api/orders").then().statusCode(201).extract().path("id");
    }

    @Test
    @DisplayName("Оплаченный заказ отменить нельзя")
    void paidOrderNotCancellable() {
        String token = login("user", "user123");
        int id = orderOf(token, 12, 1, null);
        String status = given().baseUri(base).auth().oauth2(token).post("/api/orders/" + id + "/pay")
                .then().statusCode(200).extract().path("status");
        assertThat(status).isEqualTo("PAID");
        given().baseUri(base).auth().oauth2(token).post("/api/orders/" + id + "/cancel")
                .then().statusCode(409);
    }
}
