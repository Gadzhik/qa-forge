package ru.qacourse.m09.tasks;

import org.junit.jupiter.api.Test;
import ru.qacourse.m09.Specs;
import ru.qacourse.support.ShopConfig;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.*;

/**
 * Задание 9.9: файлы.
 * 1) Загрузить текстовый файл (POST /api/files, multipart, поле file) → 201, получить id;
 *    скачать GET /api/files/{id} → содержимое совпадает с загруженным.
 * 2) Оформить заказ с промокодом SALE10 (user), скачать чек GET /api/orders/{id}/receipt.pdf,
 *    извлечь текст PDFBox'ом и проверить, что в нём "ИТОГО: <total из заказа>".
 */
public class FilesApiTest {

    @Test
    void files() {
        fail("TODO: напишите тест");
    }
}
