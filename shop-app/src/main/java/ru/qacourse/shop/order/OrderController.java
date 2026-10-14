package ru.qacourse.shop.order;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.qacourse.shop.auth.CurrentUser;
import ru.qacourse.shop.user.User;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Заказы (нужен токен)")
public class OrderController {

    public record CreateOrderRequest(String promoCode) {
    }

    private final OrderService service;
    private final DocumentService documents;

    public OrderController(OrderService service, DocumentService documents) {
        this.service = service;
        this.documents = documents;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Оформить заказ из корзины (промокод необязателен: SALE10, BURGER20)")
    public Order create(@RequestBody(required = false) CreateOrderRequest body, HttpServletRequest req) {
        return service.create(CurrentUser.require(req), body == null ? null : body.promoCode());
    }

    @GetMapping
    @Operation(summary = "Мои заказы")
    public List<Order> list(HttpServletRequest req) {
        return service.list(CurrentUser.require(req));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Заказ по id")
    public Order get(@PathVariable long id, HttpServletRequest req) {
        return service.get(CurrentUser.require(req), id);
    }

    @PostMapping("/{id}/pay")
    @Operation(summary = "Оплатить заказ (обращается к внешнему платёжному сервису)")
    public Order pay(@PathVariable long id, HttpServletRequest req) {
        return service.pay(CurrentUser.require(req), id);
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Отменить заказ (только NEW или PAYMENT_FAILED)")
    public Order cancel(@PathVariable long id, HttpServletRequest req) {
        return service.cancel(CurrentUser.require(req), id);
    }

    @GetMapping("/{id}/receipt.pdf")
    @Operation(summary = "Скачать PDF-чек")
    public ResponseEntity<byte[]> receipt(@PathVariable long id, HttpServletRequest req) {
        Order order = service.get(CurrentUser.require(req), id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=receipt-" + id + ".pdf")
                .body(documents.receiptPdf(order));
    }

    @GetMapping("/export.xlsx")
    @Operation(summary = "Выгрузить мои заказы в Excel")
    public ResponseEntity<byte[]> export(HttpServletRequest req) {
        User user = CurrentUser.require(req);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=orders.xlsx")
                .body(documents.ordersXlsx(service.list(user)));
    }
}
