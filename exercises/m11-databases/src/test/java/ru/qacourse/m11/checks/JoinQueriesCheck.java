package ru.qacourse.m11.checks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.qacourse.m11.Queries;

import static org.assertj.core.api.Assertions.*;

/** Проверка задания. Не изменяйте этот файл. */
class JoinQueriesCheck extends DbCheckBase {

    @Test
    @DisplayName("COUNT_BY_CATEGORY")
    void byCategory() throws Exception {
        var rows = rows(Queries.COUNT_BY_CATEGORY);
        assertThat(rows).extracting(r -> r.get(0) + "=" + str(r.get(1)))
                .containsExactly("BURGER=6", "DESSERT=4", "DRINK=5", "SIDE=5");
    }

    @Test
    @DisplayName("AVG_RATING (JOIN с reviews)")
    void avg() throws Exception {
        var rows = rows(Queries.AVG_RATING);
        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).get(0)).isEqualTo("Классический бургер");
        assertThat(Double.parseDouble(str(rows.get(0).get(1)))).isEqualTo(4.5);
    }

    @Test
    @DisplayName("REVENUE_BY_USER и ITEMS_OF_ORDER (заказы создаются через API)")
    void revenue() throws Exception {
        String base = app.baseUrl();
        String user = login(base, "user", "user123");
        String admin = login(base, "admin", "admin123");
        int o1 = order(base, user, 1, 2);     // 579.80
        order(base, admin, 12, 1);            // 99.00
        var rows = rows(Queries.REVENUE_BY_USER);
        assertThat(rows).extracting(r -> r.get(0) + "=" + str(r.get(1))).containsExactly("user=579.8", "admin=99");
        var items = rows(Queries.ITEMS_OF_ORDER, o1);
        assertThat(items).extracting(r -> r.get(0) + "x" + r.get(1)).containsExactly("Классический бургерx2");
    }

    static String login(String base, String u, String p) {
        return io.restassured.RestAssured.given().baseUri(base).contentType("application/json")
                .body(java.util.Map.of("username", u, "password", p)).post("/api/auth/login").then().extract().path("token");
    }

    static int order(String base, String token, int productId, int qty) {
        io.restassured.RestAssured.given().baseUri(base).contentType("application/json").auth().oauth2(token)
                .body(java.util.Map.of("productId", productId, "quantity", qty)).post("/api/cart/items").then().statusCode(200);
        return io.restassured.RestAssured.given().baseUri(base).contentType("application/json").auth().oauth2(token)
                .body("{}").post("/api/orders").then().statusCode(201).extract().path("id");
    }
}
