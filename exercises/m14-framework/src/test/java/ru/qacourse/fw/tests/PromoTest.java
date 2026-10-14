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
    void promo() {
        fail("TODO: напишите тесты");
    }
}
