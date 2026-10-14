package ru.qacourse.m12.tasks;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.assertj.core.api.Assertions.*;

/**
 * Задание 12.2: основы WireMock (без QA Burger).
 * Поднимите WireMockServer на свободном порту в @BeforeEach и остановите в @AfterEach.
 * 1) Заглушка GET /api/rates?currency=USD → 200, JSON {"currency":"USD","rate":92.5}; запрос возвращает rate = 92.5;
 *    проверьте через verify, что запрос пришёл ровно 1 раз и с параметром currency=USD.
 * 2) Заглушка POST /api/notify с телом, где $.type = "ORDER_READY" → 202; другое тело → 404 (заглушки нет).
 * 3) Заглушка GET /api/slow с задержкой 1500 мс → ответ пришёл не быстрее чем за 1500 мс.
 */
public class WireMockBasicsTest {
    private WireMockServer wm;

    @BeforeEach
    void start() {
        wm = new WireMockServer(wireMockConfig().dynamicPort());
        wm.start();
    }

    @AfterEach
    void stop() {
        wm.stop();
    }

    @Test
    void stubAndVerify() {
        wm.stubFor(get(urlPathEqualTo("/api/rates"))
                .withQueryParam("currency", equalTo("USD"))
                .willReturn(okJson("{\"currency\":\"USD\",\"rate\":92.5}")));

        given().baseUri(wm.baseUrl()).queryParam("currency", "USD")
                .get("/api/rates")
                .then().statusCode(200).body("rate", equalTo(92.5f));

        wm.verify(1, getRequestedFor(urlPathEqualTo("/api/rates")).withQueryParam("currency", equalTo("USD")));
    }

    @Test
    void matchByBody() {
        wm.stubFor(post("/api/notify")
                .withRequestBody(matchingJsonPath("$.type", equalTo("ORDER_READY")))
                .willReturn(aResponse().withStatus(202)));

        given().baseUri(wm.baseUrl()).contentType("application/json").body("{\"type\":\"ORDER_READY\",\"orderId\":7}")
                .post("/api/notify").then().statusCode(202);
        given().baseUri(wm.baseUrl()).contentType("application/json").body("{\"type\":\"OTHER\"}")
                .post("/api/notify").then().statusCode(404);
    }

    @Test
    void delay() {
        wm.stubFor(get("/api/slow").willReturn(ok("готово").withFixedDelay(1500)));
        long start = System.currentTimeMillis();
        given().baseUri(wm.baseUrl()).get("/api/slow").then().statusCode(200);
        assertThat(System.currentTimeMillis() - start).isGreaterThanOrEqualTo(1500);
    }
}
