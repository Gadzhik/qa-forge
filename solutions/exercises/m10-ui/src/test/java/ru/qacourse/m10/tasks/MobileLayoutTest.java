package ru.qacourse.m10.tasks;

import com.codeborne.selenide.Configuration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.qacourse.support.ShopConfig;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.CollectionCondition.*;
import static com.codeborne.selenide.Selenide.*;
import static org.assertj.core.api.Assertions.fail;

/**
 * Задание 10.13: мобильная вёрстка.
 * Откройте каталог в окне 375×812 (размер iPhone): кнопка меню [data-testid=menu-toggle] видна,
 * навигация [data-testid=nav] скрыта; после нажатия на кнопку меню — навигация видна.
 * На обычном экране 1280×900 — наоборот: кнопка меню скрыта, навигация видна.
 * Подсказка: Configuration.browserSize = "375x812" ДО открытия браузера.
 */
public class MobileLayoutTest {
    @BeforeEach
    void setUp() {
        Configuration.baseUrl = ShopConfig.baseUrl();
    }

    @AfterEach
    void tearDown() {
        closeWebDriver();
        Configuration.browserSize = "1366x768";
    }

    @Test
    void phone() {
        Configuration.browserSize = "375x812";
        open("/");
        $("[data-testid=menu-toggle]").shouldBe(visible);
        $("[data-testid=nav]").shouldNotBe(visible);
        $("[data-testid=menu-toggle]").click();
        $("[data-testid=nav]").shouldBe(visible);
    }

    @Test
    void desktop() {
        Configuration.browserSize = "1280x900";
        open("/");
        $("[data-testid=menu-toggle]").shouldNotBe(visible);
        $("[data-testid=nav]").shouldBe(visible);
    }
}
