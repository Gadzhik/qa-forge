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
    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    void setUp() {
        driver = Browser.chrome();
        wait = new WebDriverWait(driver, Duration.ofSeconds(5));
    }

    @AfterEach
    void tearDown() {
        driver.quit();
    }

    private String result() {
        return driver.findElement(By.cssSelector("[data-testid=alert-result]")).getText();
    }

    @Test
    void confirmAndPrompt() {
        driver.get(ShopConfig.baseUrl() + "/playground/alerts");

        driver.findElement(By.cssSelector("[data-testid=confirm-btn]")).click();
        Alert confirm = wait.until(ExpectedConditions.alertIsPresent());
        assertThat(confirm.getText()).isEqualTo("Удалить заказ?");
        confirm.dismiss();
        assertThat(result()).isEqualTo("Отменено");

        driver.findElement(By.cssSelector("[data-testid=prompt-btn]")).click();
        Alert prompt = wait.until(ExpectedConditions.alertIsPresent());
        prompt.sendKeys("Анна");
        prompt.accept();
        assertThat(result()).isEqualTo("Привет, Анна!");
    }

    @Test
    void iframe() {
        driver.get(ShopConfig.baseUrl() + "/playground/iframe");
        driver.switchTo().frame(driver.findElement(By.cssSelector("[data-testid=inner-frame]")));
        driver.findElement(By.cssSelector("[data-testid=inner-input]")).sendKeys("бургер");
        driver.findElement(By.cssSelector("[data-testid=inner-btn]")).click();
        assertThat(driver.findElement(By.cssSelector("[data-testid=inner-result]")).getText()).isEqualTo("Вы ввели: бургер");

        driver.switchTo().defaultContent();
        assertThat(driver.findElement(By.cssSelector("[data-testid=outer-text]")).getText()).isEqualTo("Я снаружи");
    }

    @Test
    void newTab() {
        driver.get(ShopConfig.baseUrl() + "/playground/windows");
        String original = driver.getWindowHandle();
        driver.findElement(By.cssSelector("[data-testid=new-tab-link]")).click();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String handle : driver.getWindowHandles()) {
            if (!handle.equals(original)) {
                driver.switchTo().window(handle);
            }
        }
        assertThat(driver.findElement(By.tagName("h1")).getText()).isEqualTo("Формы");
        driver.close();
        driver.switchTo().window(original);
    }

    @Test
    void basicAuth() {
        String url = ShopConfig.baseUrl().replace("http://", "http://admin:admin@") + "/playground/basic-auth";
        driver.get(url);
        assertThat(driver.findElement(By.cssSelector("[data-testid=basic-auth-success]")).isDisplayed()).isTrue();
    }
}
