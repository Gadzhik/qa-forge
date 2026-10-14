---
title: "10.12 Скачивание в Selenide, чтение PDF и XLSX"
minutes: 35
---

## download()

```java
File file = $("[data-testid=download-pdf]").download();
```

Одна строка: Selenide кликает по ссылке, дожидается скачивания и возвращает `File`. Под капотом — несколько стратегий (`Configuration.fileDownload`):

| Режим | Как работает | Когда |
|---|---|---|
| `HTTPGET` (по умолчанию) | берёт `href` ссылки и скачивает HTTP-запросом **с cookies браузера** | обычные ссылки на файлы |
| `FOLDER` | настраивает папку загрузок браузера и ждёт там файл | файл формируется JavaScript'ом, нет href |
| `PROXY` | пропускает трафик браузера через свой прокси и перехватывает файл | сложные случаи |
| `CDP` | через Chrome DevTools Protocol | Chromium-браузеры, в том числе удалённые |

Для кнопок без `href` используйте `download(DownloadOptions.using(FileDownloadMode.FOLDER))`. Файлы складываются в `build/downloads`.

Фильтр и таймаут: `download(DownloadOptions.using(FOLDER).withFilter(withExtension("pdf")).withTimeout(10000))`.

## Текстовый файл

```java
File file = $("[data-testid=download-txt]").download();
assertThat(Files.readString(file.toPath(), StandardCharsets.UTF_8)).contains("Этот файл скачан автотестом");
```

## PDF

```java
File file = $("[data-testid=download-pdf]").download();
try (PDDocument doc = Loader.loadPDF(file)) {
    assertThat(new PDFTextStripper().getText(doc)).contains("ИТОГО: 708.80");
}
```

PDFBox (урок 9.9). `Loader.loadPDF` принимает и `File`, и `byte[]`.

Что ещё можно проверить в PDF: количество страниц (`doc.getNumberOfPages()`), метаданные (`doc.getDocumentInformation().getTitle()`), текст на конкретной странице (`stripper.setStartPage(1); setEndPage(1)`).

## XLSX

```java
File file = $("[data-testid=download-xlsx]").download();
try (XSSFWorkbook wb = new XSSFWorkbook(file)) {
    Sheet sheet = wb.getSheetAt(0);
    assertThat(sheet.getRow(0).getCell(0).getStringCellValue()).isEqualTo("id");
    assertThat(sheet.getRow(1).getCell(5).getNumericCellValue()).isEqualTo(708.8);
}
```

Apache POI:
- `XSSFWorkbook` — книга формата `.xlsx` (для старого `.xls` — `HSSFWorkbook`);
- `getSheetAt(0)` — первый лист; `getSheet("Orders")` — по имени;
- `getRow(i).getCell(j)` — ячейка (нумерация с 0: колонка A = 0, F = 5);
- `getStringCellValue()` / `getNumericCellValue()` / `getBooleanCellValue()` — значение нужного **типа**. Вызов не того метода бросит `IllegalStateException: Cannot get a STRING value from a NUMERIC cell`. Универсально — `new DataFormatter().formatCellValue(cell)` вернёт то, что видно в Excel, строкой.

## CSV

```java
List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
String[] header = lines.get(0).split(";");
```

Для настоящих CSV с кавычками и запятыми внутри значений берут библиотеку OpenCSV.

## ZIP

```java
try (ZipInputStream zip = new ZipInputStream(new FileInputStream(file))) {
    ZipEntry entry;
    while ((entry = zip.getNextEntry()) != null) {
        System.out.println(entry.getName());
    }
}
```

## Что проверять в выгрузках

- файл скачивается, тип и имя правильные (`Content-Disposition`);
- данные в файле **совпадают** с тем, что в системе (API/БД) — классический баг: в выгрузке старые данные или не все строки (BUG-802);
- форматы: даты, суммы с копейками, кодировка русских букв (откройте файл глазами хотя бы раз).

```task
id: m10-t12
title: Скачать и прочитать TXT, PDF, XLSX
module: exercises/m10-ui
test: DownloadFilesTestCheck
file: exercises/m10-ui/src/test/java/ru/qacourse/m10/tasks/DownloadFilesTest.java
solution: solutions/exercises/m10-ui/src/test/java/ru/qacourse/m10/tasks/DownloadFilesTest.java
text: |
  На `/playground/download` скачайте три файла через `download()` и проверьте содержимое: текст, «ИТОГО: 708.80» в PDF,
  заголовок «id» и сумма 708.8 в XLSX.
hint: |
  Импорты PDFBox и POI нужно добавить. Для XLSX: `new XSSFWorkbook(file)`; ячейка F2 — `getRow(1).getCell(5)`.
```

```quiz
- q: Как работает download() в режиме HTTPGET?
  options: ["Берёт href ссылки и скачивает HTTP-запросом с cookies браузера", "Делает скриншот", "Ждёт файл в папке загрузок"]
  answer: 0
- q: Какой класс POI открывает .xlsx?
  options: ["XSSFWorkbook", "HSSFWorkbook", "PDDocument"]
  answer: 0
- q: "Ошибка Cannot get a STRING value from a NUMERIC cell означает..."
  options: ["Вызван не тот метод для типа ячейки", "Файл повреждён", "Нет листа"]
  answer: 0
- q: Какой номер у колонки F в POI?
  options: ["5", "6", "F"]
  answer: 0
```
