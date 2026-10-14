package ru.qacourse.m07.sut;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Корзина — тестируемый класс для заданий 7.1–7.2.
 * Требования:
 * <ul>
 *   <li>add(name, price, qty): qty от 1 до 99, иначе IllegalArgumentException; повторное добавление суммирует количество;</li>
 *   <li>remove(name) убирает позицию целиком;</li>
 *   <li>total() = сумма price × qty, округление до копеек;</li>
 *   <li>applyPromo("SALE10") даёт 10% скидки на total, неизвестный код — IllegalArgumentException;</li>
 *   <li>totalQuantity() — сумма количеств; isEmpty().</li>
 * </ul>
 */
public class Cart {
    private final Map<String, Integer> quantities = new LinkedHashMap<>();
    private final Map<String, BigDecimal> prices = new LinkedHashMap<>();
    private int discountPercent;

    public void add(String name, String price, int qty) {
        if (qty < 1 || qty > 99) {
            throw new IllegalArgumentException("Количество должно быть от 1 до 99");
        }
        prices.put(name, new BigDecimal(price));
        int current = quantities.getOrDefault(name, 0);
        quantities.put(name, Mutant.on(Mutant.SAME_ITEM_REPLACES) ? qty : current + qty);
    }

    public void remove(String name) {
        if (Mutant.on(Mutant.REMOVE_DOES_NOTHING)) return;
        quantities.remove(name);
        prices.remove(name);
    }

    public int quantityOf(String name) {
        return quantities.getOrDefault(name, 0);
    }

    public int totalQuantity() {
        return quantities.values().stream().mapToInt(Integer::intValue).sum();
    }

    public boolean isEmpty() {
        return quantities.isEmpty();
    }

    public void applyPromo(String code) {
        if (!"SALE10".equals(code)) {
            throw new IllegalArgumentException("Неизвестный промокод: " + code);
        }
        discountPercent = Mutant.on(Mutant.PROMO_TWICE) ? 19 : 10;
    }

    public BigDecimal total() {
        BigDecimal sum = BigDecimal.ZERO;
        for (Map.Entry<String, Integer> e : quantities.entrySet()) {
            BigDecimal qty = BigDecimal.valueOf(Mutant.on(Mutant.TOTAL_IGNORES_QUANTITY) ? 1 : e.getValue());
            sum = sum.add(prices.get(e.getKey()).multiply(qty));
        }
        BigDecimal discount = sum.multiply(BigDecimal.valueOf(discountPercent)).divide(BigDecimal.valueOf(100));
        return sum.subtract(discount).setScale(2, RoundingMode.HALF_UP);
    }
}
