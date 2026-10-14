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
    public static final String DRINKS_BY_PRICE = "SELECT 'TODO'";

    /** Сколько товаров дешевле 100 ₽. Один столбец, одна строка. */
    public static final String COUNT_CHEAPER_THAN_100 = "SELECT -1";

    /** Названия товаров, в названии которых есть «бургер» в любом регистре. Один столбец name. */
    public static final String BURGERS_BY_NAME = "SELECT 'TODO'";

    /** Три самых дорогих товара: name и price, от дорогого к дешёвому. */
    public static final String TOP3_EXPENSIVE = "SELECT 'TODO', 0";

    // ===================== 11.3 JOIN и группировка =====================

    /** Количество товаров по категориям: category, count — отсортировано по category. */
    public static final String COUNT_BY_CATEGORY = "SELECT 'TODO', 0";

    /** Название товара и средняя оценка (avg_rating) — только для товаров с отзывами. JOIN products и reviews. */
    public static final String AVG_RATING = "SELECT 'TODO', 0";

    /** Пользователи и сумма их заказов: username, total_sum — только те, у кого есть заказы, по убыванию суммы. */
    public static final String REVENUE_BY_USER = "SELECT 'TODO', 0";

    /** Позиции заказа с id = ? (параметр): name и quantity, по id позиции. */
    public static final String ITEMS_OF_ORDER = "SELECT 'TODO', 0 WHERE ? IS NOT NULL";

    // ===================== 11.4 Изменение данных =====================

    /** Добавить промокод: параметры (code, percent, valid_until). */
    public static final String INSERT_PROMO = "SELECT ?, ?, ?";

    /** Поднять цены на 10% в категории (параметр category). */
    public static final String RAISE_PRICES = "SELECT ?";

    /** Удалить все отзывы с оценкой ниже параметра. */
    public static final String DELETE_LOW_REVIEWS = "SELECT ?";
}
