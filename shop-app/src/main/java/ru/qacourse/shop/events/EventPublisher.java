package ru.qacourse.shop.events;

/** Куда отправлять события: в Kafka (профиль kafka) или в память (по умолчанию). */
public interface EventPublisher {
    void publish(OrderEvent event);
}
