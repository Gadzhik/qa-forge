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
    @Test
    void mobile() {
        fail("TODO: напишите тест");
    }
}
