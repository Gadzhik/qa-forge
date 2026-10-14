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

    @Test
    void crud() {
        fail("TODO: напишите тест");
    }
}
