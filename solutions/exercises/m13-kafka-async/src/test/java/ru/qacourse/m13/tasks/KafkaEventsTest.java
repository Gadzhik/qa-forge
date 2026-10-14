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
    private KafkaConsumer<String, String> consumer;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, ShopConfig.kafkaBootstrap());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        consumer = new KafkaConsumer<>(props);

        List<TopicPartition> partitions = consumer.partitionsFor(TOPIC, Duration.ofSeconds(10)).stream()
                .map(p -> new TopicPartition(TOPIC, p.partition()))
                .toList();
        consumer.assign(partitions);
        consumer.seekToEnd(partitions);
        partitions.forEach(consumer::position);   // зафиксировать позицию «конец» до создания заказа
    }

    @AfterEach
    void tearDown() {
        consumer.close();
    }

    @Test
    void orderEventsArePublished() {
        String token = given().baseUri(ShopConfig.baseUrl()).contentType(ContentType.JSON)
                .body(Map.of("username", "user", "password", "user123"))
                .post("/api/auth/login").then().statusCode(200).extract().path("token");
        RequestSpecification user = given().baseUri(ShopConfig.baseUrl()).contentType(ContentType.JSON).auth().oauth2(token);
        given().spec(user).delete("/api/cart").then().statusCode(204);
        given().spec(user).body(Map.of("productId", 12, "quantity", 1)).post("/api/cart/items").then().statusCode(200);
        int id = given().spec(user).body("{}").post("/api/orders").then().statusCode(201).extract().path("id");
        given().spec(user).post("/api/orders/" + id + "/pay").then().statusCode(200);

        Map<String, JsonNode> events = new LinkedHashMap<>();
        await().atMost(Duration.ofSeconds(20)).pollInterval(Duration.ofMillis(200)).untilAsserted(() -> {
            for (ConsumerRecord<String, String> record : consumer.poll(Duration.ofMillis(500))) {
                if (String.valueOf(id).equals(record.key())) {
                    JsonNode event = mapper.readTree(record.value());
                    events.put(event.get("type").asText(), event);
                }
            }
            assertThat(events).containsKeys("ORDER_CREATED", "ORDER_PAID");
        });

        JsonNode paid = events.get("ORDER_PAID");
        assertThat(paid.get("status").asText()).isEqualTo("PAID");
        assertThat(paid.get("total").decimalValue()).isPositive();
    }
}
