---
title: "8.7 Что проверять в API + HTTP из Java"
minutes: 45
---

## Чек-лист API-проверок

Для каждой ручки (endpoint) API проверяют несколько слоёв:

**1. Статус-код**
- позитивный сценарий: 200 / 201 / 204;
- каждый негативный сценарий — свой правильный код (400, 401, 403, 404, 409), **никогда** не 500.

**2. Тело ответа**
- значения полей: правильные данные, правильные вычисления (суммы, скидки);
- **структура (контракт)**: все обязательные поля на месте, правильные типы (`price` — число, а не строка), нет лишних/устаревших — проверяется JSON-схемой (модуль 9);
- для списков: количество, сортировка, фильтрация, пагинация;
- для ошибок: машиночитаемый `code`, понятный `message`.

**3. Заголовки**
- `Content-Type: application/json`;
- `Location` у 201 (если требуется);
- заголовки безопасности и кэширования (если в требованиях).

**4. Побочные эффекты** — изменилось ли состояние системы:
- после `POST /api/orders` заказ виден в `GET /api/orders`, корзина пуста, остаток товара уменьшился;
- в базе появилась запись (модуль 11), в Kafka ушло событие (модуль 13);
- после `DELETE` — ресурса нет (`GET` → 404).

**5. Негативные и граничные данные** (уроки 1.5–1.6)
- отсутствующие обязательные поля, пустые, `null`, неверный тип;
- граничные значения;
- лишние поля в запросе;
- неверный JSON (синтаксически);
- неверный `Content-Type`;
- очень большие тела, спецсимволы, Unicode.

**6. Авторизация** (урок 8.5)
- без токена, с неверным, с чужим, с недостаточными правами.

**7. Бизнес-логика и состояния**
- допустимые и недопустимые переходы (оплата оплаченного, отмена оплаченного);
- идемпотентность и повторы;
- параллельные запросы (две оплаты одновременно).

**8. Нефункциональное** (отдельные виды тестов)
- время ответа, поведение под нагрузкой, лимиты запросов.

## Пример: тест-дизайн для POST /api/cart/items

| № | Сценарий | Ожидание |
|---|---|---|
| 1 | Валидные данные, товар новый | 200, строка в корзине, количество верное |
| 2 | Тот же товар повторно | 200, количество суммировано (BUG-302) |
| 3 | quantity = 0 / −1 | 400 BAD_QUANTITY (BUG-301) |
| 4 | quantity = 99 / 100 | 200 / 400 |
| 5 | quantity > stock | 400 OUT_OF_STOCK (BUG-303) |
| 6 | Несуществующий productId | 404 |
| 7 | Нет productId в теле | 400 VALIDATION_ERROR |
| 8 | quantity = "два" | 400 BAD_REQUEST |
| 9 | Без токена | 401 |
| 10 | Итог корзины после добавления | total = Σ price × qty (BUG-304) |
| 11 | Корзина одного пользователя не видна другому | у другого пусто |

Обратите внимание: из 11 проверок 9 — негативные или граничные.

## Цепочки запросов

Большинство API-тестов — не одиночные запросы, а **сценарии**: результат одного запроса используется в следующем.

```text
1. POST /api/auth/login              → извлечь token
2. POST /api/cart/items (token)       → положить товар
3. POST /api/orders (token)           → извлечь id заказа
4. POST /api/orders/{id}/pay (token)  → проверить статус PAID
5. ждать, пока GET /api/orders/{id} вернёт READY
```

Шаги 1–3 — **подготовка** (arrange), 4 — **действие**, 5 — **проверка**. Хорошая практика — выносить подготовку в переиспользуемые методы, а ещё лучше — готовить данные самым быстрым путём (через API или прямо в БД, даже если тестируете UI).

## HTTP из Java без библиотек

Перед REST Assured посмотрим, как отправить запрос «голыми руками» встроенным в Java клиентом `java.net.http.HttpClient` (Java 11+). Это задание к уроку.

```java
HttpClient client = HttpClient.newHttpClient();

HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + "/api/products/1"))
        .GET()
        .build();

HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

int status = response.statusCode();     // 200
String body = response.body();          // {"id":1,"name":"Классический бургер",...}
```

- `HttpClient.newHttpClient()` — клиент (переиспользуется для многих запросов);
- `HttpRequest.newBuilder(URI)` — строитель запроса: адрес, метод, заголовки;
- `client.send(request, BodyHandlers.ofString())` — отправить и дождаться ответа; тело получить строкой;
- `send` бросает `IOException` (сеть) и `InterruptedException` (поток прерван) — отсюда `throws` в сигнатурах.

POST с JSON и заголовками:

```java
String json = mapper.writeValueAsString(Map.of("username", "user", "password", "user123"));

HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + "/api/auth/login"))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(json))
        .build();
```

Разбор ответа — Jackson (урок 6.6): `mapper.readTree(response.body()).get("token").asText()`.

Работает, но многословно: на каждый запрос — 5 строк, плюс ручной разбор JSON и проверки. В модуле 9 то же самое займёт одну цепочку:

```java
given().baseUri(baseUrl).get("/api/products/1")
        .then().statusCode(200).body("name", equalTo("Классический бургер"));
```

Поэтому для тестов используют REST Assured. Но понимать, что внутри, — полезно.

```task
id: m08-t07
title: HTTP-запросы на чистой Java
module: exercises/m08-http
test: ShopHttpCheck
file: exercises/m08-http/src/main/java/ru/qacourse/m08/ShopHttp.java
solution: solutions/exercises/m08-http/src/main/java/ru/qacourse/m08/ShopHttp.java
text: |
  В `ShopHttp` по образцу готового метода `productStatus` реализуйте:
  - `productName(id)` — имя товара из JSON;
  - `countInCategory(category)` — `totalElements` для фильтра по категории;
  - `login(username, password)` — POST с JSON, вернуть токен; при коде не 200 — `IllegalStateException` с кодом в сообщении;
  - `cartQuantity(token)` — GET корзины с заголовком `Authorization: Bearer ...`.

  Проверка сама запустит QA Burger на свободном порту — запускать приложение не нужно.
hint: |
  Все конструкции есть в уроке. JSON разбирайте через `mapper.readTree(body).get("поле")` и `.asText()` / `.asInt()`.
```

```quiz
- q: Тест POST /api/orders вернул 201. Что ещё стоит проверить? (несколько)
  options:
    - "Сумму и статус в теле ответа"
    - "Что корзина после заказа пуста"
    - "Что заказ появился в GET /api/orders"
    - "Ничего, 201 достаточно"
  answer: [0, 1, 2]
- q: Что такое контракт API в тестах?
  options:
    - "Структура запросов и ответов: поля, типы, обязательность"
    - "Договор с заказчиком"
    - "Тариф на использование API"
  answer: 0
- q: Какую часть цепочки «логин → корзина → заказ → оплата → проверка статуса» считают подготовкой?
  options: ["Логин, корзину и заказ", "Только проверку статуса", "Всю цепочку"]
  answer: 0
- q: Какое исключение бросает HttpClient.send при сетевой ошибке?
  options: ["IOException", "NullPointerException", "AssertionError"]
  answer: 0
```
