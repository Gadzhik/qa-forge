package ru.qacourse.shop.cart;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.qacourse.shop.auth.CurrentUser;

@RestController
@RequestMapping("/api/cart")
@Tag(name = "Корзина (нужен токен)")
public class CartController {

    public record AddItemRequest(@NotNull Long productId, int quantity) {
    }

    private final CartService service;

    public CartController(CartService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Содержимое корзины")
    public CartView get(HttpServletRequest req) {
        return service.view(CurrentUser.require(req).getId());
    }

    @PostMapping("/items")
    @Operation(summary = "Добавить товар в корзину (количество суммируется)")
    public CartView add(@Valid @RequestBody AddItemRequest body, HttpServletRequest req) {
        return service.add(CurrentUser.require(req).getId(), body.productId(), body.quantity());
    }

    @DeleteMapping("/items/{productId}")
    @Operation(summary = "Убрать товар из корзины")
    public CartView remove(@PathVariable long productId, HttpServletRequest req) {
        return service.remove(CurrentUser.require(req).getId(), productId);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Очистить корзину")
    public void clear(HttpServletRequest req) {
        service.clear(CurrentUser.require(req).getId());
    }
}
