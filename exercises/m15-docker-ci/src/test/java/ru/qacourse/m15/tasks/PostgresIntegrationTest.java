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
    @Test
    void postgres() {
        fail("TODO: напишите тест");
    }
}
