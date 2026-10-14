package ru.qacourse.m10.tasks;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import ru.qacourse.m10.Browser;
import ru.qacourse.support.ShopConfig;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Задание 10.5: явные ожидания (WebDriverWait). Страница: /playground/dynamic. Thread.sleep не использовать!
 * 1) Нажать [data-testid=start-loading] → дождаться видимости [data-testid=loaded] (до 10 c), текст содержит «Готово».
 * 2) Нажать [data-testid=enable-btn] → дождаться, что [data-testid=late-btn] станет кликабельной, кликнуть,
 *    проверить текст «Нажата!».
 * 3) Нажать [data-testid=hide-btn] → дождаться исчезновения [data-testid=vanishing].
 */
public class WaitsTest {
    @Test
    void waits() {
        fail("TODO: напишите тест");
    }
}
