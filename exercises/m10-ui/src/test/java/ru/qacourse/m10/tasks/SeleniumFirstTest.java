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
 * Задание 10.4: первый UI-тест на Selenium.
 * Откройте каталог, введите в поиск «кола» (поле [data-testid=search-input]), нажмите «Найти»
 * ([data-testid=search-button]) и проверьте, что найден хотя бы один товар и название первого содержит «Кола».
 * Браузер создавайте через Browser.chrome() в @BeforeEach и закрывайте driver.quit() в @AfterEach.
 */
public class SeleniumFirstTest {
    @Test
    void search() {
        fail("TODO: напишите тест");
    }
}
