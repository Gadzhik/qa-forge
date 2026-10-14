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
    static WireMockServer wm;
    private RequestSpecification user;

    @BeforeAll
    static void startMock() {
        wm = new WireMockServer(wireMockConfig().port(ShopConfig.paymentMockPort()));
        wm.start();
    }

    @AfterAll
    static void stopMock() {
        wm.stop();
    }

    @BeforeEach
    void setUp() {
        wm.resetAll();
        String token = given().baseUri(ShopConfig.baseUrl()).contentType(ContentType.JSON)
                .body(Map.of("username", "user", "password", "user123"))
                .post("/api/auth/login").then().statusCode(200).extract().path("token");
        user = given().baseUri(ShopConfig.baseUrl()).contentType(ContentType.JSON).auth().oauth2(token);
    }

    private int newOrder() {
        given().spec(user).delete("/api/cart").then().statusCode(204);
        given().spec(user).body(Map.of("productId", 12, "quantity", 1)).post("/api/cart/items").then().statusCode(200);
        return given().spec(user).body("{}").post("/api/orders").then().statusCode(201).extract().path("id");
    }

    private void paymentReturns(String status) {
        wm.stubFor(post("/api/payments").willReturn(okJson("{\"status\":\"" + status + "\",\"transactionId\":\"tx-1\"}")));
    }

    private String pay(int id) {
        return given().spec(user).post("/api/orders/" + id + "/pay").then().statusCode(200).extract().path("status");
    }

    @Test
    void approvedPaymentMarksOrderPaid() {
        paymentReturns("APPROVED");
        int id = newOrder();
        assertThat(pay(id)).isEqualTo("PAID");
        wm.verify(1, postRequestedFor(urlEqualTo("/api/payments"))
                .withRequestBody(matchingJsonPath("$.orderId", equalTo(String.valueOf(id))))
                .withRequestBody(matchingJsonPath("$.currency", equalTo("RUB"))));
    }

    @Test
    void paymentServiceErrorMarksFailed() {
        wm.stubFor(post("/api/payments").willReturn(serverError()));
        assertThat(pay(newOrder())).isEqualTo("PAYMENT_FAILED");
    }

    @Test
    void secondPaymentIsRejectedAndNotCharged() {
        paymentReturns("APPROVED");
        int id = newOrder();
        pay(id);
        given().spec(user).post("/api/orders/" + id + "/pay").then().statusCode(409);
        wm.verify(1, postRequestedFor(urlEqualTo("/api/payments")));
    }

    @Test
    void declinedPaymentMarksFailed() {
        paymentReturns("DECLINED");
        assertThat(pay(newOrder())).isEqualTo("PAYMENT_FAILED");
    }
}
