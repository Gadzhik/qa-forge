package ru.qacourse.m10.tasks;

import com.codeborne.selenide.Configuration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.qacourse.support.ShopConfig;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.CollectionCondition.*;
import static com.codeborne.selenide.Selenide.*;
import static org.assertj.core.api.Assertions.fail;

import io.restassured.http.ContentType;
import org.openqa.selenium.Cookie;

import java.util.Map;

import static com.codeborne.selenide.WebDriverRunner.getWebDriver;
import static io.restassured.RestAssured.given;

/**
 * Задание 10.14: авторизация в UI через API.
 * 1) Получите токен user через POST /api/auth/login (REST Assured).
 * 2) Положите в корзину 2 шт. товара 12 через API.
 * 3) Откройте любую лёгкую страницу сайта (например, /playground), добавьте cookie "token" с токеном,
 *    откройте главную — в шапке имя «user» ([data-testid=user-name]) и счётчик корзины «2».
 */
public class ApiLoginUiTest {
    @BeforeEach
    void setUp() {
        Configuration.baseUrl = ShopConfig.baseUrl();
    }

    @AfterEach
    void tearDown() {
        closeWebDriver();
    }

    @Test
    void loggedInViaCookie() {
        String token = given().baseUri(ShopConfig.baseUrl()).contentType(ContentType.JSON)
                .body(Map.of("username", "user", "password", "user123"))
                .post("/api/auth/login").then().statusCode(200).extract().path("token");
        given().baseUri(ShopConfig.baseUrl()).contentType(ContentType.JSON).auth().oauth2(token)
                .body(Map.of("productId", 12, "quantity", 2))
                .post("/api/cart/items").then().statusCode(200);

        open("/playground");
        getWebDriver().manage().addCookie(new Cookie("token", token));
        open("/");

        $("[data-testid=user-name]").shouldHave(exactText("user"));
        $("[data-testid=cart-count]").shouldHave(exactText("2"));
    }
}
