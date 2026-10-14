---
title: "9.9 Загрузка и скачивание файлов, проверка PDF"
minutes: 40
---

## Загрузка файла (multipart)

Файлы на сервер отправляют в формате **`multipart/form-data`** — тело запроса состоит из частей, каждая со своим именем и типом. Так работают HTML-формы с `<input type="file">`.

```java
Path file = tmp.resolve("menu.txt");
Files.writeString(file, "Классический бургер — 289.90", StandardCharsets.UTF_8);

String id = given().baseUri(ShopConfig.baseUrl())
        .multiPart("file", file.toFile(), "text/plain")
        .post("/api/files")
        .then().statusCode(201)
        .extract().path("id");
```

- `@TempDir Path tmp` (параметр теста) — JUnit создаёт временную папку и удаляет после теста (урок 5.8);
- **`multiPart("file", файл, тип)`** — добавить часть с именем `file` (имя поля должно совпадать с тем, что ждёт сервер — см. Swagger), сам файл и MIME-тип;
- `contentType` задавать не нужно — REST Assured поставит `multipart/form-data` сам;
- можно передать и байты: `multiPart("file", "menu.txt", bytes)`, и обычные поля формы: `multiPart("comment", "текст")`.

## Скачивание

```java
byte[] downloaded = given().baseUri(ShopConfig.baseUrl())
        .get("/api/files/" + id)
        .then().statusCode(200)
        .extract().asByteArray();

assertThat(new String(downloaded, StandardCharsets.UTF_8)).isEqualTo("Классический бургер — 289.90");
```

- `asByteArray()` — тело ответа как массив байтов (для любых файлов);
- для сохранения на диск: `Files.write(tmp.resolve("out.pdf"), bytes)`;
- полезно проверить заголовки: `.contentType("application/pdf")`, `.header("Content-Disposition", containsString("receipt-"))` — так сервер сообщает имя файла.

## Проверка содержимого PDF

Скачать файл мало — нужно проверить, что **внутри** правильные данные. Для PDF — библиотека **Apache PDFBox**:

```xml
<dependency>
    <groupId>org.apache.pdfbox</groupId>
    <artifactId>pdfbox</artifactId>
    <scope>test</scope>
</dependency>
```

```java
try (PDDocument doc = Loader.loadPDF(pdf)) {
    String text = new PDFTextStripper().getText(doc);
    assertThat(text).contains("ИТОГО: 521.82");
}
```

- `Loader.loadPDF(bytes)` — открыть PDF из массива байтов (PDFBox 3.x; в старой версии 2.x был `PDDocument.load(...)`);
- `PDFTextStripper().getText(doc)` — извлечь весь текст;
- try-with-resources — документ нужно закрыть.

> ⚠️ Текст из PDF может отличаться от того, что видно глазами: переносы строк, лишние пробелы, порядок колонок таблицы. Проверяйте `contains` по ключевым фрагментам, а не равенство всего текста.

Полный тест для BUG-801:

```java
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
        assertThat(text).contains("ИТОГО: " + new BigDecimal(order.getString("total")).setScale(2).toPlainString());
    }
}
```

`setScale(2).toPlainString()` превращает `521.82` в строку `"521.82"` ровно с двумя знаками — так, как пишет сервер в чеке.

## Excel (XLSX)

Для Excel — **Apache POI** (`poi-ooxml`):

```java
try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(xlsx))) {
    Sheet sheet = wb.getSheetAt(0);
    int rows = sheet.getLastRowNum();                          // индекс последней строки (0 — заголовок)
    String status = sheet.getRow(1).getCell(1).getStringCellValue();
    double total = sheet.getRow(1).getCell(5).getNumericCellValue();
}
```

Строки и ячейки нумеруются с 0. `getLastRowNum()` возвращает **индекс** последней строки, а не количество (при заголовке и двух заказах — 2). Этим проверяется BUG-802 в Test Lab.

Для CSV хватит обычного чтения строк и `split(",")` (или библиотеки OpenCSV).

## Файлы в отчёте

Скачанный файл полезно приложить к отчёту, чтобы при падении было что открыть. В Allure (модуль 14): `Allure.addAttachment("receipt.pdf", "application/pdf", new ByteArrayInputStream(pdf), ".pdf")`.

```task
id: m09-t09
title: Файлы и PDF
module: exercises/m09-rest-assured
test: FilesApiTestCheck
file: exercises/m09-rest-assured/src/test/java/ru/qacourse/m09/tasks/FilesApiTest.java
solution: solutions/exercises/m09-rest-assured/src/test/java/ru/qacourse/m09/tasks/FilesApiTest.java
text: |
  1. Загрузить текстовый файл и скачать его обратно — содержимое совпадает.
  2. Заказ с SALE10 → скачать PDF-чек → в тексте «ИТОГО: <total заказа>».

  Проверка ловит BUG-801.
hint: |
  Импорты PDFBox: `org.apache.pdfbox.Loader`, `org.apache.pdfbox.pdmodel.PDDocument`, `org.apache.pdfbox.text.PDFTextStripper`.
  Временная папка: параметр теста `@TempDir Path tmp`.
```

```quiz
- q: В каком формате загружают файлы через HTTP?
  options: ["multipart/form-data", "application/json", "text/plain"]
  answer: 0
- q: Как получить тело ответа как файл в памяти?
  options: [".extract().asByteArray()", ".extract().path(\"file\")", ".body().file()"]
  answer: 0
- q: Чем проверить текст внутри PDF?
  options: ["Apache PDFBox (PDFTextStripper)", "Hamcrest", "Jackson"]
  answer: 0
- q: Что вернёт getLastRowNum() для листа с заголовком и двумя строками данных?
  options: ["2", "3", "1"]
  answer: 0
```
