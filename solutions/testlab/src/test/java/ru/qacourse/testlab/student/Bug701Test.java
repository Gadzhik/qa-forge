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

public class Bug701Test {
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
    @DisplayName("Счётчик корзины показывает общее количество штук")
    void cartCounterShowsQuantity() {
        String token = login("user", "user123");
        given().baseUri(base).auth().oauth2(token).contentType(ContentType.JSON)
                .body(Map.of("productId", 1, "quantity", 3)).post("/api/cart/items").then().statusCode(200);
        loginInBrowser(token);
        open("/");
        $("[data-testid=cart-count]").shouldHave(exactText("3"));
    }
}
