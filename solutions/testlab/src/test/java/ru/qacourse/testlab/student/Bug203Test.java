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

public class Bug203Test {
    private final String base = ShopConfig.baseUrl();

    @Test
    @DisplayName("Пароль в другом регистре не подходит")
    void passwordIsCaseSensitive() {
        given().baseUri(base).contentType(ContentType.JSON)
                .body(Map.of("username", "casey", "email", "casey@test.local", "password", "Secret1"))
                .post("/api/auth/register").then().statusCode(201);
        given().baseUri(base).contentType(ContentType.JSON)
                .body(Map.of("username", "casey", "password", "SECRET1"))
                .post("/api/auth/login").then().statusCode(401);
    }
}
