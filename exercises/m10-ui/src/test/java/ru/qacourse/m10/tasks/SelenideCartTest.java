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
    @Test
    void cart() {
        fail("TODO: напишите тест");
    }
}
