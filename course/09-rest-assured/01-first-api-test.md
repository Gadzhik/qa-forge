---
title: "9.1 REST Assured: первый API-тест"
minutes: 45
---

**REST Assured** — самая популярная Java-библиотека для API-тестов. Она превращает HTTP-запрос и проверку ответа в одну читаемую цепочку.

## Подключение

```xml
<dependency>
    <groupId>io.rest-assured</groupId>
    <artifactId>rest-assured</artifactId>
    <version>5.5.7</version>
    <scope>test</scope>
</dependency>
```

В модуле `exercises/m09-rest-assured` уже подключено (версия берётся из родителя). Gradle: `testImplementation 'io.rest-assured:rest-assured:5.5.7'`.

## Перед стартом

Тесты модуля 9 работают против **запущенного QA Burger**. Запустите курс (`start.sh` / `start.cmd`) и убедитесь, что http://localhost:8081 открывается. Адрес тесты берут из `ShopConfig.baseUrl()` (по умолчанию `http://localhost:8081`). Когда вы нажимаете «Проверить», проверка поднимает свой экземпляр приложения на другом порту и подставляет его адрес.

> 💡 Если вы экспериментировали с багами в «Тренажёре», выключите их и нажмите «Сбросить данные» — иначе тесты могут упасть по «правильной» причине.

## Структура given / when / then

```java
@Test
void getProduct() {
    given()
            .baseUri(ShopConfig.baseUrl())
    .when()
            .get("/api/products/1")
    .then()
            .statusCode(200)
            .body("name", equalTo("Классический бургер"))
            .body("category", equalTo("BURGER"))
            .body("price", greaterThan(0f));
}
```

Цепочка читается как тест-кейс (урок 1.4):

| Часть | Значение | Что настраиваем |
|---|---|---|
| `given()` | **Дано** — подготовка запроса | адрес, заголовки, параметры, тело, авторизация |
| `.when()` | **Когда** — действие | метод и путь: `get`, `post`, `put`, `delete` |
| `.then()` | **Тогда** — проверки ответа | статус, тело, заголовки |

`when()` — необязательное «украшение» для читаемости: `given().get(...)` работает так же.

Импорты:

```java
import static io.restassured.RestAssured.given;   // точка входа
import static org.hamcrest.Matchers.*;             // equalTo, greaterThan, hasSize...
```

### Разбор по строкам

- **`given()`** — статический метод REST Assured, начинает описание запроса. Возвращает объект `RequestSpecification`.
- **`.baseUri(ShopConfig.baseUrl())`** — базовый адрес сервера; путь в `get` к нему приклеится.
- **`.get("/api/products/1")`** — отправить GET и получить ответ. Именно здесь происходит сетевой запрос.
- **`.then()`** — перейти к проверкам; возвращает `ValidatableResponse`.
- **`.statusCode(200)`** — код должен быть 200. Если нет — тест упадёт с `AssertionError: Expected status code <200> but was <404>`.
- **`.body("name", equalTo("Классический бургер"))`** — в JSON-ответе поле `name` должно равняться строке. Первый аргумент — **путь** в JSON (GPath, урок 9.2), второй — **матчер** Hamcrest.
- **`.body("price", greaterThan(0f))`** — цена больше нуля. Почему `0f`? REST Assured читает дробные числа JSON как `Float`, и сравнивать нужно с тем же типом (`greaterThan(0)` — `Integer` — не сработает). Это частая ловушка; надёжнее для денег — извлечь значение и сравнить как `BigDecimal` (урок 9.2).

## Матчеры Hamcrest

**Hamcrest** — библиотека «сравнивателей», которую REST Assured использует внутри `body(...)`:

