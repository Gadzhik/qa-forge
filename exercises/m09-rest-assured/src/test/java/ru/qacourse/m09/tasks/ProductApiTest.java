package ru.qacourse.m09.tasks;

import org.junit.jupiter.api.Test;
import ru.qacourse.support.ShopConfig;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.assertj.core.api.Assertions.fail;

/**
 * Задание 9.1: первые API-тесты.
 * 1) GET /api/products/1 → 200, name = «Классический бургер», category = BURGER, price > 0.
 * 2) GET /api/products/999999 → 404, в теле code = NOT_FOUND.
 */
public class ProductApiTest {

    @Test
    void getProduct() {
        fail("TODO: напишите тест");
    }
}
