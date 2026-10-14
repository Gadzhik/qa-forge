---
title: "9.8 Обёртка над ответом и рефакторинг тестов"
minutes: 40
---

## Зачем оборачивать

Посмотрите на типичный «вырастающий» набор API-тестов:

```java
given(Specs.auth("user", "user123"))
        .body(Map.of("productId", 1, "quantity", 0))
        .post("/api/cart/items")
        .then()
        .statusCode(400)
        .body("code", equalTo("BAD_QUANTITY"));
```

Через 200 тестов: одна и та же цепочка «400 + code» повторяется 80 раз; если формат ошибки изменится (`code` → `errorCode`), править 80 мест. А ещё хочется проверок с понятными именами.

Решение — **обёртка** (wrapper): свой класс вокруг `ValidatableResponse` с методами на языке предметной области.

## AssertableResponse

```java
public class AssertableResponse {
    private final ValidatableResponse response;

    public AssertableResponse(ValidatableResponse response) {
        this.response = response;
    }

    public AssertableResponse shouldHaveStatus(int status) {
        response.statusCode(status);
        return this;
    }

    public AssertableResponse shouldHaveField(String jsonPath, Matcher<?> matcher) {
        response.body(jsonPath, matcher);
        return this;
    }

    public AssertableResponse shouldHaveError(int status, String code) {
        return shouldHaveStatus(status).shouldHaveField("code", equalTo(code));
    }

    public <T> T asPojo(Class<T> type) {
        return response.extract().as(type);
    }
}
```

- поле `response` — исходный ответ REST Assured; все проверки делегируются ему;
- **`return this`** — каждый метод возвращает сам объект, поэтому вызовы цепляются (**fluent interface**, как у AssertJ и билдеров);
- `shouldHaveError` построен из двух других — правило формата ошибки записано в одном месте;
- `<T> T asPojo(Class<T> type)` — обобщённый метод (урок 4.9): вернёт объект того класса, который передали.

Использование:

```java
ErrorResponse error = new AssertableResponse(given(user).body(...).post("/api/cart/items").then())
        .shouldHaveError(400, "BAD_QUANTITY")
        .asPojo(ErrorResponse.class);
assertThat(error.message()).contains("больше 0");
```

## Свои «условия» (Condition)

Следующий шаг — проверки как объекты, по аналогии с Selenide (модуль 10):

```java
public interface Condition {
    void check(ValidatableResponse response);
}

public class Conditions {
    public static Condition statusCode(int code) {
        return r -> r.statusCode(code);
    }
    public static Condition field(String path, Matcher<?> matcher) {
        return r -> r.body(path, matcher);
    }
}

public AssertableResponse shouldHave(Condition condition) {
    condition.check(response);
    return this;
}
```

```java
response.shouldHave(statusCode(200)).shouldHave(field("name", equalTo("Кола 0.5")));
```

Каждое условие — лямбда (урок 5.4). Новое условие добавляется без изменения обёртки.

## Рефакторинг: из 30 строк в 5

**До** — всё внутри теста:

```java
@Test
void orderWithPromo() {
    String token = given().baseUri(URL).contentType(JSON)
            .body("{\"username\":\"user\",\"password\":\"user123\"}")
            .post("/api/auth/login").then().statusCode(200).extract().path("token");
    given().baseUri(URL).contentType(JSON).header("Authorization", "Bearer " + token)
            .delete("/api/cart").then().statusCode(204);
    given().baseUri(URL).contentType(JSON).header("Authorization", "Bearer " + token)
            .body("{\"productId\":1,\"quantity\":2}").post("/api/cart/items").then().statusCode(200);
    Response r = given().baseUri(URL).contentType(JSON).header("Authorization", "Bearer " + token)
            .body("{\"promoCode\":\"SALE10\"}").post("/api/orders");
    r.then().statusCode(201);
    assertThat(r.jsonPath().getString("status")).isEqualTo("NEW");
    assertThat(new BigDecimal(r.jsonPath().getString("total"))).isEqualByComparingTo("521.82");
}
```

**После** — слои:

```java
@Test
void orderWithPromo() {
    CartClient cart = new CartClient(Specs.auth("user", "user123"));
    cart.clear();
    cart.add(BURGER, 2);

    OrderDto order = new OrderClient(cart.spec()).create("SALE10");

    assertThat(order.status()).isEqualTo("NEW");
    assertThat(order.total()).isEqualByComparingTo("521.82");
}
```

- **клиенты** (`CartClient`, `OrderClient`) прячут HTTP: метод = одна ручка API;
- **модели** (`OrderDto`) — типизированные ответы;
- **константы** тестовых данных (`BURGER = 1`);
- тест описывает **сценарий**, а не HTTP.

Это архитектура тестового фреймворка, которую мы целиком соберём в модуле 14. Правило рефакторинга: делайте его **маленькими шагами**, запуская тесты после каждого шага — они ваша страховка.

```task
id: m09-t08
title: Обёртка AssertableResponse
module: exercises/m09-rest-assured
test: AssertableResponseCheck
file: exercises/m09-rest-assured/src/main/java/ru/qacourse/m09/AssertableResponse.java
solution: solutions/exercises/m09-rest-assured/src/main/java/ru/qacourse/m09/AssertableResponse.java
text: |
  Реализуйте методы `AssertableResponse`: `shouldHaveStatus`, `shouldHaveField`, `shouldHaveError` (через два первых), `asPojo`.
  Каждый метод-проверка должен возвращать `this`.

  Проверка запустит тест-образец `checks/WrapperSample.java`, который пользуется вашей обёрткой.
hint: |
  Внутри — вызовы `response.statusCode(...)`, `response.body(...)`, `response.extract().as(...)`.
```

```quiz
- q: Зачем методы обёртки возвращают this?
  options: ["Чтобы проверки можно было вызывать цепочкой", "Так требует REST Assured", "Для скорости"]
  answer: 0
- q: Какая главная выгода обёртки над ответом?
  options:
    - "Общие правила проверок (формат ошибки и т.п.) записаны в одном месте, тесты читаемее"
    - "Тесты работают без сети"
    - "Не нужен REST Assured"
  answer: 0
- q: Что такое «клиент» в архитектуре API-тестов?
  options: ["Класс, методы которого соответствуют ручкам API и прячут HTTP", "Браузер", "Пользователь"]
  answer: 0
- q: Как правильно рефакторить тесты?
  options: ["Маленькими шагами, запуская тесты после каждого", "Переписать всё за раз", "Без запуска тестов"]
  answer: 0
```
