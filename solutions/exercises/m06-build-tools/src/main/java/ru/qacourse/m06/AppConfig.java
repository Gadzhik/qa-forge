package ru.qacourse.m06;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Задание 6.7: свой «читатель настроек».
 * Приоритет: системное свойство (-Dключ=значение) → файл config.properties → значение по умолчанию.
 */
public class AppConfig {
    private static final Properties FILE = load();

    private static Properties load() {
        Properties props = new Properties();
        try (InputStream in = AppConfig.class.getResourceAsStream("/config.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось прочитать config.properties", e);
        }
        return props;
    }

    /** Значение по ключу с учётом приоритетов. */
    public static String get(String key, String defaultValue) {
        String fromSystem = System.getProperty(key);
        if (fromSystem != null) {
            return fromSystem;
        }
        return FILE.getProperty(key, defaultValue);
    }

    public static String baseUrl() {
        return get("base.url", "http://localhost:8081");
    }

    /** Таймаут в секундах как число. По умолчанию 5. */
    public static int timeoutSeconds() {
        return Integer.parseInt(get("timeout.seconds", "5"));
    }
}
