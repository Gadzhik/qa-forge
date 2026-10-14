---
title: "9.5 Спецификации, логирование и фильтры"
minutes: 45
---

## Проблема

В каждом тесте повторяется одно и то же: `.baseUri(...)`, `.contentType(JSON)`, `.header("Authorization", ...)`, `.statusCode(200)`. Повторение — это и лишний код, и риск: поменялся адрес — правь 300 тестов.

## RequestSpecification

**Спецификация запроса** — заготовка с общими настройками. Откройте `exercises/m09-rest-assured/src/main/java/ru/qacourse/m09/Specs.java`:

```java
public static RequestSpecification base() {
    return new RequestSpecBuilder()
            .setBaseUri(ShopConfig.baseUrl())
            .setContentType(ContentType.JSON)
            .setAccept(ContentType.JSON)
            .log(LogDetail.METHOD)
            .log(LogDetail.URI)
            .build();
}
```

- `RequestSpecBuilder` — строитель спецификации; методы `set...`/`add...` задают настройки;
- `setBaseUri` — адрес сервера; есть ещё `setBasePath("/api")` — общая часть пути;
- `setContentType` / `setAccept` — формат тела запроса и желаемого ответа;
- `log(LogDetail.METHOD/URI)` — печатать метод и адрес каждого запроса (видно, что тест делает);
- `build()` — получить готовую `RequestSpecification`.

Использование:

```java
given(Specs.base()).get("/api/products/1");
given().spec(Specs.base()).get("/api/products/1");   // то же самое
```

### Спецификация с авторизацией

```java
public static RequestSpecification auth(String username, String password) {
    return new RequestSpecBuilder()
            .addRequestSpecification(base())
            .addHeader("Authorization", "Bearer " + token(username, password))
            .build();
}

public static String token(String username, String password) {
    return given(base())
            .body(new LoginRequest(username, password))
            .post("/api/auth/login")
            .then().statusCode(200)
            .extract().path("token");
}
```

- `addRequestSpecification(base())` — взять все настройки базовой и дополнить;
- `addHeader(...)` — заголовок с токеном;
- `token(...)` — вход через API и извлечение токена.

> 💡 **Почему спецификации — методы, а не `static final` поля?** Адрес QA Burger при проверке заданий каждый раз другой (новый порт). Поле посчитается один раз при загрузке класса и «запомнит» старый адрес. Метод читает адрес при каждом вызове. Это частая ошибка и в реальных проектах, когда стенд выбирается во время запуска.

> ⚠️ **Глобальные настройки** `RestAssured.baseURI = "..."`, `RestAssured.requestSpecification = ...` — тоже вариант, их часто ставят в `@BeforeAll`. Но это статическое изменяемое состояние (урок 4.3): при параллельном запуске тесты с разными настройками будут мешать друг другу. Явные спецификации надёжнее.

## ResponseSpecification

Общие проверки ответа тоже можно вынести:

```java
public static ResponseSpecification ok200() {
    return new ResponseSpecBuilder()
            .expectStatusCode(200)
            .expectContentType(ContentType.JSON)
            .build();
}
```

```java
given(user).get("/api/cart")
        .then().spec(Specs.ok200())
        .body("totalQuantity", equalTo(3));
```

## Подготовка в @BeforeEach

```java
public class CartApiTest {

    private RequestSpecification user;

    @BeforeEach
    void setUp() {
        user = Specs.auth("user", "user123");
        given(user).delete("/api/cart").then().statusCode(204);
    }

    private void add(int productId, int quantity) {
        given(user).body(Map.of("productId", productId, "quantity", quantity))
                .post("/api/cart/items")
                .then().statusCode(200);
    }

    @Test
    void sameProductIsSummed() {
        add(1, 2);
        add(1, 1);
        given(user).get("/api/cart")
                .then().spec(Specs.ok200())
                .body("items", hasSize(1))
                .body("items[0].quantity", equalTo(3));
    }
}
```

