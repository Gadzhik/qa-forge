package ru.qacourse.fw.api;

import io.qameta.allure.Step;
import io.restassured.specification.RequestSpecification;
import ru.qacourse.fw.models.CartDto;

import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * Задание 14.2: клиент корзины. Методы уже работают — добавьте каждому аннотацию @Step с понятным описанием
 * (по образцу AuthClient): «Очистить корзину», «Добавить в корзину товар {productId} × {quantity}», «Получить корзину».
 */
public class CartClient {
    private final RequestSpecification spec;

    public CartClient(String token) {
        this.spec = Specs.withToken(token);
    }

    public CartClient clear() {
        given(spec).delete("/api/cart").then().statusCode(204);
        return this;
    }

    public CartClient add(long productId, int quantity) {
        given(spec).body(Map.of("productId", productId, "quantity", quantity))
                .post("/api/cart/items").then().statusCode(200);
        return this;
    }

    public CartDto get() {
        return given(spec).get("/api/cart").then().statusCode(200).extract().as(CartDto.class);
    }
}
