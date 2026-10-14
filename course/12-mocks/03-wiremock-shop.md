---
title: "12.3 Подменяем платёжный сервис QA Burger"
minutes: 45
---

## Как QA Burger находит платёжку

В `shop-app/.../payment/PaymentClient.java` адрес берётся из настройки `shop.payment.url`. Если она пустая — используется встроенная заглушка самого приложения (`/fake-payment`, всегда «одобрено»). Мы направим QA Burger на WireMock.

Контракт платёжного сервиса:

```http
POST /api/payments
Content-Type: application/json

{"orderId": 1, "amount": 99.00, "currency": "RUB"}
```

```json
{"status": "APPROVED", "transactionId": "tx-1"}
```

`status` — `APPROVED` (одобрено) или `DECLINED` (отказ).

## Запуск QA Burger с WireMock

Остановите обычный запуск курса (Ctrl+C в окне start) и запустите QA Burger с параметром:

```tabs
Windows (PowerShell): |
  ~~~powershell
  java -jar shop-app\target\shop-app-1.0.0-exec.jar --shop.payment.url=http://localhost:8089
  ~~~
macOS / Linux: |
  ~~~bash
  java -jar shop-app/target/shop-app-1.0.0-exec.jar --shop.payment.url=http://localhost:8089
  ~~~
```

`--shop.payment.url=...` — параметр Spring Boot из командной строки: имеет наивысший приоритет над `application.yml` (урок 6.7).

Пока WireMock не запущен, оплата в магазине будет падать (`Connection refused`) — это и есть сценарий «платёжка недоступна». Проверка задания поднимает QA Burger с этим параметром сама.

## Тест

```java
static WireMockServer wm;
private RequestSpecification user;

@BeforeAll
static void startMock() {
    wm = new WireMockServer(wireMockConfig().port(ShopConfig.paymentMockPort()));
    wm.start();
}

@AfterAll
static void stopMock() {
    wm.stop();
}

@BeforeEach
void setUp() {
    wm.resetAll();
    String token = ... вход user / user123 ...;
    user = given().baseUri(ShopConfig.baseUrl()).contentType(ContentType.JSON).auth().oauth2(token);
}
```

- WireMock на **фиксированном** порту (`ShopConfig.paymentMockPort()`, по умолчанию 8089): QA Burger должен знать адрес заранее;
- `@BeforeAll` / `@AfterAll` — один сервер на весь класс (запуск занимает время);
- `resetAll()` перед каждым тестом — тесты не видят заглушки и запросы друг друга.

Вспомогательные шаги:

```java
private int newOrder() {
    given().spec(user).delete("/api/cart").then().statusCode(204);
    given().spec(user).body(Map.of("productId", 12, "quantity", 1)).post("/api/cart/items").then().statusCode(200);
    return given().spec(user).body("{}").post("/api/orders").then().statusCode(201).extract().path("id");
}

private void paymentReturns(String status) {
    wm.stubFor(post("/api/payments").willReturn(okJson("{\"status\":\"" + status + "\",\"transactionId\":\"tx-1\"}")));
}

private String pay(int id) {
    return given().spec(user).post("/api/orders/" + id + "/pay").then().statusCode(200).extract().path("status");
}
```

### Сценарий 1: успешная оплата и что ушло в банк

```java
@Test
void approvedPaymentMarksOrderPaid() {
    paymentReturns("APPROVED");
    int id = newOrder();
    assertThat(pay(id)).isEqualTo("PAID");
    wm.verify(1, postRequestedFor(urlEqualTo("/api/payments"))
            .withRequestBody(matchingJsonPath("$.orderId", equalTo(String.valueOf(id))))
            .withRequestBody(matchingJsonPath("$.currency", equalTo("RUB"))));
}
```

Проверяем **обе стороны**: результат для пользователя (PAID) и содержимое запроса в банк — `orderId` нужного заказа, валюта. Можно добавить сумму: `matchingJsonPath("$.amount", equalTo("99.0"))`.

### Сценарий 2: банк упал

```java
@Test
void paymentServiceErrorMarksFailed() {
    wm.stubFor(post("/api/payments").willReturn(serverError()));
    assertThat(pay(newOrder())).isEqualTo("PAYMENT_FAILED");
}
```

Ловит BUG-503: при включённом баге QA Burger считает заказ оплаченным, хотя деньги не списались. Представьте цену такого бага в продакшене.

### Сценарий 3: двойное списание

```java
@Test
void secondPaymentIsRejectedAndNotCharged() {
    paymentReturns("APPROVED");
    int id = newOrder();
    pay(id);
    given().spec(user).post("/api/orders/" + id + "/pay").then().statusCode(409);
    wm.verify(1, postRequestedFor(urlEqualTo("/api/payments")));
}
```

Даже если бы API ответил 409, без `verify` мы бы не узнали, ходил ли QA Burger в банк второй раз. Мок позволяет проверить **невидимый** снаружи эффект — ради этого он и нужен.

### Сценарий 4: отказ банка

```java
@Test
void declinedPaymentMarksFailed() {
    paymentReturns("DECLINED");
    assertThat(pay(newOrder())).isEqualTo("PAYMENT_FAILED");
}
```

## Ещё идеи для проверок

- задержка 6 с (больше таймаута клиента 5 с) → PAYMENT_FAILED, а запрос пользователя не висит минуту;
- ответ без поля `status` или не JSON → не 500;
- после PAYMENT_FAILED повторная оплата с APPROVED → PAID (по требованиям можно повторить);
- оплата заказа на сумму больше 50 000 (встроенная заглушка отказывает) — а что говорит ваш WireMock?

```task
id: m12-t03
title: WireMock вместо платёжного сервиса
module: exercises/m12-mocks
test: PaymentMockTestCheck
file: exercises/m12-mocks/src/test/java/ru/qacourse/m12/tasks/PaymentMockTest.java
solution: solutions/exercises/m12-mocks/src/test/java/ru/qacourse/m12/tasks/PaymentMockTest.java
text: |
  Четыре сценария оплаты с WireMock на порту `ShopConfig.paymentMockPort()`: APPROVED (+ verify запроса), 500, повторная оплата (+ verify ровно 1 вызова), DECLINED.

  Проверка поднимет QA Burger с `shop.payment.url` на ваш WireMock и убедится, что тесты ловят BUG-503 и BUG-502.
hint: |
  WireMock — в `@BeforeAll` на фиксированном порту; `resetAll()` — в `@BeforeEach`. Код всех сценариев — в уроке.
```

```quiz
- q: Почему здесь WireMock запускают на фиксированном порту, а не dynamicPort?
  options: ["QA Burger должен заранее знать адрес платёжки", "dynamicPort не работает", "Так быстрее"]
  answer: 0
- q: Как запустить QA Burger, чтобы он ходил в WireMock?
  options: ["--shop.payment.url=http://localhost:8089", "--wiremock=true", "Никак"]
  answer: 0
- q: Что позволяет проверить verify в сценарии двойной оплаты?
  options:
    - "Что приложение не обратилось в банк второй раз — эффект, невидимый в ответе API"
    - "Статус ответа"
    - "Время ответа"
  answer: 0
- q: Платёжка ответила 500. Какой статус заказа правильный?
  options: ["PAYMENT_FAILED", "PAID", "NEW"]
  answer: 0
```