- перед каждым тестом — свежий токен и **пустая корзина** (тест не зависит от того, что осталось после предыдущего);
- `add(...)` — маленький метод-шаг: тест читается как сценарий. Такие шаги в модуле 14 переедут в отдельные классы-клиенты и получат аннотацию Allure `@Step`.

## Логирование

| Где | Что | Когда |
|---|---|---|
| `given().log().all()` | весь запрос | всегда |
| `given().log().body()` / `.headers()` / `.uri()` / `.method()` | часть запроса | всегда |
| `then().log().all()` | весь ответ | всегда |
| `then().log().ifValidationFails()` | весь ответ | только если проверка упала |
| `given().log().ifValidationFails()` | запрос | только если проверка упала |
| `RestAssured.enableLoggingOfRequestAndResponseIfValidationFails()` | оба | глобально при падении |

Разумная практика: логировать запрос и ответ **при ошибке** — и консоль чистая, и при падении всё видно.

> ⚠️ Логи попадают в CI и отчёты. Токены и пароли в них — утечка. REST Assured умеет скрывать заголовки: `.config(RestAssured.config().logConfig(LogConfig.logConfig().blacklistHeader("Authorization")))`.

## Фильтры

**Фильтр** — код, который видит каждый запрос и ответ: логирует, добавляет заголовки, прикладывает в отчёт. Через фильтр работает интеграция с Allure (модуль 14):

```java
given().filter(new AllureRestAssured())...
```

Свой фильтр — реализация интерфейса `Filter`:

```java
public class RequestIdFilter implements Filter {
    @Override
    public Response filter(FilterableRequestSpecification req, FilterableResponseSpecification res, FilterContext ctx) {
        req.header("X-Request-Id", UUID.randomUUID().toString());   // метка, по которой запрос найдут в логах сервера
        return ctx.next(req, res);                                  // передать дальше и вернуть ответ
    }
}
```

Фильтр добавляют в спецификацию: `.addFilter(new RequestIdFilter())`.

```task
id: m09-t05
title: Корзина на спецификациях
module: exercises/m09-rest-assured
test: CartApiTestCheck
file: exercises/m09-rest-assured/src/test/java/ru/qacourse/m09/tasks/CartApiTest.java
solution: solutions/exercises/m09-rest-assured/src/test/java/ru/qacourse/m09/tasks/CartApiTest.java
text: |
  В `CartApiTest`: спецификация пользователя и очистка корзины в `@BeforeEach`, два теста:
  - товар 1 добавлен дважды (2 + 1) → в корзине одна строка, количество 3;
  - 3 шт. товара 12 (99.00) → `total` = 297.0.

  Проверка ловит BUG-302 и BUG-304.
hint: |
  Код почти целиком разобран в уроке. `total` сравнивайте как `equalTo(297.0f)`.
```

```quiz
- q: Зачем нужны RequestSpecification?
  options:
    - "Вынести общие настройки запросов (адрес, формат, токен) в одно место"
    - "Чтобы ускорить сеть"
    - "Для проверки ответов"
  answer: 0
- q: Почему спецификация в курсе — метод, а не static final поле?
  options:
    - "Адрес приложения при проверках меняется, а поле запомнило бы старый"
    - "Поля в Java запрещены"
    - "Так короче"
  answer: 0
- q: Какое логирование разумно включать по умолчанию?
  options: ["При падении проверки (ifValidationFails)", "Всё и всегда", "Никакого"]
  answer: 0
- q: Чем опасен RestAssured.baseURI = ... в параллельных тестах?
  options:
    - "Это общее статическое состояние — тесты с разными настройками мешают друг другу"
    - "Ничем"
    - "Он не работает"
  answer: 0
- q: Что делает фильтр REST Assured?
  options: ["Перехватывает каждый запрос и ответ (логирование, заголовки, отчёт)", "Фильтрует JSON", "Удаляет тесты"]
  answer: 0
```
