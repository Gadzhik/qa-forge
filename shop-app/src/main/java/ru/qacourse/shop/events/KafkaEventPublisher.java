package ru.qacourse.shop.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.admin.NewTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Режим с Kafka (профиль kafka, включается профилем docker).
 * Заказ → JSON-сообщение в топик qa-burger.orders (ключ = id заказа).
 * Кухня — отдельный consumer в группе "kitchen", читает тот же топик.
 */
@Configuration
@Profile("kafka")
public class KafkaEventPublisher {
    public static final String TOPIC = "qa-burger.orders";
    private static final Logger log = LoggerFactory.getLogger(KafkaEventPublisher.class);

    @Bean
    public NewTopic ordersTopic() {
        return TopicBuilder.name(TOPIC).partitions(3).replicas(1).build();
    }

    @Bean
    public EventPublisher kafkaPublisher(KafkaTemplate<String, String> template, ObjectMapper mapper, EventLog eventLog) {
        return event -> {
            try {
                eventLog.add(event);
                template.send(TOPIC, String.valueOf(event.orderId()), mapper.writeValueAsString(event));
                log.info("kafka -> {} {} order={}", TOPIC, event.type(), event.orderId());
            } catch (Exception e) {
                throw new IllegalStateException("Не удалось отправить событие в Kafka", e);
            }
        };
    }

    @Component
    @Profile("kafka")
    public static class KitchenListener {
        private final KitchenService kitchen;
        private final ObjectMapper mapper;

        public KitchenListener(KitchenService kitchen, ObjectMapper mapper) {
            this.kitchen = kitchen;
            this.mapper = mapper;
        }

        @KafkaListener(topics = TOPIC, groupId = "kitchen")
        public void onMessage(String json) throws Exception {
            OrderEvent event = mapper.readValue(json, OrderEvent.class);
            log.info("kafka <- {} {} order={}", TOPIC, event.type(), event.orderId());
            kitchen.onEvent(event);
        }
    }
}
