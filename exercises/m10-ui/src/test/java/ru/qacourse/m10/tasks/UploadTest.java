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
    @Test
    void upload() {
        fail("TODO: напишите тест");
    }
}
