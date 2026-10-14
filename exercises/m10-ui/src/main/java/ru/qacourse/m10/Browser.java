package ru.qacourse.m10;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

/**
 * Создание браузера для уроков про Selenium (10.4–10.8). Разбор — урок 10.4.
 * Без окна (headless) — если задано системное свойство headless=true (так запускают проверки и CI).
 */
public final class Browser {
    private Browser() {
    }

    public static WebDriver chrome() {
        ChromeOptions options = new ChromeOptions();
        if (Boolean.getBoolean("headless")) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--window-size=1280,900");
        options.addArguments("--lang=ru");
        return new ChromeDriver(options);
    }
}
