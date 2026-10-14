package ru.qacourse.m10.tasks;

import com.codeborne.selenide.Configuration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.qacourse.support.ShopConfig;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.CollectionCondition.*;
import static com.codeborne.selenide.Selenide.*;
import static org.assertj.core.api.Assertions.fail;

/**
 * Задание 10.10: первый тест на Selenide.
 * Войти через форму как user/user123, открыть товар /product/1, указать количество 3 ([data-testid=quantity-input]),
 * нажать «В корзину» ([data-testid=add-to-cart]), дождаться уведомления [data-testid=toast],
 * затем проверить, что счётчик в шапке [data-testid=cart-count] показывает 3,
 * и что в корзине (/cart) одна строка с количеством 3.
 */
public class SelenideCartTest {
    @BeforeEach
    void setUp() {
        Configuration.baseUrl = ShopConfig.baseUrl();
        open("/login");
        $("[data-testid=login-username]").setValue("user");
        $("[data-testid=login-password]").setValue("user123");
        $("[data-testid=login-submit]").click();
        $("[data-testid=user-name]").shouldBe(visible);
    }

    @AfterEach
    void tearDown() {
        closeWebDriver();
    }

    @Test
    void addFromProductPage() {
        open("/product/1");
        $("[data-testid=quantity-input]").setValue("3");
        $("[data-testid=add-to-cart]").click();
        $("[data-testid=toast]").shouldBe(visible).shouldHave(text("Добавлено"));

        refresh();
        $("[data-testid=cart-count]").shouldHave(exactText("3"));

        open("/cart");
        $$("[data-testid=cart-row]").shouldHave(size(1));
        $("[data-testid=cart-item-quantity]").shouldHave(exactText("3"));
    }
}
