package ru.qacourse.m09.tasks;

import org.junit.jupiter.api.Test;
import ru.qacourse.m09.Specs;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.assertj.core.api.Assertions.fail;

/**
 * Задание 9.4: полный цикл CRUD под администратором (admin / admin123) и проверка прав.
 * 1) POST /api/products (тело — HashMap) → 201, получить id;
 *    PUT /api/products/{id} с новой ценой → 200, цена изменилась;
 *    DELETE /api/products/{id} → 204; GET /api/products/{id} → 404.
 * 2) Обычный пользователь (user / user123): POST /api/products → 403.
 * Для авторизации используйте готовый Specs.auth(...) (разбор — урок 9.5).
 */
public class AdminProductsApiTest {

    private Map<String, Object> product(double price) {
        Map<String, Object> body = new HashMap<>();
        body.put("name", "Тестовый бургер");
        body.put("description", "Создан автотестом");
        body.put("price", price);
        body.put("category", "BURGER");
        body.put("stock", 5);
        return body;
    }

    @Test
    void adminCrud() {
        var admin = Specs.auth("admin", "admin123");

        int id = given(admin).body(product(100.0))
                .post("/api/products")
                .then().statusCode(201).body("name", equalTo("Тестовый бургер"))
                .extract().path("id");

        given(admin).body(product(150.5))
                .put("/api/products/" + id)
                .then().statusCode(200).body("price", equalTo(150.5f));

        given(admin).delete("/api/products/" + id).then().statusCode(204);
        given(admin).get("/api/products/" + id).then().statusCode(404);
    }

    @Test
    void userCannotCreate() {
        given(Specs.auth("user", "user123")).body(product(100.0))
                .post("/api/products")
                .then().statusCode(403);
    }
}
