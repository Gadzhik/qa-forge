package ru.qacourse.testlab.student;

import com.codeborne.selenide.Configuration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.qacourse.support.ShopConfig;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;
import static org.assertj.core.api.Assertions.fail;

/**
 * BUG-703 · Интерфейс · Кнопка «В корзину» не добавляет товар со страницы товара
 * <p>
 * Жалоба пользователя: «На странице товара жму «В корзину», появляется «Добавлено», но корзина пустая.»
 * <br>Где смотреть: UI: /product/{id}
 * <p>
 * ЗАДАНИЕ: напишите тест, который ПРОХОДИТ на исправном приложении и ПАДАЕТ, когда баг включён.
 * <ul>
 *   <li>Проверить решение: кнопка в портале или {@code ./mvnw -pl testlab test -Dtest=Bug703Check}</li>
 *   <li>Отладить вручную: запустите приложение (start), включите баг на http://localhost:8081/admin
 *       и запустите этот тест из IDE (зелёный треугольник слева от метода).</li>
 * </ul>
 */
public class Bug703Test {
    private final String base = ShopConfig.baseUrl();

    @BeforeEach
    void setUp() {
        Configuration.baseUrl = base; // open("/login") откроет base + "/login"
    }

    @AfterEach
    void tearDown() {
        closeWebDriver(); // закрываем браузер после каждого теста
    }

    @Test
    @DisplayName("TODO: опишите, что проверяет тест")
    void myTest() {
        // TODO: удалите строку с fail(...) и напишите проверку.
        // Пример: open("/"); $("[data-testid=logo]").shouldBe(visible);
        fail("Тест ещё не написан");
    }
}
