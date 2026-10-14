package ru.qacourse.m09.tasks;

import org.junit.jupiter.api.Test;
import ru.qacourse.support.ShopConfig;

import java.util.Comparator;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.*;

/**
 * Задание 9.2: query-параметры и извлечение данных из ответа.
 * 1) GET /api/products?sort=price,desc&size=100 — цены идут по убыванию (извлеките список цен и проверьте AssertJ).
 * 2) GET /api/products?category=DRINK&size=100 — все товары категории DRINK, и их ровно столько, сколько totalElements.
 * Используйте queryParam(...) и extract().
 */
public class CatalogApiTest {

    @Test
    void sortedByPriceDesc() {
        List<Float> prices = given()
                .baseUri(ShopConfig.baseUrl())
                .queryParam("sort", "price,desc")
                .queryParam("size", 100)
                .get("/api/products")
                .then().statusCode(200)
                .extract().path("content.price");

        assertThat(prices).isNotEmpty().isSortedAccordingTo(Comparator.reverseOrder());
    }

    @Test
    void filterByCategory() {
        var json = given()
                .baseUri(ShopConfig.baseUrl())
                .queryParam("category", "DRINK")
                .queryParam("size", 100)
                .get("/api/products")
                .then().statusCode(200)
                .extract().jsonPath();

        List<String> categories = json.getList("content.category");
        assertThat(categories).isNotEmpty().containsOnly("DRINK");
        assertThat(categories).hasSize(json.getInt("totalElements"));
    }
}
