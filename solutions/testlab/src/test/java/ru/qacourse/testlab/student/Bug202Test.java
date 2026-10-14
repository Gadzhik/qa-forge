package ru.qacourse.testlab.student;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.qacourse.support.ShopConfig;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class Bug202Test {
    private final String base = ShopConfig.baseUrl();

    @Test
    @DisplayName("Пароль короче 6 символов отклоняется")
    void shortPasswordRejected() {
        given().baseUri(base).contentType(ContentType.JSON)
                .body(Map.of("username", "shorty", "email", "shorty@test.local", "password", "123"))
                .post("/api/auth/register").then().statusCode(400);
    }
}
