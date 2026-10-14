package ru.qacourse.fw.tests;

import io.qameta.allure.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.qacourse.fw.models.OrderDto;

import static org.assertj.core.api.Assertions.*;
import static ru.qacourse.fw.data.Products.*;

/**
 * Задание 14.5: тесты промокодов на вашем фреймворке (клиенты, модели, BaseTest), с метками Allure.
 * 1) SALE10 даёт ровно 10%: subtotal × 0.9 = total, discount = 10% subtotal (заказ на 2 бургера + кола).
 * 2) Истёкший SUMMER2020 → 400 и code = PROMO_EXPIRED (используйте orders.tryCreate).
 * 3) BURGER20 даёт 20%.
 */
@Epic("Магазин")
@Feature("Промокоды")
@DisplayName("Промокоды")
public class PromoTest extends BaseTest {
    @Test
    @Story("Скидка по промокоду")
    @Issue("BUG-401")
    @DisplayName("SALE10 даёт ровно 10%")
    void sale10() {
        cart.add(CLASSIC_BURGER, 2).add(COLA, 1);
        OrderDto order = orders.create("SALE10");
        assertThat(order.subtotal()).isEqualByComparingTo("678.80");
        assertThat(order.discount()).isEqualByComparingTo("67.88");
        assertThat(order.total()).isEqualByComparingTo("610.92");
    }

    @Test
    @Story("Истёкший промокод")
    @Issue("BUG-402")
    @DisplayName("SUMMER2020 отклоняется: 400 PROMO_EXPIRED")
    void expired() {
        cart.add(COLA, 1);
        orders.tryCreate("SUMMER2020").statusCode(400).body("code", org.hamcrest.Matchers.equalTo("PROMO_EXPIRED"));
    }

    @Test
    @Story("Скидка по промокоду")
    @DisplayName("BURGER20 даёт 20%")
    void burger20() {
        cart.add(FRIES, 1);
        OrderDto order = orders.create("BURGER20");
        assertThat(order.total()).isEqualByComparingTo("103.20");
    }
}
