package ru.qacourse.m09;

import io.restassured.response.ValidatableResponse;
import org.hamcrest.Matcher;

/**
 * Задание 9.8: обёртка над ответом REST Assured с понятными методами-проверками.
 *
 *   new AssertableResponse(given(...).get(...).then())
 *       .shouldHaveStatus(200)
 *       .shouldHaveField("name", equalTo("Кола 0.5"))
 *       .shouldHaveError(400, "BAD_QUANTITY");
 *
 * Каждый метод возвращает this, чтобы проверки шли цепочкой.
 */
public class AssertableResponse {
    private final ValidatableResponse response;

    public AssertableResponse(ValidatableResponse response) {
        this.response = response;
    }

    public AssertableResponse shouldHaveStatus(int status) {
        response.statusCode(status);
        return this;
    }

    public AssertableResponse shouldHaveField(String jsonPath, Matcher<?> matcher) {
        response.body(jsonPath, matcher);
        return this;
    }

    /** Ошибка QA Burger: заданный статус и поле code. */
    public AssertableResponse shouldHaveError(int status, String code) {
        return shouldHaveStatus(status).shouldHaveField("code", org.hamcrest.Matchers.equalTo(code));
    }

    /** Достать тело как объект. */
    public <T> T asPojo(Class<T> type) {
        return response.extract().as(type);
    }
}
