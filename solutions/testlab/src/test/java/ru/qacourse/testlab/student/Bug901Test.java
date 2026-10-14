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

public class Bug901Test {
    private final String base = ShopConfig.baseUrl();

    @Test
    @DisplayName("Цена в GraphQL совпадает с REST")
    void graphqlPriceMatchesRest() {
        String rest = given().baseUri(base).get("/api/products/1").then().extract().jsonPath().getString("price");
        String graphql = given().baseUri(base).contentType(ContentType.JSON)
                .body(Map.of("query", "{ product(id: 1) { price } }"))
                .post("/graphql").then().statusCode(200).extract().jsonPath().getString("data.product.price");
        assertThat(new BigDecimal(graphql)).isEqualByComparingTo(new BigDecimal(rest));
    }
}
