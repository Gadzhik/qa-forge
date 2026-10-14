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

public class Bug304Test {
    private final String base = ShopConfig.baseUrl();

    private String login(String username, String password) {
        return given().baseUri(base).contentType(ContentType.JSON)
                .body(Map.of("username", username, "password", password))
                .post("/api/auth/login").then().statusCode(200).extract().path("token");
    }

    @Test
    @DisplayName("Итог корзины = цена × количество")
    void totalUsesQuantity() {
        String price = given().baseUri(base).get("/api/products/12").then().extract().jsonPath().getString("price");
        String total = given().baseUri(base).auth().oauth2(login("user", "user123")).contentType(ContentType.JSON)
                .body(Map.of("productId", 12, "quantity", 3)).post("/api/cart/items")
                .then().statusCode(200).extract().jsonPath().getString("total");
        assertThat(new BigDecimal(total)).isEqualByComparingTo(new BigDecimal(price).multiply(BigDecimal.valueOf(3)));
    }
}
