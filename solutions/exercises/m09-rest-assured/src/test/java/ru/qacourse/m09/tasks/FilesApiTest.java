package ru.qacourse.m09.tasks;

import org.junit.jupiter.api.Test;
import ru.qacourse.m09.Specs;
import ru.qacourse.support.ShopConfig;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.*;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Задание 9.9: файлы.
 * 1) Загрузить текстовый файл (POST /api/files, multipart, поле file) → 201, получить id;
 *    скачать GET /api/files/{id} → содержимое совпадает с загруженным.
 * 2) Оформить заказ с промокодом SALE10 (user), скачать чек GET /api/orders/{id}/receipt.pdf,
 *    извлечь текст PDFBox'ом и проверить, что в нём "ИТОГО: <total из заказа>".
 */
public class FilesApiTest {

    @Test
    void uploadAndDownload(@TempDir Path tmp) throws Exception {
        Path file = tmp.resolve("menu.txt");
        Files.writeString(file, "Классический бургер — 289.90", StandardCharsets.UTF_8);

        String id = given().baseUri(ShopConfig.baseUrl())
                .multiPart("file", file.toFile(), "text/plain")
                .post("/api/files")
                .then().statusCode(201)
                .extract().path("id");

        byte[] downloaded = given().baseUri(ShopConfig.baseUrl())
                .get("/api/files/" + id)
                .then().statusCode(200)
                .extract().asByteArray();

        assertThat(new String(downloaded, StandardCharsets.UTF_8)).isEqualTo("Классический бургер — 289.90");
    }

    @Test
    void receiptHasDiscountedTotal() throws Exception {
        var user = Specs.auth("user", "user123");
        given(user).body(Map.of("productId", 1, "quantity", 2)).post("/api/cart/items").then().statusCode(200);
        var order = given(user).body(Map.of("promoCode", "SALE10")).post("/api/orders")
                .then().statusCode(201).extract().jsonPath();

        byte[] pdf = given(user).get("/api/orders/" + order.getInt("id") + "/receipt.pdf")
                .then().statusCode(200).contentType("application/pdf")
                .extract().asByteArray();

        try (PDDocument doc = Loader.loadPDF(pdf)) {
            String text = new PDFTextStripper().getText(doc);
            assertThat(text).contains("ИТОГО: " + new java.math.BigDecimal(order.getString("total")).setScale(2).toPlainString());
        }
    }
}
