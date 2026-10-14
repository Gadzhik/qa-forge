package ru.qacourse.m11.checks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.qacourse.m11.Queries;

import static org.assertj.core.api.Assertions.*;

/** Проверка задания. Не изменяйте этот файл. */
class ShopDbCheck extends DbCheckBase {

    private ru.qacourse.m11.ShopDb db() {
        return new ru.qacourse.m11.ShopDb(app.jdbcUrl(), "sa", "");
    }

    @Test
    @DisplayName("Пример productCount работает")
    void count() throws Exception {
        try (var db = db()) {
            assertThat(db.productCount()).isEqualTo(20);
        }
    }

    @Test
    @DisplayName("stockOf и productNames")
    void reads() throws Exception {
        try (var db = db()) {
            assertThat(db.stockOf("Мороженое рожок")).isEqualTo(100);
            assertThat(db.stockOf("Пицца")).isEqualTo(-1);
            assertThat(db.stockOf("x' OR '1'='1")).as("никакой склейки строк — только параметры").isEqualTo(-1);
            assertThat(db.productNames("DESSERT")).containsExactly("Молочный коктейль", "Мороженое рожок", "Пирожок вишнёвый", "Чизкейк");
        }
    }

    @Test
    @DisplayName("orderStatus")
    void status() throws Exception {
        try (var db = db()) {
            assertThat(db.orderStatus(999)).isEmpty();
            String token = JoinQueriesCheck.login(app.baseUrl(), "user", "user123");
            int id = JoinQueriesCheck.order(app.baseUrl(), token, 12, 1);
            assertThat(db.orderStatus(id)).hasValue("NEW");
        }
    }

    @Test
    @DisplayName("moveStock — транзакция: при нехватке ничего не меняется")
    void move() throws Exception {
        try (var db = db()) {
            int lemonade = db.stockOf("Лимонад домашний");
            int mors = db.stockOf("Морс клюквенный");
            db.moveStock("Лимонад домашний", "Морс клюквенный", 5);
            assertThat(db.stockOf("Лимонад домашний")).isEqualTo(lemonade - 5);
            assertThat(db.stockOf("Морс клюквенный")).isEqualTo(mors + 5);
            assertThatThrownBy(() -> db.moveStock("Чизкейк", "Морс клюквенный", 1000)).isInstanceOf(IllegalStateException.class);
            assertThat(db.stockOf("Чизкейк")).isEqualTo(12);
            assertThat(db.stockOf("Морс клюквенный")).isEqualTo(mors + 5);
        }
    }
}
