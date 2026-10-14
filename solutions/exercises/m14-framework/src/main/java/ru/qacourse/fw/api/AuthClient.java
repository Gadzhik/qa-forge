package ru.qacourse.fw.api;

import io.qameta.allure.Step;

import java.util.Map;

import static io.restassured.RestAssured.given;

/** Клиент авторизации (готовый образец клиента). */
public class AuthClient {

    @Step("Войти как {username}")
    public String login(String username, String password) {
        return given(Specs.base())
                .body(Map.of("username", username, "password", password))
                .post("/api/auth/login")
                .then().statusCode(200)
                .extract().path("token");
    }
}
