---
title: "13.1 Асинхронность и Awaitility"
minutes: 40
---

## Синхронно и асинхронно

**Синхронно:** вы отправили запрос — сервер всё сделал — вернул результат. Как `POST /api/cart/items`: в ответе уже обновлённая корзина.

**Асинхронно:** сервер принял задачу, ответил «принято» — а работа идёт **потом**, в фоне. Как оплата в QA Burger:

```text
POST /api/orders/1/pay  →  200 {"status": "PAID"}       ← ответ сразу
                         … 2 секунды …  кухня: COOKING
                         … 3 секунды …  кухня: READY
```

Кухня получает событие «заказ оплачен» и меняет статус в своём темпе. В реальных системах так устроено почти всё долгое: отправка писем, генерация отчётов, обработка платежей, доставка уведомлений, синхронизация со складом.

## Почему это ломает тесты

```java
pay(orderId);
assertThat(status(orderId)).isEqualTo("READY");   // ❌ упадёт: прямо сейчас статус PAID
```

Наивное лечение:

```java
pay(orderId);
Thread.sleep(6000);                               // ❌ «подождём наверняка»
assertThat(status(orderId)).isEqualTo("READY");
```

Проблемы мы уже обсуждали (урок 5.10): то ждём впустую, то не дожидаемся на медленном стенде. Правильно — **опрашивать** с таймаутом: проверять каждые полсекунды, пока условие не выполнится, но не дольше N секунд.

## Awaitility

**Awaitility** — библиотека, которая делает ровно это:

```xml
<dependency>
    <groupId>org.awaitility</groupId>
    <artifactId>awaitility</artifactId>
    <scope>test</scope>
</dependency>
```

```java
import static org.awaitility.Awaitility.await;

await().atMost(Duration.ofSeconds(20))
        .pollInterval(Duration.ofMillis(500))
        .untilAsserted(() -> {
            String status = given().spec(user).get("/api/orders/" + id)
                    .then().statusCode(200).extract().path("status");
            assertThat(status).isEqualTo("READY");
        });
```

Разбор:
- `await()` — начать ожидание;
- **`atMost(20 с)`** — максимум. Не дождались — тест падает с `ConditionTimeoutException`, в сообщении — последняя ошибка проверки (например, `expected: "READY" but was: "COOKING"`);
- **`pollInterval(500 мс)`** — как часто повторять (по умолчанию 100 мс; для HTTP-запросов лучше реже, не нагружая сервер);
- **`untilAsserted(лямбда)`** — повторять блок, пока он не выполнится **без исключения**. Внутри — обычные проверки AssertJ.

Если пришло за 1 секунду — тест идёт дальше через 1 секунду. Если стенд тормозит — подождём до 20.

Другие формы:

```java
await().atMost(Duration.ofSeconds(10)).until(() -> status(id).equals("READY"));       // условие boolean
String s = await().until(() -> status(id), equalTo("READY"));                          // значение + матчер Hamcrest
await().pollDelay(Duration.ofSeconds(1)).atMost(...).until(...);                        // начать опрос не сразу
await().during(Duration.ofSeconds(3)).atMost(Duration.ofSeconds(5)).until(...);        // условие ДЕРЖИТСЯ 3 с
await().ignoreExceptions().until(...);                                                  // игнорировать исключения, пока ждём
```

`during` полезен для проверок «ничего не произошло»: статус **не** должен меняться в течение 3 секунд (например, отменённый заказ не начали готовить).

### Как выбрать таймаут

- Узнайте **ожидаемое** время у разработчиков или из требований (кухня: ~5 с).
- Таймаут — с запасом в 2–4 раза (20 с), чтобы медленный CI-стенд не давал ложных падений.
- Выносите таймауты в конфигурацию (урок 6.7).

## Что проверять в асинхронных процессах

- конечный результат наступает (READY);
- промежуточные состояния идут **в правильном порядке** (NEW → PAID → COOKING → READY, а не READY до COOKING);
- **ничего лишнего** не происходит (отменённый заказ не готовится);
- поведение при сбоях (сервис-получатель недоступен — сообщение не теряется);
- **идемпотентность**: событие пришло дважды — заказ не готовят дважды.

## Журнал событий QA Burger

Без Kafka (обычный запуск) QA Burger передаёт события кухне внутри себя, а копию складывает в журнал: http://localhost:8081/api/debug/events?orderId=1

```json
[
  {"type": "ORDER_CREATED", "orderId": 1, "status": "NEW", ...},
  {"type": "ORDER_PAID",    "orderId": 1, "status": "PAID", ...},
  {"type": "ORDER_COOKING", "orderId": 1, "status": "COOKING", ...},
  {"type": "ORDER_READY",   "orderId": 1, "status": "READY", ...}
]
```

Порядок и полнота событий — второй тест задания: `containsExactly(...)` проверяет и состав, и порядок. С BUG-501 событие ORDER_PAID не публикуется — кухня о заказе не узнаёт, и оба теста падают по таймауту.

```task
id: m13-t01
title: Ожидание статуса с Awaitility
module: exercises/m13-kafka-async
test: OrderStatusAwaitTestCheck
file: exercises/m13-kafka-async/src/test/java/ru/qacourse/m13/tasks/OrderStatusAwaitTest.java
solution: solutions/exercises/m13-kafka-async/src/test/java/ru/qacourse/m13/tasks/OrderStatusAwaitTest.java
text: |
  Два теста: после оплаты заказ доходит до READY; в журнале событий заказа по порядку CREATED, PAID, COOKING, READY.
  Только Awaitility — `Thread.sleep(` проверка не пропустит. Работает без Docker.
hint: |
  `await().atMost(Duration.ofSeconds(20)).pollInterval(Duration.ofMillis(500)).untilAsserted(() -> { ... });`
  Список типов событий: `extract().path("type")` от `GET /api/debug/events?orderId=...`.
```

```quiz
- q: Что делает await().atMost(20 с).untilAsserted(...)?
  options:
    - "Повторяет проверку, пока она не пройдёт, но не дольше 20 секунд"
    - "Ждёт ровно 20 секунд"
    - "Проверяет один раз"
  answer: 0
- q: Чем Awaitility лучше Thread.sleep?
  options:
    - "Не ждёт дольше нужного и даёт запас на медленном стенде"
    - "Ничем"
    - "Работает без сети"
  answer: 0
- q: Как проверить, что статус НЕ меняется в течение 3 секунд?
  options: ["await().during(3 с)...", "Thread.sleep(3000)", "Никак"]
  answer: 0
- q: Какой интервал опроса разумен для HTTP-запросов?
  options: ["Сотни миллисекунд — не нагружая сервер", "1 мс", "1 минута"]
  answer: 0
```
