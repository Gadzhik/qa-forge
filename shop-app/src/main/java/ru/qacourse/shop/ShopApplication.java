package ru.qacourse.shop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Точка входа учебного приложения QA Burger.
 * Запуск: ./mvnw -pl shop-app spring-boot:run  (или скриптом start.sh / start.cmd).
 */
@SpringBootApplication
@EnableAsync
public class ShopApplication {
    public static void main(String[] args) {
        SpringApplication.run(ShopApplication.class, args);
    }
}
