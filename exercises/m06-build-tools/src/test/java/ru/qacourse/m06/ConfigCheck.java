package ru.qacourse.m06;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.aeonbits.owner.ConfigFactory;
import org.junit.jupiter.api.AfterEach;

import static org.assertj.core.api.Assertions.*;

/** Проверка задания. Не изменяйте этот файл. */
class ConfigCheck {

    @AfterEach
    void cleanup() {
        System.clearProperty("base.url");
        System.clearProperty("timeout.seconds");
    }

    @Test
    @DisplayName("AppConfig читает config.properties")
    void fromFile() {
        assertThat(AppConfig.baseUrl()).isEqualTo("http://localhost:8081");
        assertThat(AppConfig.timeoutSeconds()).isEqualTo(10);
        assertThat(AppConfig.get("nope", "default")).isEqualTo("default");
    }

    @Test
    @DisplayName("AppConfig: системное свойство важнее файла")
    void systemWins() {
        System.setProperty("base.url", "http://test-stand:8081");
        System.setProperty("timeout.seconds", "30");
        assertThat(AppConfig.baseUrl()).isEqualTo("http://test-stand:8081");
        assertThat(AppConfig.timeoutSeconds()).isEqualTo(30);
    }

    @Test
    @DisplayName("Owner: значения из файла")
    void ownerFile() {
        OwnerConfig cfg = ConfigFactory.create(OwnerConfig.class);
        assertThat(cfg.baseUrl()).isEqualTo("http://localhost:8081");
        assertThat(cfg.timeoutSeconds()).isEqualTo(10);
        assertThat(cfg.browser()).isEqualTo("chrome");
    }

    @Test
    @DisplayName("Owner: системное свойство важнее файла")
    void ownerSystem() {
        System.setProperty("base.url", "http://stage:8081");
        OwnerConfig cfg = ConfigFactory.create(OwnerConfig.class);
        assertThat(cfg.baseUrl()).isEqualTo("http://stage:8081");
    }
}
