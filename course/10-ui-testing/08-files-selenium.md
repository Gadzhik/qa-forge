---
title: "10.8 Загрузка и скачивание файлов в Selenium"
minutes: 30
---

## Загрузка (upload)

Окно выбора файла — это окно **операционной системы**, Selenium не может им управлять. Но и не нужно: у поля `<input type="file">` можно просто «напечатать» путь к файлу.

```java
@Test
void uploadFile(@TempDir Path tmp) throws Exception {
    Path file = tmp.resolve("order.txt");
    Files.writeString(file, "Бургер x2", StandardCharsets.UTF_8);

    driver.get(ShopConfig.baseUrl() + "/playground/upload");
    driver.findElement(By.cssSelector("[data-testid=file-input]")).sendKeys(file.toAbsolutePath().toString());
    driver.findElement(By.cssSelector("[data-testid=upload-button]")).click();

    wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid=uploaded-name]")));
    assertThat(driver.findElement(By.cssSelector("[data-testid=uploaded-name]")).getText()).isEqualTo("order.txt");
}
```

- файл создаём во временной папке `@TempDir` (урок 5.8) — тест независим от содержимого диска;
- **`sendKeys(абсолютный путь)`** в `input[type=file]` — браузер считает, что пользователь выбрал файл;
- путь — **абсолютный** (`toAbsolutePath()`), относительный браузер не поймёт;
- `Path` даёт правильный разделитель для любой ОС;
- после отправки формы загружается новая страница — **ждём** результат (урок 10.5). Без ожидания мы получили `NoSuchElementException`, когда готовили это задание.

> 💡 Поле `input[type=file]` часто **спрятано** дизайнерами под красивую кнопку (`display:none` или нулевой размер). `sendKeys` в скрытое поле обычно всё равно работает; если нет — сделайте его видимым JavaScript'ом: `js.executeScript("arguments[0].style.display='block'", input)`.

> ⚠️ При удалённом запуске (Selenium Grid, урок 15.4) файл находится на **вашей** машине, а браузер — на другой. Нужно `((RemoteWebDriver) driver).setFileDetector(new LocalFileDetector())` — Selenium сам передаст файл.

## Скачивание (download)

Скачанный браузером файл попадает в папку загрузок — Selenium о ней ничего не знает. Варианты:

**1. Настроить папку загрузок Chrome** и ждать появления файла:

```java
Map<String, Object> prefs = Map.of(
        "download.default_directory", downloadDir.toAbsolutePath().toString(),
        "download.prompt_for_download", false);
options.setExperimentalOption("prefs", prefs);
...
link.click();
await().atMost(Duration.ofSeconds(10)).until(() -> Files.exists(downloadDir.resolve("menu.pdf")));
```

Громоздко и капризно (имена файлов, недокачанные `.crdownload`, удалённые браузеры).

**2. Взять ссылку и скачать HTTP-клиентом** — надёжнее:

```java
String href = driver.findElement(By.cssSelector("[data-testid=download-pdf]")).getAttribute("href");
byte[] pdf = given().get(href).then().statusCode(200).extract().asByteArray();
```

Если файл доступен только авторизованному пользователю, перенесите cookies браузера в запрос: `given().cookie("token", driver.manage().getCookieNamed("token").getValue())`.

**3. Selenide** — `$("a").download()` одной строкой (урок 10.12). Именно поэтому скачивание в задании — на Selenide.

```task
id: m10-t08
title: Загрузка файла
module: exercises/m10-ui
test: UploadTestCheck
file: exercises/m10-ui/src/test/java/ru/qacourse/m10/tasks/UploadTest.java
solution: solutions/exercises/m10-ui/src/test/java/ru/qacourse/m10/tasks/UploadTest.java
text: |
  Создайте во временной папке `order.txt` с текстом «Бургер x2», загрузите через `/playground/upload`
  и проверьте имя и содержимое на странице результата.
hint: |
  `sendKeys(file.toAbsolutePath().toString())` в `[data-testid=file-input]`, затем клик по кнопке и ожидание `[data-testid=uploaded-name]`.
```

```quiz
- q: Как загрузить файл через input type=file в Selenium?
  options: ["sendKeys(абсолютный путь к файлу)", "Кликнуть и выбрать файл в окне ОС", "Это невозможно"]
  answer: 0
- q: Почему путь должен быть абсолютным?
  options: ["Браузер не знает рабочую папку теста", "Так короче", "Это не обязательно"]
  answer: 0
- q: Какой способ скачивания в Selenium надёжнее?
  options: ["Взять href и скачать HTTP-клиентом", "Ждать файл в папке загрузок по Thread.sleep", "Сделать скриншот"]
  answer: 0
```
