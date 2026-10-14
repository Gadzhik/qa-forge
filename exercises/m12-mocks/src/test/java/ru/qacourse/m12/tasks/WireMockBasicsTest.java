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
    @Test
    void basics() {
        fail("TODO: напишите тест");
    }
}