| Матчер | Значение |
|---|---|
| `equalTo(x)` | равно |
| `not(equalTo(x))` | не равно |
| `notNullValue()` / `nullValue()` | есть / null |
| `greaterThan(x)`, `lessThanOrEqualTo(x)` | больше, меньше или равно |
| `containsString("бург")` | строка содержит |
| `startsWith`, `endsWith`, `matchesPattern(regex)` | начало, конец, шаблон |
| `hasSize(n)` | размер списка |
| `hasItem(x)`, `hasItems(x, y)` | список содержит |
| `everyItem(матчер)` | каждый элемент удовлетворяет |
| `empty()`, `not(empty())` | пустой / не пустой список |
| `hasKey("x")` | у объекта есть поле |
| `anyOf(...)`, `allOf(...)` | или / и |

```java
.then()
        .body("content", hasSize(5))
        .body("content.category", everyItem(equalTo("DRINK")))
        .body("content.name", hasItem("Кола 0.5"))
        .body("$", hasKey("totalElements"));
```

`$` — корень JSON.

## Негативный тест

```java
@Test
void unknownProductIs404() {
    given()
            .baseUri(ShopConfig.baseUrl())
    .when()
            .get("/api/products/999999")
    .then()
            .statusCode(404)
            .body("code", equalTo("NOT_FOUND"));
}
```

Проверяем и код, и машиночитаемый `code` ошибки (урок 8.3). При BUG-104 сервер вернёт 500, и тест это поймает.

## Если тест упал — что смотреть

Добавьте **логирование**:

```java
given().log().all()            // напечатать весь запрос
        ...
.then().log().all()            // напечатать весь ответ
        ...
```

Или только при ошибке: `.then().log().ifValidationFails()`. В консоли будут метод, URL, заголовки, тело запроса и ответа — как `curl -v`. Подробнее — урок 9.5.

> ⚠️ **Частые ошибки первых тестов:**
> - `Connection refused` — QA Burger не запущен;
> - `Expected status code <200> but was <401>` — забыли токен;
> - `Expected status code <201> but was <415>` — забыли `contentType(ContentType.JSON)` у POST;
> - `JSON path name doesn't match. Expected: "Кола" Actual: "Кола 0.5"` — читайте Actual, это реальность.

```task
id: m09-t01
title: Первые API-тесты
module: exercises/m09-rest-assured
test: ProductApiTestCheck
file: exercises/m09-rest-assured/src/test/java/ru/qacourse/m09/tasks/ProductApiTest.java
solution: solutions/exercises/m09-rest-assured/src/test/java/ru/qacourse/m09/tasks/ProductApiTest.java
text: |
  В `ProductApiTest` напишите два теста:
  1. `GET /api/products/1` → 200, `name` = «Классический бургер», `category` = `BURGER`, `price` > 0;
  2. `GET /api/products/999999` → 404, `code` = `NOT_FOUND`.

  Сначала запустите тесты из IDEA против работающего QA Burger, потом нажмите «Проверить» — проверка убедится, что тест ловит BUG-104.
hint: |
  Код обоих тестов разобран в уроке. Не забудьте удалить строку `fail("TODO...")`.
```

```quiz
- q: Что настраивают в given()?
  options: ["Запрос: адрес, заголовки, параметры, тело", "Проверки ответа", "Только метод"]
  answer: 0
- q: Почему в .body("price", greaterThan(0f)) стоит 0f, а не 0?
  options:
    - "REST Assured читает дробные числа JSON как Float — сравнивать нужно с тем же типом"
    - "Опечатка"
    - "0f быстрее"
  answer: 0
- q: Как напечатать запрос и ответ, только если проверка упала?
  options: [".then().log().ifValidationFails()", ".log().never()", ".debug()"]
  answer: 0
- q: "Тест упал: Expected status code <200> but was <401>. Скорее всего..."
  options: ["Не передан токен авторизации", "Сервер не запущен", "Неверный JSON-путь"]
  answer: 0
- q: Что проверяет body("content.category", everyItem(equalTo("DRINK")))?
  options: ["Что у каждого товара в content категория DRINK", "Что первый товар — DRINK", "Что есть хотя бы один DRINK"]
  answer: 0
```
