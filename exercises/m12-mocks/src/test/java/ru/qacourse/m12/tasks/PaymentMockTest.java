package ru.qacourse.m12.tasks;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.qacourse.support.ShopConfig;

import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.*;

/**
 * Задание 12.3: WireMock вместо платёжного сервиса QA Burger.
 * QA Burger должен быть запущен с --shop.payment.url=http://localhost:8089 (проверка делает это сама).
 * Платёжный сервис: POST /api/payments, тело {"orderId":1,"amount":99.00,"currency":"RUB"},
 * ответ {"status":"APPROVED"|"DECLINED","transactionId":"..."}.
 *
 * WireMock поднимите на порту ShopConfig.paymentMockPort() (по умолчанию 8089) в @BeforeAll, остановите в @AfterAll,
 * сбрасывайте заглушки и журнал запросов в @BeforeEach (wm.resetAll()).
 * 1) APPROVED → заказ PAID; WireMock получил ровно 1 запрос с orderId этого заказа и currency RUB.
 * 2) Платёжка отвечает 500 → заказ PAYMENT_FAILED.
 * 3) Повторная оплата уже оплаченного заказа → 409, и платёжка вызвана только 1 раз.
 * 4) DECLINED → PAYMENT_FAILED.
 */
public class PaymentMockTest {
    @Test
    void payment() {
        fail("TODO: напишите тест");
    }
}
