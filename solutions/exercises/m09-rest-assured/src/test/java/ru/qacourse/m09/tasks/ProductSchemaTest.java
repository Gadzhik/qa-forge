package ru.qacourse.m09.tasks;

import org.junit.jupiter.api.Test;
import ru.qacourse.support.ShopConfig;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.fail;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

/**
 * Задание 9.7: проверка контракта JSON-схемой.
 * 1) Ответ GET /api/products/1 соответствует схеме src/test/resources/schemas/product.json.
 * 2) Каждый элемент content в GET /api/products?size=100 — тоже (подсказка: схема элемента та же,
 *    проверьте через цикл по id, или напишите схему страницы — по желанию).
 */
public class ProductSchemaTest {

    @Test
    void productMatchesSchema() {
        given().baseUri(ShopConfig.baseUrl())
                .get("/api/products/1")
                .then().statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/product.json"));
    }

    @Test
    void everyProductMatchesSchema() {
        java.util.List<Integer> ids = given().baseUri(ShopConfig.baseUrl())
                .queryParam("size", 100).get("/api/products")
                .then().statusCode(200).extract().path("content.id");
        for (int id : ids) {
            given().baseUri(ShopConfig.baseUrl())
                    .get("/api/products/" + id)
                    .then().body(matchesJsonSchemaInClasspath("schemas/product.json"));
        }
    }
}
