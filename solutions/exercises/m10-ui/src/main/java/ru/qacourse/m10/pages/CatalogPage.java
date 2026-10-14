package ru.qacourse.m10.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * Задание 10.9: каталог (главная страница).
 * Карточка товара: [data-testid=product-card] с атрибутом data-product-id;
 * внутри — название [data-testid=product-name] и кнопка [data-testid=add-to-cart].
 */
public class CatalogPage extends BasePage {

    public CatalogPage(WebDriver driver) {
        super(driver);
    }

    public CatalogPage open() {
        open("/");
        return this;
    }

    /** Названия всех товаров на странице. */
    public List<String> productNames() {
        visible("product-list");
        return driver.findElements(testId("product-name")).stream().map(WebElement::getText).toList();
    }

    /** Нажать «В корзину» у товара с указанным id. Страница перезагрузится. */
    public CatalogPage addToCart(long productId) {
        WebElement card = driver.findElement(By.cssSelector("[data-testid=product-card][data-product-id='" + productId + "']"));
        card.findElement(testId("add-to-cart")).click();
        visible("flash-message");
        return this;
    }

    /** Перейти в корзину по ссылке в шапке. */
    public CartPage openCart() {
        visible("nav-cart").click();
        return new CartPage(driver);
    }
}
