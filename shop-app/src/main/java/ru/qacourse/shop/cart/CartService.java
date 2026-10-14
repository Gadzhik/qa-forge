package ru.qacourse.shop.cart;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.qacourse.shop.bugs.Bug;
import ru.qacourse.shop.bugs.BugRegistry;
import ru.qacourse.shop.common.ApiException;
import ru.qacourse.shop.product.Product;
import ru.qacourse.shop.product.ProductRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CartService {
    private final CartRepository cart;
    private final ProductRepository products;
    private final BugRegistry bugs;

    public CartService(CartRepository cart, ProductRepository products, BugRegistry bugs) {
        this.cart = cart;
        this.products = products;
        this.bugs = bugs;
    }

    @Transactional
    public CartView add(long userId, long productId, int quantity) {
        if (quantity < 1 && !bugs.isOn(Bug.BUG_301)) {
            throw ApiException.badRequest("BAD_QUANTITY", "Количество должно быть больше 0");
        }
        if (quantity > 99) {
            throw ApiException.badRequest("BAD_QUANTITY", "Не больше 99 штук за раз");
        }
        Product product = products.findById(productId).orElseThrow(() -> ApiException.notFound("Товар " + productId));
        CartItem item = cart.findByUserIdAndProductId(userId, productId).orElse(null);
        int newQuantity;
        if (item == null) {
            newQuantity = quantity;
        } else {
            newQuantity = bugs.isOn(Bug.BUG_302) ? quantity : item.getQuantity() + quantity;
        }
        if (newQuantity > product.getStock() && !bugs.isOn(Bug.BUG_303)) {
            throw ApiException.badRequest("OUT_OF_STOCK", "На складе только " + product.getStock() + " шт.");
        }
        if (item == null) {
            cart.save(new CartItem(userId, productId, newQuantity));
        } else {
            item.setQuantity(newQuantity);
            cart.save(item);
        }
        return view(userId);
    }

    @Transactional
    public CartView remove(long userId, long productId) {
        cart.findByUserIdAndProductId(userId, productId).ifPresent(cart::delete);
        return view(userId);
    }

    @Transactional
    public void clear(long userId) {
        cart.deleteByUserId(userId);
    }

    public CartView view(long userId) {
        List<CartView.Line> lines = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        int count = 0;
        for (CartItem item : cart.findByUserIdOrderByIdAsc(userId)) {
            Product p = products.findById(item.getProductId()).orElse(null);
            if (p == null) continue;
            BigDecimal lineTotal = p.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            lines.add(new CartView.Line(p.getId(), p.getName(), p.getEmoji(), p.getPrice(), item.getQuantity(), lineTotal));
            total = total.add(bugs.isOn(Bug.BUG_304) ? p.getPrice() : lineTotal);
            count += item.getQuantity();
        }
        return new CartView(lines, count, total);
    }
}
