package ru.qacourse.shop.events;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

/** Последние 200 событий — их видно в /api/debug/events (удобно для тестов без Kafka). */
@Component
public class EventLog {
    private final Deque<OrderEvent> events = new ConcurrentLinkedDeque<>();

    public void add(OrderEvent e) {
        events.addLast(e);
        while (events.size() > 200) events.pollFirst();
    }

    public List<OrderEvent> all() {
        return new ArrayList<>(events);
    }

    public void clear() {
        events.clear();
    }
}
