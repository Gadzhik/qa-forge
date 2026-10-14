package ru.qacourse.shop.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import ru.qacourse.shop.order.OrderRepository;
import ru.qacourse.shop.order.OrderStatus;

import jakarta.annotation.PreDestroy;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * "Кухня": получив ORDER_PAID, через паузу ставит COOKING, затем READY.
 * Работает асинхронно — поэтому в тестах статус нужно ждать (Awaitility), а не проверять сразу.
 */
@Service
public class KitchenService {
    private static final Logger log = LoggerFactory.getLogger(KitchenService.class);

    private final OrderRepository orders;
    private final EventPublisher publisher;
    private final Duration cookingDelay;
    private final Duration readyDelay;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    public KitchenService(OrderRepository orders, @Lazy EventPublisher publisher,
                          @Value("${shop.kitchen.cooking-delay:PT2S}") Duration cookingDelay,
                          @Value("${shop.kitchen.ready-delay:PT3S}") Duration readyDelay) {
        this.orders = orders;
        this.publisher = publisher;
        this.cookingDelay = cookingDelay;
        this.readyDelay = readyDelay;
    }

    public void onEvent(OrderEvent event) {
        if (!OrderEvent.PAID.equals(event.type())) return;
        scheduler.schedule(() -> move(event, OrderStatus.PAID, OrderStatus.COOKING, OrderEvent.COOKING),
                cookingDelay.toMillis(), TimeUnit.MILLISECONDS);
        scheduler.schedule(() -> move(event, OrderStatus.COOKING, OrderStatus.READY, OrderEvent.READY),
                cookingDelay.plus(readyDelay).toMillis(), TimeUnit.MILLISECONDS);
    }

    private void move(OrderEvent event, OrderStatus from, OrderStatus to, String type) {
        try {
            orders.findById(event.orderId()).ifPresent(o -> {
                if (o.getStatus() != from) return;
                o.setStatus(to);
                orders.save(o);
                log.info("kitchen: order={} {} -> {}", o.getId(), from, to);
                publisher.publish(new OrderEvent(type, o.getId(), o.getUserId(), o.getTotal(), to.name(), Instant.now()));
            });
        } catch (Exception e) {
            log.error("kitchen failed for order {}", event.orderId(), e);
        }
    }

    @PreDestroy
    void shutdown() {
        scheduler.shutdownNow();
    }
}
