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
        throw new UnsupportedOperationException("TODO");
    }

    public AssertableResponse shouldHaveField(String jsonPath, Matcher<?> matcher) {
        throw new UnsupportedOperationException("TODO");
    }

    /** Ошибка QA Burger: заданный статус и поле code. */
    public AssertableResponse shouldHaveError(int status, String code) {
        throw new UnsupportedOperationException("TODO");
    }

    /** Достать тело как объект. */
    public <T> T asPojo(Class<T> type) {
        throw new UnsupportedOperationException("TODO");
    }
}
