package ru.qacourse.testlab.student;

import com.codeborne.selenide.Configuration;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Cookie;
import ru.qacourse.support.ShopConfig;

import java.util.Map;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.WebDriverRunner.getWebDriver;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class Bug601Test {
    private final String base = ShopConfig.baseUrl();

    @BeforeEach
    void setUp() {
        Configuration.baseUrl = base;
    }

    @AfterEach
    void tearDown() {
        closeWebDriver();
    }

    private String login(String username, String password) {
        return given().baseUri(base).contentType(ContentType.JSON)
                .body(Map.of("username", username, "password", password))
                .post("/api/auth/login").then().statusCode(200).extract().path("token");
    }

    /** Авторизация в браузере без формы входа: кладём токен из API в cookie. */
    private void loginInBrowser(String token) {
        open("/playground");
        getWebDriver().manage().addCookie(new Cookie("token", token));
    }

    @Test
    @DisplayName("HTML в отзыве показывается как текст, а не выполняется")
    void reviewHtmlIsEscaped() {
        given().baseUri(base).auth().oauth2(login("user", "user123")).contentType(ContentType.JSON)
                .body(Map.of("text", "<b>жирный</b>", "rating", 5))
                .post("/api/products/1/reviews").then().statusCode(201);
        open("/product/1");
        $$("[data-testid=review-text]").last().shouldHave(text("<b>жирный</b>"));
    }
}
