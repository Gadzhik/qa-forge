---
title: "12.2 WireMock: заглушки и проверка запросов"
minutes: 45
---

**WireMock** — HTTP-сервер, которым управляет тест: «на такой запрос ответь вот так» и «покажи, какие запросы приходили».

## Подключение

```xml
<dependency>
    <groupId>org.wiremock</groupId>
    <artifactId>wiremock-standalone</artifactId>
    <version>3.13.2</version>
    <scope>test</scope>
</dependency>
```

`wiremock-standalone` содержит все свои зависимости внутри (shaded) и не конфликтует с вашими библиотеками.

## Запуск сервера

```java
private WireMockServer wm;

@BeforeEach
void start() {
    wm = new WireMockServer(wireMockConfig().dynamicPort());
    wm.start();
}

@AfterEach
void stop() {
    wm.stop();
}
```

- `wireMockConfig().dynamicPort()` — занять **любой свободный** порт (не будет конфликта); или `.port(8089)` — конкретный;
- `wm.start()` — поднять HTTP-сервер; `wm.baseUrl()` — его адрес (`http://localhost:54321`);
- `wm.stop()` — остановить.

Импорты:

```java
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
```

(Пакет `com.github.tomakehurst` — историческое имя, по фамилии автора WireMock.)

## Заглушка: stubFor

```java
wm.stubFor(get(urlPathEqualTo("/api/rates"))
        .withQueryParam("currency", equalTo("USD"))
        .willReturn(okJson("{\"currency\":\"USD\",\"rate\":92.5}")));
```

Читается: «на GET `/api/rates` с параметром `currency=USD` ответь 200 с таким JSON».

**Сопоставление запроса:**

| Метод | Что проверяет |
|---|---|
| `get(...)`, `post(...)`, `put(...)`, `delete(...)`, `any(...)` | метод |
| `urlEqualTo("/a?x=1")` | путь **с** параметрами, точно |
| `urlPathEqualTo("/a")` | путь **без** параметров |
| `urlPathMatching("/api/orders/\\d+")` | путь по регулярному выражению |
| `.withQueryParam("x", equalTo("1"))` | параметр |
| `.withHeader("Authorization", containing("Bearer"))` | заголовок |
| `.withRequestBody(equalToJson("{...}"))` | тело равно JSON (порядок полей неважен) |
| `.withRequestBody(matchingJsonPath("$.type", equalTo("ORDER_READY")))` | поле тела по JSONPath |
| `.withRequestBody(containing("текст"))` | тело содержит |

**Ответы:**

| Метод | Ответ |
|---|---|
| `ok()`, `ok("текст")` | 200 |
| `okJson("{...}")` | 200 + JSON + `Content-Type: application/json` |
| `created()`, `noContent()` | 201, 204 |
| `badRequest()`, `unauthorized()`, `forbidden()`, `notFound()` | 400, 401, 403, 404 |
| `serverError()` | 500 |
| `aResponse().withStatus(503).withHeader("Retry-After", "10").withBody("...")` | что угодно |
| `.withFixedDelay(1500)` | ответ через 1,5 с |
| `.withFault(Fault.CONNECTION_RESET_BY_PEER)` | обрыв соединения |
| `.withFault(Fault.MALFORMED_RESPONSE_CHUNK)` | «мусор» вместо ответа |

Если ни одна заглушка не подошла — WireMock ответит **404** и (в логе) покажет, какая заглушка была «ближе всего» — очень удобно для отладки.

## Проверка запросов: verify

```java
wm.verify(1, getRequestedFor(urlPathEqualTo("/api/rates"))
        .withQueryParam("currency", equalTo("USD")));
```

- «запрос GET `/api/rates?currency=USD` пришёл **ровно 1 раз**»;
- варианты количества: `exactly(2)`, `moreThan(0)`, `lessThanOrExactly(3)`; без числа — «хотя бы один»;
- `postRequestedFor(...)`, `.withRequestBody(matchingJsonPath(...))` — проверить, **что** отправило приложение.

