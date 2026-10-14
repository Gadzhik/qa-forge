package ru.qacourse.shop.order;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.qacourse.shop.bugs.Bug;
import ru.qacourse.shop.bugs.BugRegistry;
import ru.qacourse.shop.cart.CartItem;
import ru.qacourse.shop.cart.CartRepository;
import ru.qacourse.shop.common.ApiException;
import ru.qacourse.shop.events.EventPublisher;
import ru.qacourse.shop.events.OrderEvent;
import ru.qacourse.shop.payment.PaymentClient;
import ru.qacourse.shop.product.Product;
import ru.qacourse.shop.product.ProductRepository;
import ru.qacourse.shop.user.Role;
import ru.qacourse.shop.user.User;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final OrderRepository orders;
    private final CartRepository cart;
    private final ProductRepository products;
    private final PromoRepository promos;
    private final PaymentClient payment;
    private final EventPublisher events;
    private final BugRegistry bugs;

    public OrderService(OrderRepository orders, CartRepository cart, ProductRepository products, PromoRepository promos,
                        PaymentClient payment, EventPublisher events, BugRegistry bugs) {
        this.orders = orders;
        this.cart = cart;
        this.products = products;
        this.promos = promos;
        this.payment = payment;
        this.events = events;
        this.bugs = bugs;
    }

    @Transactional
    public Order create(User user, String promoCode) {
        List<CartItem> cartItems = cart.findByUserIdOrderByIdAsc(user.getId());
        if (cartItems.isEmpty() && !bugs.isOn(Bug.BUG_403)) {
            throw ApiException.badRequest("EMPTY_CART", "Корзина пуста");
        }
        List<OrderItem> items = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItem ci : cartItems) {
            Product p = products.findById(ci.getProductId()).orElseThrow(() -> ApiException.notFound("Товар"));
            if (ci.getQuantity() > p.getStock() && !bugs.isOn(Bug.BUG_303)) {
                throw ApiException.badRequest("OUT_OF_STOCK", "Товара «" + p.getName() + "» осталось " + p.getStock() + " шт.");
            }
            items.add(new OrderItem(p.getId(), p.getName(), p.getPrice(), ci.getQuantity()));
            subtotal = subtotal.add(p.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity())));
            p.setStock(Math.max(0, p.getStock() - ci.getQuantity()));
            products.save(p);
        }

        BigDecimal discount = BigDecimal.ZERO;
        String appliedPromo = null;
        if (promoCode != null && !promoCode.isBlank()) {
            PromoCode promo = promos.findById(promoCode.trim().toUpperCase())
                    .orElseThrow(() -> ApiException.badRequest("BAD_PROMO", "Промокод не найден"));
            if (promo.getValidUntil().isBefore(LocalDate.now()) && !bugs.isOn(Bug.BUG_402)) {
                throw ApiException.badRequest("PROMO_EXPIRED", "Срок действия промокода истёк");
            }
            BigDecimal rate = BigDecimal.valueOf(promo.getPercent()).divide(HUNDRED);
            discount = subtotal.multiply(rate);
            if (bugs.isOn(Bug.BUG_401)) {
                // скидка применяется дважды: 1 - (1 - r)^2
                BigDecimal once = subtotal.subtract(discount);
                discount = discount.add(once.multiply(rate));
            }
            discount = discount.setScale(2, RoundingMode.HALF_UP);
            appliedPromo = promo.getCode();
        }
        BigDecimal total = subtotal.subtract(discount);
        total = bugs.isOn(Bug.BUG_404)
                ? total.setScale(0, RoundingMode.FLOOR).setScale(2, RoundingMode.UNNECESSARY)
                : total.setScale(2, RoundingMode.HALF_UP);

        Order order = orders.save(new Order(user.getId(), items, subtotal.setScale(2, RoundingMode.HALF_UP), discount, total, appliedPromo));
        cart.deleteByUserId(user.getId());
        log.info("order created id={} user={} total={}", order.getId(), user.getUsername(), total);
        publish(OrderEvent.CREATED, order);
        return order;
    }

    public List<Order> list(User user) {
        return orders.findByUserIdOrderByIdAsc(user.getId());
    }

    public List<Order> listAll() {
        return orders.findAll();
    }

    public Order get(User user, long id) {
        Order order = orders.findById(id).orElseThrow(() -> ApiException.notFound("Заказ " + id));
        boolean owner = order.getUserId().equals(user.getId());
        if (!owner && user.getRole() != Role.ADMIN && !bugs.isOn(Bug.BUG_204)) {
            // для чужого заказа отвечаем 404, чтобы не раскрывать сам факт его существования
            throw ApiException.notFound("Заказ " + id);
        }
        return order;
    }

    @Transactional
    public Order pay(User user, long id) {
        Order order = get(user, id);
        Set<OrderStatus> payable = Set.of(OrderStatus.NEW, OrderStatus.PAYMENT_FAILED);
        if (!payable.contains(order.getStatus())) {
            boolean doubleCharge = bugs.isOn(Bug.BUG_502) && order.getStatus() != OrderStatus.CANCELLED;
            if (!doubleCharge) {
                throw ApiException.conflict("NOT_PAYABLE", "Заказ в статусе " + order.getStatus() + " нельзя оплатить");
            }
            payment.charge(order.getId(), order.getTotal());
            return order;
        }
        boolean approved;
        try {
            approved = payment.charge(order.getId(), order.getTotal()).approved();
        } catch (Exception e) {
            log.error("payment service error for order {}: {}", id, e.getMessage());
            approved = bugs.isOn(Bug.BUG_503);
        }
        if (approved) {
            order.setStatus(OrderStatus.PAID);
            orders.save(order);
            if (!bugs.isOn(Bug.BUG_501)) {
                publish(OrderEvent.PAID, order);
            }
        } else {
            order.setStatus(OrderStatus.PAYMENT_FAILED);
            orders.save(order);
        }
        return order;
    }

    @Transactional
    public Order cancel(User user, long id) {
        Order order = get(user, id);
        boolean cancellable = order.getStatus() == OrderStatus.NEW || order.getStatus() == OrderStatus.PAYMENT_FAILED;
        if (!cancellable && !(bugs.isOn(Bug.BUG_405) && order.getStatus() != OrderStatus.CANCELLED)) {
            throw ApiException.conflict("NOT_CANCELLABLE", "Заказ в статусе " + order.getStatus() + " нельзя отменить");
        }
        order.setStatus(OrderStatus.CANCELLED);
        orders.save(order);
        publish(OrderEvent.CANCELLED, order);
        return order;
    }

    private void publish(String type, Order o) {
        events.publish(new OrderEvent(type, o.getId(), o.getUserId(), o.getTotal(), o.getStatus().name(), Instant.now()));
    }
}
