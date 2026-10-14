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

public class Bug703Test {
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
    @DisplayName("Кнопка «В корзину» на странице товара действительно добавляет товар")
    void addToCartFromProductPage() {
        String token = login("user", "user123");
        loginInBrowser(token);
        open("/product/1");
        $("[data-testid=add-to-cart]").click();
        $("[data-testid=toast]").shouldBe(visible);
        int quantity = given().baseUri(base).auth().oauth2(token).get("/api/cart")
                .then().statusCode(200).extract().path("totalQuantity");
        assertThat(quantity).isEqualTo(1);
    }
}
