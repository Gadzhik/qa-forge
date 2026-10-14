package ru.qacourse.m11.checks;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import ru.qacourse.shop.launcher.ShopAppLauncher;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Запускает QA Burger и даёт выполнять SQL в его базе. */
abstract class DbCheckBase {
    static ShopAppLauncher.Running app;

    @BeforeAll
    static void start() {
        app = ShopAppLauncher.start();
    }

    @AfterAll
    static void stop() {
        app.close();
    }

    static Connection connect() throws SQLException {
        return DriverManager.getConnection(app.jdbcUrl(), "sa", "");
    }

    /** Все строки результата; каждая строка — список значений столбцов. */
    static List<List<Object>> rows(String sql, Object... params) throws SQLException {
        try (Connection c = connect(); PreparedStatement st = c.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) st.setObject(i + 1, params[i]);
            try (ResultSet rs = st.executeQuery()) {
                List<List<Object>> result = new ArrayList<>();
                int cols = rs.getMetaData().getColumnCount();
                while (rs.next()) {
                    List<Object> row = new ArrayList<>();
                    for (int i = 1; i <= cols; i++) row.add(rs.getObject(i));
                    result.add(row);
                }
                return result;
            }
        }
    }

    static List<Object> column(String sql, Object... params) throws SQLException {
        return rows(sql, params).stream().map(r -> r.get(0)).toList();
    }

    static String str(Object o) {
        return o == null ? null : (o instanceof java.math.BigDecimal bd ? bd.stripTrailingZeros().toPlainString() : o.toString());
    }
}
