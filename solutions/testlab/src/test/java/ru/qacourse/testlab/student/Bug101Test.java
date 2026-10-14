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

public class Bug101Test {
    private final String base = ShopConfig.baseUrl();

    @Test
    @DisplayName("Пройдя по всем страницам, получаем все товары без потерь")
    void pagesContainAllProducts() {
        int total = given().baseUri(base).get("/api/products?size=100").then().statusCode(200)
                .extract().path("totalElements");
        int collected = 0;
        for (int page = 0; page < 10; page++) {
            List<Object> content = given().baseUri(base).queryParam("page", page).queryParam("size", 5)
                    .get("/api/products").then().statusCode(200).extract().path("content");
            if (content.isEmpty()) break;
            collected += content.size();
        }
        assertThat(collected).isEqualTo(total);
    }
}
