package ru.qacourse.m11;

/**
 * Задания 11.2–11.4: напишите SQL-запросы к базе QA Burger.
 * Таблицы: users, products, reviews, promo_codes, cart_items, orders, order_items (схема — урок 11.1).
 * Проверьте запросы в консоли БД (http://localhost:8081/h2-console или DBeaver), потом нажмите «Проверить».
 */
public final class Queries {
    private Queries() {
    }

    // ===================== 11.2 SELECT =====================

    /** Названия напитков (category = 'DRINK'), от дешёвых к дорогим. Один столбец name. */
    public static final String DRINKS_BY_PRICE =
            "SELECT name FROM products WHERE category = 'DRINK' ORDER BY price";

    /** Сколько товаров дешевле 100 ₽. Один столбец, одна строка. */
    public static final String COUNT_CHEAPER_THAN_100 =
            "SELECT COUNT(*) FROM products WHERE price < 100";

    /** Названия товаров, в названии которых есть «бургер» в любом регистре. Один столбец name. */
    public static final String BURGERS_BY_NAME =
            "SELECT name FROM products WHERE LOWER(name) LIKE '%бургер%'";

    /** Три самых дорогих товара: name и price, от дорогого к дешёвому. */
    public static final String TOP3_EXPENSIVE =
            "SELECT name, price FROM products ORDER BY price DESC LIMIT 3";

    // ===================== 11.3 JOIN и группировка =====================

    /** Количество товаров по категориям: category, count — отсортировано по category. */
    public static final String COUNT_BY_CATEGORY =
            "SELECT category, COUNT(*) AS cnt FROM products GROUP BY category ORDER BY category";

    /** Название товара и средняя оценка (avg_rating) — только для товаров с отзывами. JOIN products и reviews. */
    public static final String AVG_RATING =
            "SELECT p.name, AVG(r.rating * 1.0) AS avg_rating FROM products p "
                    + "JOIN reviews r ON r.product_id = p.id GROUP BY p.name";

    /** Пользователи и сумма их заказов: username, total_sum — только те, у кого есть заказы, по убыванию суммы. */
    public static final String REVENUE_BY_USER =
            "SELECT u.username, SUM(o.total) AS total_sum FROM users u "
                    + "JOIN orders o ON o.user_id = u.id GROUP BY u.username ORDER BY total_sum DESC";

    /** Позиции заказа с id = ? (параметр): name и quantity, по id позиции. */
    public static final String ITEMS_OF_ORDER =
            "SELECT name, quantity FROM order_items WHERE order_id = ? ORDER BY id";

    // ===================== 11.4 Изменение данных =====================

    /** Добавить промокод: параметры (code, percent, valid_until). */
    public static final String INSERT_PROMO =
            "INSERT INTO promo_codes (code, percent, valid_until) VALUES (?, ?, ?)";

    /** Поднять цены на 10% в категории (параметр category). */
    public static final String RAISE_PRICES =
            "UPDATE products SET price = price * 1.1 WHERE category = ?";

    /** Удалить все отзывы с оценкой ниже параметра. */
    public static final String DELETE_LOW_REVIEWS =
            "DELETE FROM reviews WHERE rating < ?";
}
