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

public class Bug303Test {
    private final String base = ShopConfig.baseUrl();

    private String login(String username, String password) {
        return given().baseUri(base).contentType(ContentType.JSON)
                .body(Map.of("username", username, "password", password))
                .post("/api/auth/login").then().statusCode(200).extract().path("token");
    }

    @Test
    @DisplayName("Нельзя положить больше, чем на складе")
    void cannotExceedStock() {
        int stock = given().baseUri(base).get("/api/products/16").then().extract().path("stock");
        given().baseUri(base).auth().oauth2(login("user", "user123")).contentType(ContentType.JSON)
                .body(Map.of("productId", 16, "quantity", stock + 1))
                .post("/api/cart/items").then().statusCode(400);
    }
}
