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

import org.openqa.selenium.Alert;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Задание 10.6: alert, iframe, вкладки, Basic Auth.
 * 1) /playground/alerts: Confirm → отменить (dismiss) → результат «Отменено»;
 *    Prompt → ввести «Анна» и принять → результат «Привет, Анна!».
 * 2) /playground/iframe: внутри iframe ввести текст в [data-testid=inner-input], нажать OK,
 *    проверить [data-testid=inner-result]; вернуться из фрейма и проверить [data-testid=outer-text].
 * 3) /playground/windows: клик по [data-testid=new-tab-link] открывает новую вкладку с формой — переключиться и проверить заголовок h1 «Формы».
 * 4) Basic Auth: открыть /playground/basic-auth с логином admin / admin в адресе → виден [data-testid=basic-auth-success].
 */
public class AlertsFramesTest {
    @Test
    void alerts() {
        fail("TODO: напишите тест");
    }
}
