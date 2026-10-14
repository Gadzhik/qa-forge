package ru.qacourse.fw.api;

import io.qameta.allure.Step;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import ru.qacourse.fw.models.OrderDto;

import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * Задание 14.2/14.5: клиент заказов.
 * create(promo) — POST /api/orders (promoCode может быть null) → 201 и OrderDto;
 * tryCreate(promo) — то же, но без проверки статуса, возвращает ValidatableResponse (для негативных тестов);
 * pay(id), get(id). Каждому методу — @Step.
 */
public class OrderClient {
    private final RequestSpecification spec;

    public OrderClient(String token) {
        this.spec = Specs.withToken(token);
    }

    public OrderDto create(String promoCode) {
        throw new UnsupportedOperationException("TODO");
    }

    public ValidatableResponse tryCreate(String promoCode) {
        throw new UnsupportedOperationException("TODO");
    }

    public OrderDto pay(long id) {
        throw new UnsupportedOperationException("TODO");
    }

    public OrderDto get(long id) {
        throw new UnsupportedOperationException("TODO");
    }
}
