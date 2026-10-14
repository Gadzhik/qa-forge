package ru.qacourse.m11.checks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.qacourse.m11.Queries;
import java.sql.*;

import static org.assertj.core.api.Assertions.*;

/** Проверка задания. Не изменяйте этот файл. */
class ModifyQueriesCheck extends DbCheckBase {

    /** Выполнить изменение внутри транзакции и откатить, вернув результат проверки. */
    interface InTx {
        void run(Connection c) throws Exception;
    }

    static void inRollback(InTx body) throws Exception {
        try (Connection c = connect()) {
            c.setAutoCommit(false);
            try {
                body.run(c);
            } finally {
                c.rollback();
            }
        }
    }

    @Test
    @DisplayName("INSERT_PROMO добавляет промокод")
    void insert() throws Exception {
        inRollback(c -> {
            try (PreparedStatement st = c.prepareStatement(Queries.INSERT_PROMO)) {
                st.setString(1, "TEST50");
                st.setInt(2, 50);
                st.setDate(3, java.sql.Date.valueOf("2099-01-01"));
                assertThat(st.executeUpdate()).isEqualTo(1);
            }
            try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery("SELECT percent FROM promo_codes WHERE code = 'TEST50'")) {
                assertThat(rs.next()).isTrue();
                assertThat(rs.getInt(1)).isEqualTo(50);
            }
        });
    }

    @Test
    @DisplayName("RAISE_PRICES меняет цены только в категории")
    void update() throws Exception {
        inRollback(c -> {
            try (PreparedStatement st = c.prepareStatement(Queries.RAISE_PRICES)) {
                st.setString(1, "DRINK");
                assertThat(st.executeUpdate()).isEqualTo(5);
            }
            try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery(
                    "SELECT (SELECT price FROM products WHERE name = 'Кола 0.5'), (SELECT price FROM products WHERE name = 'Картофель фри')")) {
                rs.next();
                assertThat(rs.getBigDecimal(1)).isEqualByComparingTo("108.90");
                assertThat(rs.getBigDecimal(2)).isEqualByComparingTo("129.00");
            }
        });
    }

    @Test
    @DisplayName("DELETE_LOW_REVIEWS удаляет отзывы с низкой оценкой")
    void delete() throws Exception {
        inRollback(c -> {
            try (PreparedStatement st = c.prepareStatement(Queries.DELETE_LOW_REVIEWS)) {
                st.setInt(1, 5);
                assertThat(st.executeUpdate()).isEqualTo(1);
            }
        });
    }
}
