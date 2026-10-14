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
 * BUG-501 · Кухня · Оплаченный заказ никогда не начинают готовить
 * <p>
 * Жалоба пользователя: «Заказ оплачен полчаса назад, а статус так и висит PAID — кухня его не видит.»
 * <br>Где смотреть: POST /api/orders/{id}/pay → статус
 * <p>
 * ЗАДАНИЕ: напишите тест, который ПРОХОДИТ на исправном приложении и ПАДАЕТ, когда баг включён.
 * <ul>
 *   <li>Проверить решение: кнопка в портале или {@code ./mvnw -pl testlab test -Dtest=Bug501Check}</li>
 *   <li>Отладить вручную: запустите приложение (start), включите баг на http://localhost:8081/admin
 *       и запустите этот тест из IDE (зелёный треугольник слева от метода).</li>
 * </ul>
 */
public class Bug501Test {
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
