package ru.qacourse.support;

/**
 * Где искать QA Burger.
 * По умолчанию — http://localhost:8081 (приложение запущено скриптом start).
 * Проверки заданий подставляют свой адрес через системное свойство shop.url.
 */
public final class ShopConfig {
    private ShopConfig() {
    }

    public static String baseUrl() {
        String fromProperty = System.getProperty("shop.url");
        if (fromProperty != null && !fromProperty.isBlank()) return fromProperty;
        String fromEnv = System.getenv("SHOP_URL");
        if (fromEnv != null && !fromEnv.isBlank()) return fromEnv;
        return "http://localhost:8081";
    }

    /**
     * JDBC-адрес базы QA Burger (модуль 11). По умолчанию — встроенная H2 запущенного приложения.
     * Для Docker-профиля: -Dshop.db.url=jdbc:postgresql://localhost:5432/qaburger (пользователь и пароль qaburger).
     */
    public static String dbUrl() {
        return value("shop.db.url", "SHOP_DB_URL", "jdbc:h2:tcp://localhost:9093/mem:shop");
    }

    public static String dbUser() {
        return value("shop.db.user", "SHOP_DB_USER", dbUrl().startsWith("jdbc:postgresql") ? "qaburger" : "sa");
    }

    public static String dbPassword() {
        return value("shop.db.password", "SHOP_DB_PASSWORD", dbUrl().startsWith("jdbc:postgresql") ? "qaburger" : "");
    }

    /** Адрес Kafka (модуль 13). */
    public static String kafkaBootstrap() {
        return value("shop.kafka", "SHOP_KAFKA", "localhost:9092");
    }

    /** Порт WireMock, который подменяет платёжный сервис (модуль 12). */
    public static int paymentMockPort() {
        return Integer.parseInt(value("payment.mock.port", "PAYMENT_MOCK_PORT", "8089"));
    }

    private static String value(String property, String env, String defaultValue) {
        String fromProperty = System.getProperty(property);
        if (fromProperty != null && !fromProperty.isBlank()) return fromProperty;
        String fromEnv = System.getenv(env);
        if (fromEnv != null && !fromEnv.isBlank()) return fromEnv;
        return defaultValue;
    }
}
