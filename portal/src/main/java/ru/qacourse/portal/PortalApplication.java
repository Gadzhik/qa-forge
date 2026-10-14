package ru.qacourse.portal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Портал курса. Запуск: скрипт start.sh / start.cmd в корне проекта,
 * затем откройте http://localhost:8080
 */
@SpringBootApplication
public class PortalApplication {
    public static void main(String[] args) {
        SpringApplication.run(PortalApplication.class, args);
    }
}
