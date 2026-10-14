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

public class Bug103Test {
    private final String base = ShopConfig.baseUrl();

    @Test
    @DisplayName("Поиск не зависит от регистра букв")
    void searchIsCaseInsensitive() {
        List<String> lower = given().baseUri(base).queryParam("search", "бургер")
                .get("/api/products").then().statusCode(200).extract().path("content.name");
        List<String> capital = given().baseUri(base).queryParam("search", "Бургер")
                .get("/api/products").then().statusCode(200).extract().path("content.name");
        assertThat(lower).isNotEmpty().containsExactlyInAnyOrderElementsOf(capital);
    }
}
