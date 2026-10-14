package ru.qacourse.m13.tasks;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.qacourse.support.ShopConfig;

import java.time.Duration;
import java.util.*;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.await;

/**
 * Задание 13.4: проверка событий в Kafka.
 * QA Burger публикует события заказов в топик qa-burger.orders (ключ — id заказа, значение — JSON с полем type).
 * 1) Создайте consumer (bootstrap.servers = ShopConfig.kafkaBootstrap()), назначьте ему ВСЕ разделы топика
 *    и перемотайте в конец (seekToEnd) — чтобы читать только новые сообщения.
 * 2) Через API оформите и оплатите заказ.
 * 3) Читайте топик (poll) внутри Awaitility, собирая type событий с ключом = id заказа,
 *    пока не увидите ORDER_CREATED и ORDER_PAID (до 20 с).
 * 4) Проверьте, что у события ORDER_PAID поле status = PAID и total > 0.
 */
public class KafkaEventsTest {
    static final String TOPIC = "qa-burger.orders";
    @Test
    void kafka() {
        fail("TODO: напишите тест");
    }
}
