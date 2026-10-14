package ru.qacourse.m12.checks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.qacourse.support.BugCatcher;
import ru.qacourse.support.ShopConfig;

import java.util.Map;

/** Проверка задания 12.3: QA Burger запускается с платёжным сервисом на WireMock. Не изменяйте. */
class PaymentMockTestCheck {
    private static Map<String, Object> props() {
        return Map.of("shop.payment.url", "http://localhost:" + ShopConfig.paymentMockPort());
    }

    @Test
    @DisplayName("Ловит BUG-503 (заказ «оплачен», хотя платёжка упала)")
    void failedPayment() {
        BugCatcher.assertCatches(ru.qacourse.m12.tasks.PaymentMockTest.class, "BUG-503", props());
    }

    @Test
    @DisplayName("Ловит BUG-502 (повторная оплата списывает деньги второй раз)")
    void doubleCharge() {
        BugCatcher.assertCatches(ru.qacourse.m12.tasks.PaymentMockTest.class, "BUG-502", props());
    }
}
