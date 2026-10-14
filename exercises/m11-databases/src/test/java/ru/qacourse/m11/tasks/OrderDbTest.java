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
    @Test
    void orderInDb() {
        fail("TODO: напишите тест");
    }
}
