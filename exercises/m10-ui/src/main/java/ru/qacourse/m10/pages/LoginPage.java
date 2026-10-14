package ru.qacourse.m10.pages;

import org.openqa.selenium.WebDriver;

/**
 * Задание 10.9: страница входа /login.
 * Поля: [data-testid=login-username], [data-testid=login-password], кнопка [data-testid=login-submit].
 */
public class LoginPage extends BasePage {

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    /** Открыть страницу входа. */
    public LoginPage open() {
        throw new UnsupportedOperationException("TODO");
    }

    /** Ввести логин и пароль, нажать «Войти». После входа открывается каталог. */
    public CatalogPage loginAs(String username, String password) {
        throw new UnsupportedOperationException("TODO");
    }
}
