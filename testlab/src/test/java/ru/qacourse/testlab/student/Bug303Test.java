package ru.qacourse.testlab.student;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.qacourse.support.ShopConfig;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * BUG-303 · Корзина · Можно положить больше, чем есть на складе
 * <p>
 * Жалоба пользователя: «На складе 10 штук, а в корзину положились 50, и заказ оформился.»
 * <br>Где смотреть: POST /api/cart/items
 * <p>
 * ЗАДАНИЕ: напишите тест, который ПРОХОДИТ на исправном приложении и ПАДАЕТ, когда баг включён.
 * <ul>
 *   <li>Проверить решение: кнопка в портале или {@code ./mvnw -pl testlab test -Dtest=Bug303Check}</li>
 *   <li>Отладить вручную: запустите приложение (start), включите баг на http://localhost:8081/admin
 *       и запустите этот тест из IDE (зелёный треугольник слева от метода).</li>
 * </ul>
 */
public class Bug303Test {
    // Адрес приложения: http://localhost:8081, а при проверке — адрес временного экземпляра
    private final String base = ShopConfig.baseUrl();

    @Test
    @DisplayName("TODO: опишите, что проверяет тест")
    void myTest() {
        // TODO: удалите строку с fail(...) и напишите проверку.
        // Пример запроса: given().baseUri(base).get("/api/products").then().statusCode(200);
        fail("Тест ещё не написан");
    }
}
