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

public class Bug105Test {
    private final String base = ShopConfig.baseUrl();

    @Test
    @DisplayName("Фильтр category=DRINK возвращает только напитки")
    void categoryFilter() {
        List<String> categories = given().baseUri(base).queryParam("category", "DRINK").queryParam("size", 100)
                .get("/api/products").then().statusCode(200).extract().path("content.category");
        assertThat(categories).isNotEmpty().containsOnly("DRINK");
    }
}
