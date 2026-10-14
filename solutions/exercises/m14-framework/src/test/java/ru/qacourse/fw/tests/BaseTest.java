package ru.qacourse.fw.tests;

import org.junit.jupiter.api.BeforeEach;
import ru.qacourse.fw.api.AuthClient;
import ru.qacourse.fw.api.CartClient;
import ru.qacourse.fw.api.OrderClient;
import ru.qacourse.fw.config.Config;

/** Базовый класс: перед каждым тестом — вход и пустая корзина. (Готово.) */
public abstract class BaseTest {
    protected CartClient cart;
    protected OrderClient orders;

    @BeforeEach
    void loginAndClearCart() {
        String token = new AuthClient().login(Config.get().userLogin(), Config.get().userPassword());
        cart = new CartClient(token).clear();
        orders = new OrderClient(token);
    }
}
