package ru.qacourse.m09.checks;

import org.junit.jupiter.api.Test;
import ru.qacourse.m09.AssertableResponse;
import ru.qacourse.m09.Specs;
import ru.qacourse.m09.models.ErrorResponse;
import ru.qacourse.m09.models.ProductDto;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.equalTo;

/** Тест-образец, использующий вашу обёртку. Не изменяйте. */
public class WrapperSample {

    @Test
    void positive() {
        ProductDto cola = new AssertableResponse(given(Specs.base()).get("/api/products/12").then())
                .shouldHaveStatus(200)
                .shouldHaveField("name", equalTo("Кола 0.5"))
                .asPojo(ProductDto.class);
        assertThat(cola.price()).isEqualByComparingTo("99.00");
    }

    @Test
    void error() {
        ErrorResponse error = new AssertableResponse(given(Specs.auth("user", "user123"))
                .body(Map.of("productId", 1, "quantity", 0)).post("/api/cart/items").then())
                .shouldHaveError(400, "BAD_QUANTITY")
                .asPojo(ErrorResponse.class);
        assertThat(error.message()).contains("больше 0");
    }

    @Test
    void wrapperReallyFails() {
        assertThatThrownBy(() -> new AssertableResponse(given(Specs.base()).get("/api/products/12").then())
                .shouldHaveStatus(404)).isInstanceOf(AssertionError.class);
        assertThatThrownBy(() -> new AssertableResponse(given(Specs.base()).get("/api/products/12").then())
                .shouldHaveField("name", equalTo("Пиво"))).isInstanceOf(AssertionError.class);
    }
}
