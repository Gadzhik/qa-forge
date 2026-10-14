package ru.qacourse.m13.checks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.qacourse.support.BugCatcher;
import ru.qacourse.support.ShopConfig;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Map;

import static org.assertj.core.api.Assertions.fail;

/** Проверка задания 13.4: нужна запущенная Kafka (docker compose up -d kafka). Не изменяйте. */
class KafkaEventsTestCheck {
    @Test
    @DisplayName("Kafka-тест ловит BUG-501 (событие ORDER_PAID не публикуется)")
    void catches() {
        String[] hostPort = ShopConfig.kafkaBootstrap().split(":");
        try (Socket s = new Socket()) {
            s.connect(new InetSocketAddress(hostPort[0], Integer.parseInt(hostPort[1])), 1000);
        } catch (Exception e) {
            fail("Kafka не запущена на " + ShopConfig.kafkaBootstrap()
                    + ". Запустите её: docker compose up -d kafka (урок 13.4) и повторите проверку.");
        }
        BugCatcher.assertCatches(ru.qacourse.m13.tasks.KafkaEventsTest.class, "BUG-501", Map.of(
                "spring.profiles.active", "kafka",
                "spring.kafka.bootstrap-servers", ShopConfig.kafkaBootstrap(),
                "spring.kafka.listener.auto-startup", true));
    }
}
