package ru.qacourse.shop.payment;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.UUID;

/** Встроенная заглушка платёжного сервиса: одобряет всё, что не больше 50 000 ₽. */
@Hidden
@RestController
@RequestMapping("/fake-payment/api/payments")
public class FakePaymentController {
    @PostMapping
    public PaymentClient.PaymentResponse pay(@RequestBody PaymentClient.PaymentRequest req) {
        boolean ok = req.amount().compareTo(new BigDecimal("50000")) <= 0;
        return new PaymentClient.PaymentResponse(ok ? "APPROVED" : "DECLINED", UUID.randomUUID().toString());
    }
}
