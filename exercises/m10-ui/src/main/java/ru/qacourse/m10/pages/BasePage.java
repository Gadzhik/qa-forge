package ru.qacourse.m10.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import ru.qacourse.support.ShopConfig;

import java.time.Duration;

/** Общее для всех страниц: драйвер, ожидания, поиск по data-testid, шапка сайта. (Готово, менять не нужно.) */
public abstract class BasePage {
    protected final WebDriver driver;
    protected final WebDriverWait wait;

    protected BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(5));
    }

    protected By testId(String id) {
        return By.cssSelector("[data-testid=" + id + "]");
    }

    protected WebElement visible(String id) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(testId(id)));
    }

    protected void open(String path) {
        driver.get(ShopConfig.baseUrl() + path);
    }

    /** Текст счётчика корзины в шапке. */
    public String cartCount() {
        return visible("cart-count").getText();
    }

    /** Имя пользователя в шапке. */
    public String userName() {
        return visible("user-name").getText();
    }
}
