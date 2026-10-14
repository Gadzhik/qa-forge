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

public class Bug902Test {
    private final String base = ShopConfig.baseUrl();

    @Test
    @DisplayName("Доставка на 0 км не может стоить меньше нуля")
    void zeroDistanceNotNegative() {
        String xml = """
                <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/" xmlns:d="http://qaburger.local/delivery">
                  <soap:Body>
                    <d:CalculateDeliveryRequest>
                      <d:distanceKm>0</d:distanceKm>
                      <d:orderAmount>500</d:orderAmount>
                    </d:CalculateDeliveryRequest>
                  </soap:Body>
                </soap:Envelope>""";
        String cost = given().baseUri(base).contentType("text/xml; charset=UTF-8").body(xml)
                .post("/ws/delivery").then().statusCode(200)
                .extract().xmlPath().getString("Envelope.Body.CalculateDeliveryResponse.cost");
        assertThat(new BigDecimal(cost)).isGreaterThanOrEqualTo(BigDecimal.ZERO);
    }
}
