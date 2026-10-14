package ru.qacourse.fw.tests;

import io.qameta.allure.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.qacourse.fw.models.OrderDto;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.qacourse.fw.data.Products.*;

/**
 * Задание 14.2: пометьте класс Allure-метками @Epic("Магазин"), @Feature("Заказы"),
 * а тест — @Story("Оформление заказа"), @Severity(SeverityLevel.CRITICAL), @TmsLink("TC-ORDER-1").
 */
@Epic("Магазин")
@Feature("Заказы")
@DisplayName("Заказы")
public class OrderFlowTest extends BaseTest {

    @Test
    @Story("Оформление заказа")
    @Severity(SeverityLevel.CRITICAL)
    @TmsLink("TC-ORDER-1")
    @DisplayName("Заказ из корзины: сумма = цена × количество, статус NEW")
    void orderFromCart() {
        cart.add(CLASSIC_BURGER, 2);
        OrderDto order = orders.create(null);
        assertThat(order.status()).isEqualTo("NEW");
        assertThat(order.total()).isEqualByComparingTo("579.80");
        assertThat(cart.get().items()).isEmpty();
    }
}
