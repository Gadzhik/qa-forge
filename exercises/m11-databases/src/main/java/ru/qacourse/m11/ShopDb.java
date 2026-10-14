package ru.qacourse.m11;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Задание 11.6: доступ к базе QA Burger через JDBC и пул соединений HikariCP.
 * Используйте PreparedStatement с параметрами "?" (никакой склейки строк!) и try-with-resources.
 */
public class ShopDb implements AutoCloseable {
    private final HikariDataSource dataSource;

    public ShopDb(String url, String user, String password) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(user);
        config.setPassword(password);
        config.setMaximumPoolSize(3);
        this.dataSource = new HikariDataSource(config);
    }

    /** Пример, готовый: количество товаров в таблице products. */
    public int productCount() throws SQLException {
        try (Connection c = dataSource.getConnection();
             PreparedStatement st = c.prepareStatement("SELECT COUNT(*) FROM products");
             ResultSet rs = st.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    /** Остаток товара по точному названию; если товара нет — -1. */
    public int stockOf(String productName) throws SQLException {
        throw new UnsupportedOperationException("TODO");
    }

    /** Статус заказа по id или пусто, если заказа нет. */
    public Optional<String> orderStatus(long orderId) throws SQLException {
        throw new UnsupportedOperationException("TODO");
    }

    /** Названия товаров категории по алфавиту. */
    public List<String> productNames(String category) throws SQLException {
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * Перенести остаток: уменьшить stock у товара from на amount и увеличить у товара to — В ОДНОЙ ТРАНЗАКЦИИ.
     * Если у from не хватает остатка — откатить и бросить IllegalStateException.
     */
    public void moveStock(String from, String to, int amount) throws SQLException {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public void close() {
        dataSource.close();
    }
}
