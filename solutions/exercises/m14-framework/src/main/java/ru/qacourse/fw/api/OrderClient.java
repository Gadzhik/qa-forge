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

    @Step("Оформить заказ с промокодом {promoCode}")
    public OrderDto create(String promoCode) {
        return tryCreate(promoCode).statusCode(201).extract().as(OrderDto.class);
    }

    @Step("Попытаться оформить заказ с промокодом {promoCode}")
    public ValidatableResponse tryCreate(String promoCode) {
        Map<String, Object> body = promoCode == null ? Map.of() : Map.of("promoCode", promoCode);
        return given(spec).body(body).post("/api/orders").then();
    }

    @Step("Оплатить заказ {id}")
    public OrderDto pay(long id) {
        return given(spec).post("/api/orders/" + id + "/pay").then().statusCode(200).extract().as(OrderDto.class);
    }

    @Step("Получить заказ {id}")
    public OrderDto get(long id) {
        return given(spec).get("/api/orders/" + id).then().statusCode(200).extract().as(OrderDto.class);
    }
}
