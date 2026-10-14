package ru.qacourse.shop.soap;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.w3c.dom.Document;
import ru.qacourse.shop.bugs.Bug;
import ru.qacourse.shop.bugs.BugRegistry;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

/**
 * Простейший SOAP-сервис расчёта доставки (без фреймворков, чтобы было видно "внутренности").
 * WSDL: GET /ws/delivery.wsdl. Запрос: POST /ws/delivery, Content-Type: text/xml.
 * Тариф: 99 ₽ + 25 ₽ за км; бесплатно, если сумма заказа от 1000 ₽.
 */
@Hidden
@RestController
public class DeliverySoapController {
    private static final String NS = "http://qaburger.local/delivery";
    private final BugRegistry bugs;

    public DeliverySoapController(BugRegistry bugs) {
        this.bugs = bugs;
    }

    @GetMapping(value = "/ws/delivery.wsdl", produces = MediaType.TEXT_XML_VALUE)
    public String wsdl() throws IOException {
        return new ClassPathResource("soap/delivery.wsdl").getContentAsString(StandardCharsets.UTF_8);
    }

    @PostMapping(value = "/ws/delivery", produces = MediaType.TEXT_XML_VALUE)
    public ResponseEntity<String> calculate(@RequestBody String body) {
        try {
            DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
            f.setNamespaceAware(true);
            f.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            Document doc = f.newDocumentBuilder().parse(new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)));
            String km = doc.getElementsByTagNameNS(NS, "distanceKm").item(0).getTextContent().trim();
            String amount = doc.getElementsByTagNameNS(NS, "orderAmount").item(0).getTextContent().trim();
            BigDecimal distance = new BigDecimal(km);
            BigDecimal orderAmount = new BigDecimal(amount);
            if (distance.signum() < 0) return fault("Client", "distanceKm не может быть отрицательным");
            BigDecimal cost;
            if (orderAmount.compareTo(new BigDecimal("1000")) >= 0) {
                cost = BigDecimal.ZERO;
            } else if (distance.signum() == 0 && bugs.isOn(Bug.BUG_902)) {
                cost = new BigDecimal("-50");
            } else {
                cost = new BigDecimal("99").add(distance.multiply(new BigDecimal("25")));
            }
            return ResponseEntity.ok(envelope("""
                    <d:CalculateDeliveryResponse xmlns:d="%s">
                      <d:cost>%s</d:cost>
                      <d:currency>RUB</d:currency>
                    </d:CalculateDeliveryResponse>""".formatted(NS, cost.setScale(2).toPlainString())));
        } catch (Exception e) {
            return fault("Client", "Некорректный запрос: ожидаются distanceKm и orderAmount");
        }
    }

    private ResponseEntity<String> fault(String code, String message) {
        return ResponseEntity.status(500).contentType(MediaType.TEXT_XML).body(envelope("""
                <soap:Fault>
                  <faultcode>soap:%s</faultcode>
                  <faultstring>%s</faultstring>
                </soap:Fault>""".formatted(code, message)));
    }

    private static String envelope(String body) {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
                  <soap:Body>
                %s
                  </soap:Body>
                </soap:Envelope>""".formatted(body);
    }
}
