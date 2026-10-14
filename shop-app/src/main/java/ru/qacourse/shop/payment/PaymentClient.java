package ru.qacourse.shop.payment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

/**
 * Клиент внешнего платёжного сервиса.
 * Адрес задаётся свойством shop.payment.url. Если не задан — используется встроенная
 * заглушка /fake-payment этого же приложения. В уроке про WireMock мы подменяем адрес
 * на WireMock-сервер и управляем ответами "платёжки" из теста.
 */
@Component
public class PaymentClient {
    private static final Logger log = LoggerFactory.getLogger(PaymentClient.class);

    public record PaymentRequest(long orderId, BigDecimal amount, String currency) {
    }

    public record PaymentResponse(String status, String transactionId) {
        public boolean approved() {
            return "APPROVED".equals(status);
        }
    }

    private final String configuredUrl;
    private final Environment env;

    public PaymentClient(@Value("${shop.payment.url:}") String configuredUrl, Environment env) {
        this.configuredUrl = configuredUrl;
        this.env = env;
    }

    public PaymentResponse charge(long orderId, BigDecimal amount) {
        String base = configuredUrl.isBlank()
                ? "http://localhost:" + env.getProperty("local.server.port", "8081") + "/fake-payment"
                : configuredUrl;
        SimpleClientHttpRequestFactory rf = new SimpleClientHttpRequestFactory();
        rf.setConnectTimeout(2000);
        rf.setReadTimeout(5000);
        log.info("payment -> {}/api/payments order={} amount={}", base, orderId, amount);
        PaymentResponse response = RestClient.builder().requestFactory(rf).baseUrl(base).build()
                .post().uri("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new PaymentRequest(orderId, amount, "RUB"))
                .retrieve()
                .body(PaymentResponse.class);
        log.info("payment <- order={} status={}", orderId, response == null ? null : response.status());
        return response;
    }
}
