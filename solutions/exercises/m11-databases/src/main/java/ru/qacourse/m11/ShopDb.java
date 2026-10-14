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
        try (Connection c = dataSource.getConnection();
             PreparedStatement st = c.prepareStatement("SELECT stock FROM products WHERE name = ?")) {
            st.setString(1, productName);
            try (ResultSet rs = st.executeQuery()) {
                return rs.next() ? rs.getInt("stock") : -1;
            }
        }
    }

    /** Статус заказа по id или пусто, если заказа нет. */
    public Optional<String> orderStatus(long orderId) throws SQLException {
        try (Connection c = dataSource.getConnection();
             PreparedStatement st = c.prepareStatement("SELECT status FROM orders WHERE id = ?")) {
            st.setLong(1, orderId);
            try (ResultSet rs = st.executeQuery()) {
                return rs.next() ? Optional.of(rs.getString("status")) : Optional.empty();
            }
        }
    }

    /** Названия товаров категории по алфавиту. */
    public List<String> productNames(String category) throws SQLException {
        List<String> names = new ArrayList<>();
        try (Connection c = dataSource.getConnection();
             PreparedStatement st = c.prepareStatement("SELECT name FROM products WHERE category = ? ORDER BY name")) {
            st.setString(1, category);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    names.add(rs.getString("name"));
                }
            }
        }
        return names;
    }

    /**
     * Перенести остаток: уменьшить stock у товара from на amount и увеличить у товара to — В ОДНОЙ ТРАНЗАКЦИИ.
     * Если у from не хватает остатка — откатить и бросить IllegalStateException.
     */
    public void moveStock(String from, String to, int amount) throws SQLException {
        try (Connection c = dataSource.getConnection()) {
            c.setAutoCommit(false);
            try (PreparedStatement dec = c.prepareStatement("UPDATE products SET stock = stock - ? WHERE name = ? AND stock >= ?");
                 PreparedStatement inc = c.prepareStatement("UPDATE products SET stock = stock + ? WHERE name = ?")) {
                dec.setInt(1, amount);
                dec.setString(2, from);
                dec.setInt(3, amount);
                if (dec.executeUpdate() != 1) {
                    c.rollback();
                    throw new IllegalStateException("Недостаточно товара: " + from);
                }
                inc.setInt(1, amount);
                inc.setString(2, to);
                inc.executeUpdate();
                c.commit();
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        }
    }

    @Override
    public void close() {
        dataSource.close();
    }
}
