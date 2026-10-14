package ru.qacourse.m10.pages;

import org.openqa.selenium.WebDriver;

/**
 * Задание 10.9: корзина /cart.
 * Итог: [data-testid=cart-total] (текст вида «579.80 ₽»), строки: [data-testid=cart-row].
 */
public class CartPage extends BasePage {

    public CartPage(WebDriver driver) {
        super(driver);
    }

    /** Количество строк в корзине. */
    public int rowsCount() {
        visible("cart-total");
        return driver.findElements(testId("cart-row")).size();
    }

    /** Итог без «₽» и пробелов: «579.80». */
    public String total() {
        return visible("cart-total").getText().replace("₽", "").trim();
    }
}
