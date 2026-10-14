package ru.qacourse.shop.launcher;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.context.ConfigurableApplicationContext;
import ru.qacourse.shop.ShopApplication;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Запускает QA Burger прямо внутри теста: на свободном порту, с отдельной базой в памяти
 * и нужным набором багов. Используется проверками заданий и тренажёром Test Lab.
 *
 * <pre>
 * try (ShopAppLauncher.Running app = ShopAppLauncher.start(Map.of("shop.bugs", "BUG-101"))) {
 *     RestAssured.baseURI = app.baseUrl();
 *     ...
 * }
 * </pre>
 */
public final class ShopAppLauncher {

    private ShopAppLauncher() {
    }

    public record Running(ConfigurableApplicationContext context, int port, String jdbcUrl) implements AutoCloseable {
        public String baseUrl() {
            return "http://localhost:" + port;
        }

        @Override
        public void close() {
            context.close();
        }
    }

    public static Running start() {
        return start(Map.of());
    }

    public static Running start(Map<String, Object> properties) {
        Map<String, Object> props = new HashMap<>();
        props.put("server.port", 0);
        String dbName = "shop-" + UUID.randomUUID();
        int h2Port = freePort();
        props.put("spring.datasource.url", "jdbc:h2:mem:" + dbName + ";DB_CLOSE_DELAY=-1");
        props.put("shop.h2.tcp-port", h2Port);
        props.put("spring.main.banner-mode", "off");
        props.put("logging.level.root", "WARN");
        props.put("logging.level.ru.qacourse", "INFO");
        props.put("logging.file.name", "");
        props.put("springdoc.api-docs.enabled", false);
        props.put("spring.graphql.graphiql.enabled", false);
        props.putAll(properties);
        SpringApplication app = new SpringApplication(ShopApplication.class);
        app.setWebApplicationType(WebApplicationType.SERVLET);
        app.setAdditionalProfiles("embedded");
        // Аргументы командной строки имеют наивысший приоритет — перекрывают application.yml
        String[] args = props.entrySet().stream()
                .map(e -> "--" + e.getKey() + "=" + e.getValue())
                .toArray(String[]::new);
        ConfigurableApplicationContext ctx = app.run(args);
        int port = Integer.parseInt(ctx.getEnvironment().getProperty("local.server.port"));
        String jdbcUrl = String.valueOf(props.get("spring.datasource.url")).startsWith("jdbc:h2:mem:")
                ? "jdbc:h2:tcp://localhost:" + h2Port + "/mem:" + dbName
                : String.valueOf(props.get("spring.datasource.url"));
        return new Running(ctx, port, jdbcUrl);
    }

    private static int freePort() {
        try (java.net.ServerSocket s = new java.net.ServerSocket(0)) {
            return s.getLocalPort();
        } catch (java.io.IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
