package ru.qacourse.m09;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import ru.qacourse.m09.models.LoginRequest;
import ru.qacourse.support.ShopConfig;

import static io.restassured.RestAssured.given;

/**
 * Готовые спецификации запросов (разбор — урок 9.5).
 * Адрес приложения читается при КАЖДОМ вызове: проверки запускают QA Burger на разных портах.
 */
public final class Specs {
    private Specs() {
    }

    /** Базовая спецификация: адрес, JSON, логирование при ошибке. */
    public static RequestSpecification base() {
        return new RequestSpecBuilder()
                .setBaseUri(ShopConfig.baseUrl())
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .log(LogDetail.METHOD)
                .log(LogDetail.URI)
                .build();
    }

    /** То же + заголовок Authorization с токеном пользователя. */
    public static RequestSpecification auth(String username, String password) {
        return new RequestSpecBuilder()
                .addRequestSpecification(base())
                .addHeader("Authorization", "Bearer " + token(username, password))
                .build();
    }

    public static String token(String username, String password) {
        return given(base())
                .body(new LoginRequest(username, password))
                .post("/api/auth/login")
                .then().statusCode(200)
                .extract().path("token");
    }

    /** Ожидание «200 и JSON». */
    public static ResponseSpecification ok200() {
        return new ResponseSpecBuilder().expectStatusCode(200).expectContentType(ContentType.JSON).build();
    }
}
