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

public class Bug102Test {
    private final String base = ShopConfig.baseUrl();

    @Test
    @DisplayName("sort=price,desc возвращает товары от дорогих к дешёвым")
    void sortByPriceDesc() {
        List<Float> prices = given().baseUri(base).queryParam("sort", "price,desc").queryParam("size", 100)
                .get("/api/products").then().statusCode(200).extract().path("content.price");
        assertThat(prices).isNotEmpty();
        assertThat(prices).isSortedAccordingTo((a, b) -> Float.compare(b, a));
    }
}
