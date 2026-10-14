package ru.qacourse.m09.tasks;

import org.junit.jupiter.api.Test;
import ru.qacourse.support.ShopConfig;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.fail;

/**
 * Задание 9.7: проверка контракта JSON-схемой.
 * 1) Ответ GET /api/products/1 соответствует схеме src/test/resources/schemas/product.json.
 * 2) Каждый элемент content в GET /api/products?size=100 — тоже (подсказка: схема элемента та же,
 *    проверьте через цикл по id, или напишите схему страницы — по желанию).
 */
public class ProductSchemaTest {

    @Test
    void schema() {
        fail("TODO: напишите тест");
    }
}