Посмотреть все пришедшие запросы: `wm.getAllServeEvents()` или `wm.findAll(postRequestedFor(...))` — удобно для отладки и для проверок AssertJ.

`wm.resetAll()` — удалить все заглушки и журнал запросов (между тестами). `wm.resetRequests()` — только журнал.

## Задержки и обрывы

```java
wm.stubFor(get("/api/slow").willReturn(ok("готово").withFixedDelay(1500)));
long start = System.currentTimeMillis();
given().baseUri(wm.baseUrl()).get("/api/slow").then().statusCode(200);
assertThat(System.currentTimeMillis() - start).isGreaterThanOrEqualTo(1500);
```

Так проверяют таймауты приложения: QA Burger ждёт ответа платёжки максимум 5 секунд (`setReadTimeout(5000)` в `PaymentClient`). С задержкой 6000 мс оплата должна завершиться ошибкой, а не зависнуть.

## Сценарии (stateful)

Ответ может зависеть от предыдущих запросов: первый раз «обрабатывается», второй — «готово»:

```java
wm.stubFor(get("/api/status").inScenario("Оплата")
        .whenScenarioStateIs(Scenario.STARTED)
        .willReturn(okJson("{\"status\":\"PROCESSING\"}"))
        .willSetStateTo("готово"));
wm.stubFor(get("/api/status").inScenario("Оплата")
        .whenScenarioStateIs("готово")
        .willReturn(okJson("{\"status\":\"APPROVED\"}")));
```

## WireMock как отдельный сервер

WireMock можно запустить без Java-теста — в Docker (`docker run -p 8089:8080 wiremock/wiremock:3.13.2`) — и описывать заглушки JSON-файлами в папке `mappings`:

```json
{
  "request": { "method": "POST", "url": "/api/payments" },
  "response": { "status": 200, "jsonBody": { "status": "APPROVED", "transactionId": "tx-1" } }
}
```

Так делают общие стенды с заглушками для ручного тестирования и CI. Заглушками можно управлять и по HTTP через админ-API (`/__admin/mappings`).

```task
id: m12-t02
title: Основы WireMock
module: exercises/m12-mocks
test: WireMockBasicsTestCheck
file: exercises/m12-mocks/src/test/java/ru/qacourse/m12/tasks/WireMockBasicsTest.java
solution: solutions/exercises/m12-mocks/src/test/java/ru/qacourse/m12/tasks/WireMockBasicsTest.java
text: |
  Три теста с WireMock (без QA Burger): заглушка + verify с параметром, сопоставление по полю JSON-тела, задержка ответа.
  Запросы к заглушке отправляйте REST Assured'ом на `wm.baseUrl()`.
hint: |
  `okJson(...)`, `matchingJsonPath("$.type", equalTo("ORDER_READY"))`, `ok(...).withFixedDelay(1500)`. Число в ответе REST Assured прочитает как `92.5f`.
```

```quiz
- q: Что вернёт WireMock, если ни одна заглушка не подошла?
  options: ["404 и подсказку о ближайшей заглушке", "200", "Зависнет"]
  answer: 0
- q: Чем urlEqualTo отличается от urlPathEqualTo?
  options: ["urlEqualTo сравнивает путь вместе с параметрами", "Ничем", "urlPathEqualTo строже"]
  answer: 0
- q: Как проверить, что приложение отправило в платёжку правильную сумму?
  options: ['verify(postRequestedFor(...).withRequestBody(matchingJsonPath("$.amount", ...)))', "Посмотреть логи вручную", "Никак"]
  answer: 0
- q: Как сымитировать медленный сервис?
  options: [".withFixedDelay(миллисекунды)", "Thread.sleep в тесте", "serverError()"]
  answer: 0
- q: Зачем resetAll() между тестами?
  options: ["Удалить заглушки и журнал запросов предыдущего теста", "Перезапустить приложение", "Это не нужно"]
  answer: 0
```
