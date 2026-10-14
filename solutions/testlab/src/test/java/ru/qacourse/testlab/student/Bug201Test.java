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

public class Bug201Test {
    private final String base = ShopConfig.baseUrl();

    @Test
    @DisplayName("Повторная регистрация с тем же email — 409")
    void duplicateEmailRejected() {
        given().baseUri(base).contentType(ContentType.JSON)
                .body(Map.of("username", "first", "email", "same@test.local", "password", "secret1"))
                .post("/api/auth/register").then().statusCode(201);
        given().baseUri(base).contentType(ContentType.JSON)
                .body(Map.of("username", "second", "email", "same@test.local", "password", "secret1"))
                .post("/api/auth/register").then().statusCode(409);
    }
}
