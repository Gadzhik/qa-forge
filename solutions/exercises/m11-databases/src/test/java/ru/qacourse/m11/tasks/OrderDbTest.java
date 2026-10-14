package ru.qacourse.m11.tasks;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.qacourse.m11.ShopDb;
import ru.qacourse.support.ShopConfig;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.*;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Задание 11.7: проверка побочных эффектов в базе.
 * Через API (user / user123) положите в корзину 1 шт. товара «Классический бургер» (id 1, 289.90) и оформите заказ.
 * Затем через JDBC проверьте:
 *  - в таблице orders у этого заказа total = 289.90 и status = NEW;
 *  - в order_items ровно одна строка этого заказа, quantity = 1;
 *  - остаток (stock) бургера уменьшился на 1 по сравнению со значением до заказа.
 * Подключение: ShopConfig.dbUrl(), dbUser(), dbPassword().
 */
public class OrderDbTest {
    private ShopDb db;

    @BeforeEach
    void setUp() {
        db = new ShopDb(ShopConfig.dbUrl(), ShopConfig.dbUser(), ShopConfig.dbPassword());
    }

    @AfterEach
    void tearDown() {
        db.close();
    }

    @Test
    void orderIsSavedCorrectly() throws Exception {
        int stockBefore = db.stockOf("Классический бургер");

        String token = given().baseUri(ShopConfig.baseUrl()).contentType(ContentType.JSON)
                .body(Map.of("username", "user", "password", "user123"))
                .post("/api/auth/login").then().statusCode(200).extract().path("token");
        given().baseUri(ShopConfig.baseUrl()).contentType(ContentType.JSON).auth().oauth2(token)
                .delete("/api/cart").then().statusCode(204);
        given().baseUri(ShopConfig.baseUrl()).contentType(ContentType.JSON).auth().oauth2(token)
                .body(Map.of("productId", 1, "quantity", 1)).post("/api/cart/items").then().statusCode(200);
        int orderId = given().baseUri(ShopConfig.baseUrl()).contentType(ContentType.JSON).auth().oauth2(token)
                .body("{}").post("/api/orders").then().statusCode(201).extract().path("id");

        try (Connection c = DriverManager.getConnection(ShopConfig.dbUrl(), ShopConfig.dbUser(), ShopConfig.dbPassword())) {
            try (PreparedStatement st = c.prepareStatement("SELECT total, status FROM orders WHERE id = ?")) {
                st.setLong(1, orderId);
                try (ResultSet rs = st.executeQuery()) {
                    assertThat(rs.next()).as("заказ есть в таблице orders").isTrue();
                    assertThat(rs.getBigDecimal("total")).isEqualByComparingTo(new BigDecimal("289.90"));
                    assertThat(rs.getString("status")).isEqualTo("NEW");
                }
            }
            try (PreparedStatement st = c.prepareStatement("SELECT quantity FROM order_items WHERE order_id = ?")) {
                st.setLong(1, orderId);
                try (ResultSet rs = st.executeQuery()) {
                    assertThat(rs.next()).isTrue();
                    assertThat(rs.getInt("quantity")).isEqualTo(1);
                    assertThat(rs.next()).as("в заказе ровно одна позиция").isFalse();
                }
            }
        }
        assertThat(db.stockOf("Классический бургер")).isEqualTo(stockBefore - 1);
    }
}
