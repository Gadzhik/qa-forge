package ru.qacourse.shop.events;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Событие о заказе. В профиле kafka уходит в топик qa-burger.orders в виде JSON:
 * {"type":"ORDER_PAID","orderId":1,"userId":2,"total":379.80,"status":"PAID","timestamp":"..."}
 */
public record OrderEvent(String type, long orderId, long userId, BigDecimal total, String status, Instant timestamp) {
    public static final String CREATED = "ORDER_CREATED";
    public static final String PAID = "ORDER_PAID";
    public static final String COOKING = "ORDER_COOKING";
    public static final String READY = "ORDER_READY";
    public static final String CANCELLED = "ORDER_CANCELLED";
}
