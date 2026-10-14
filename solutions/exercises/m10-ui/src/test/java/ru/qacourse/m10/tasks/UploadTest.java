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

import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Задание 10.8: загрузка файла в Selenium.
 * Создайте во временной папке файл order.txt с текстом «Бургер x2», на /playground/upload передайте
 * АБСОЛЮТНЫЙ путь в поле [data-testid=file-input] через sendKeys, нажмите [data-testid=upload-button]
 * и проверьте имя ([data-testid=uploaded-name]) и содержимое ([data-testid=uploaded-preview]).
 */
public class UploadTest {
    private WebDriver driver;

    @BeforeEach
    void setUp() {
        driver = Browser.chrome();
    }

    @AfterEach
    void tearDown() {
        driver.quit();
    }

    @Test
    void uploadFile(@TempDir Path tmp) throws Exception {
        Path file = tmp.resolve("order.txt");
        Files.writeString(file, "Бургер x2", StandardCharsets.UTF_8);

        driver.get(ShopConfig.baseUrl() + "/playground/upload");
        driver.findElement(By.cssSelector("[data-testid=file-input]")).sendKeys(file.toAbsolutePath().toString());
        driver.findElement(By.cssSelector("[data-testid=upload-button]")).click();

        var wait = new org.openqa.selenium.support.ui.WebDriverWait(driver, Duration.ofSeconds(5));
        wait.until(org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[data-testid=uploaded-name]")));
        assertThat(driver.findElement(By.cssSelector("[data-testid=uploaded-name]")).getText()).isEqualTo("order.txt");
        assertThat(driver.findElement(By.cssSelector("[data-testid=uploaded-preview]")).getText()).isEqualTo("Бургер x2");
    }
}
