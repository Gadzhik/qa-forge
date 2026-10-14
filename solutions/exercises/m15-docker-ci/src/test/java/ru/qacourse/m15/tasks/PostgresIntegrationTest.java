package ru.qacourse.m15.tasks;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import ru.qacourse.shop.launcher.ShopAppLauncher;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.*;

/**
 * Задание 15.3: Testcontainers.
 * В @BeforeAll: запустить контейнер PostgreSQL 16 (postgres:16-alpine), затем запустить QA Burger
 * на этой базе: ShopAppLauncher.start(Map.of(
 *     "spring.datasource.url", pg.getJdbcUrl(),
 *     "spring.datasource.username", pg.getUsername(),
 *     "spring.datasource.password", pg.getPassword())).
 * В @AfterAll — остановить приложение и контейнер.
 * Тест: через API (app.baseUrl()) user/user123 кладёт в корзину 2 шт. товара 12 и оформляет заказ;
 * через JDBC к контейнеру проверить, что в orders есть заказ с total = 198.00, а в order_items — quantity = 2.
 */
public class PostgresIntegrationTest {
    static PostgreSQLContainer<?> pg;
    static ShopAppLauncher.Running app;

    @BeforeAll
    static void start() {
        pg = new PostgreSQLContainer<>("postgres:16-alpine");
        pg.start();
        app = ShopAppLauncher.start(Map.of(
                "spring.datasource.url", pg.getJdbcUrl(),
                "spring.datasource.username", pg.getUsername(),
                "spring.datasource.password", pg.getPassword()));
    }

    @AfterAll
    static void stop() {
        if (app != null) app.close();
        if (pg != null) pg.stop();
    }

    @Test
    void orderIsStoredInPostgres() throws Exception {
        String token = given().baseUri(app.baseUrl()).contentType(ContentType.JSON)
                .body(Map.of("username", "user", "password", "user123"))
                .post("/api/auth/login").then().statusCode(200).extract().path("token");
        given().baseUri(app.baseUrl()).contentType(ContentType.JSON).auth().oauth2(token)
                .body(Map.of("productId", 12, "quantity", 2)).post("/api/cart/items").then().statusCode(200);
        int id = given().baseUri(app.baseUrl()).contentType(ContentType.JSON).auth().oauth2(token)
                .body("{}").post("/api/orders").then().statusCode(201).extract().path("id");

        try (Connection c = DriverManager.getConnection(pg.getJdbcUrl(), pg.getUsername(), pg.getPassword())) {
            try (PreparedStatement st = c.prepareStatement("SELECT total FROM orders WHERE id = ?")) {
                st.setLong(1, id);
                try (ResultSet rs = st.executeQuery()) {
                    assertThat(rs.next()).isTrue();
                    assertThat(rs.getBigDecimal("total")).isEqualByComparingTo("198.00");
                }
            }
            try (PreparedStatement st = c.prepareStatement("SELECT quantity FROM order_items WHERE order_id = ?")) {
                st.setLong(1, id);
                try (ResultSet rs = st.executeQuery()) {
                    assertThat(rs.next()).isTrue();
                    assertThat(rs.getInt("quantity")).isEqualTo(2);
                }
            }
        }
    }
}
