package ru.qacourse.shop.admin;

import org.h2.tools.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.sql.SQLException;

/**
 * Открывает встроенную базу H2 для подключения извне по JDBC (модуль 11):
 *   jdbc:h2:tcp://localhost:9093/mem:shop   пользователь sa, пароль пустой
 * Так автотесты и DBeaver могут заглянуть в базу работающего QA Burger без Docker.
 * Работает только с H2 (в профиле postgres база и так доступна на порту 5432).
 */
@Component
public class H2TcpServer {
    private static final Logger log = LoggerFactory.getLogger(H2TcpServer.class);

    private final String datasourceUrl;
    private final int port;
    private Server server;

    public H2TcpServer(@Value("${spring.datasource.url}") String datasourceUrl,
                       @Value("${shop.h2.tcp-port:9093}") int port) {
        this.datasourceUrl = datasourceUrl;
        this.port = port;
    }

    @PostConstruct
    void start() {
        if (!datasourceUrl.startsWith("jdbc:h2:mem:") || port <= 0) {
            return;
        }
        try {
            server = Server.createTcpServer("-tcpPort", String.valueOf(port), "-tcpDaemon").start();
            String db = datasourceUrl.substring("jdbc:h2:".length()).split(";")[0];
            log.info("H2 доступна по JDBC: jdbc:h2:tcp://localhost:{}/{} (пользователь sa, пароль пустой)", port, db);
        } catch (SQLException e) {
            log.warn("Не удалось открыть H2 по TCP на порту {}: {}", port, e.getMessage());
        }
    }

    @PreDestroy
    void stop() {
        if (server != null) {
            server.stop();
        }
    }
}
