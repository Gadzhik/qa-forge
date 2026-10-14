package ru.qacourse.shop.cart;

import java.math.BigDecimal;
import java.util.List;

/** То, что API отдаёт по запросу корзины. */
public record CartView(List<Line> items, int totalQuantity, BigDecimal total) {
    public record Line(long productId, String name, String emoji, BigDecimal price, int quantity, BigDecimal lineTotal) {
    }
}
