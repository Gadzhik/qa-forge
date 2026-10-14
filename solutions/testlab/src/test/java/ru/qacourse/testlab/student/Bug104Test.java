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

public class Bug104Test {
    private final String base = ShopConfig.baseUrl();

    @Test
    @DisplayName("Несуществующий товар — 404")
    void unknownProductIs404() {
        given().baseUri(base).get("/api/products/999999").then().statusCode(404);
    }
}
