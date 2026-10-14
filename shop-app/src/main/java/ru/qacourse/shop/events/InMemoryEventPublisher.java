package ru.qacourse.shop.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/** Режим без Docker: событие сразу передаётся кухне внутри приложения. */
@Component
@Profile("!kafka")
public class InMemoryEventPublisher implements EventPublisher {
    private static final Logger log = LoggerFactory.getLogger(InMemoryEventPublisher.class);
    private final EventLog eventLog;
    private final KitchenService kitchen;

    public InMemoryEventPublisher(EventLog eventLog, KitchenService kitchen) {
        this.eventLog = eventLog;
        this.kitchen = kitchen;
    }

    @Override
    public void publish(OrderEvent event) {
        log.info("event {} order={}", event.type(), event.orderId());
        eventLog.add(event);
        kitchen.onEvent(event);
    }
}
