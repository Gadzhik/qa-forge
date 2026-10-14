---
title: "9.6 Авторизация в тестах: JWT, Basic, покрытие Swagger"
minutes: 40
---

## Способы передать авторизацию в REST Assured

```java
// Bearer-токен (JWT) — как в QA Burger
given().header("Authorization", "Bearer " + token)
given().auth().oauth2(token)                      // то же самое короче

// Basic Auth
given().auth().preemptive().basic("admin", "admin")

// Cookie
given().cookie("token", token)
```

- `auth().oauth2(token)` — несмотря на название, просто добавляет `Authorization: Bearer <token>`;
- `auth().preemptive().basic(...)` — **preemptive** («упреждающий»): сразу отправить заголовок. Без `preemptive()` REST Assured сначала отправит запрос без авторизации, получит 401 и только потом повторит с логином — лишний запрос.

## Кэширование токена

Вход — тоже запрос. Если в каждом из 500 тестов логиниться заново, это 500 лишних запросов. Токен можно получать один раз на пользователя и хранить:

```java
private static final Map<String, String> TOKENS = new ConcurrentHashMap<>();

public static String token(String username, String password) {
    return TOKENS.computeIfAbsent(username, u -> login(u, password));
}
```

- `ConcurrentHashMap` — потокобезопасный (урок 5.10), на случай параллельного запуска;
- `computeIfAbsent` — если токена для пользователя ещё нет, вычислить и запомнить; иначе вернуть сохранённый.

Учтите срок жизни токена (`exp`, у QA Burger — 8 часов) и то, что кэш ломается, если проверки поднимают новое приложение (в нашем курсе `Specs.token` не кэширует именно поэтому).

## Негативные проверки авторизации

```java
@Test
void withoutTokenIs401() {
    given(Specs.base()).get("/api/orders").then().statusCode(401);
}

@Test
void brokenTokenIs401() {
    String broken = Specs.token("user", "user123") + "x";
    given(Specs.base()).header("Authorization", "Bearer " + broken)
            .get("/api/orders").then().statusCode(401);
}
```

Испорченная подпись должна отвергаться — проверка того, что сервер действительно проверяет подпись JWT, а не просто читает его содержимое.

## Доступ к чужим данным (IDOR)

**IDOR** (Insecure Direct Object Reference) — одна из самых частых уязвимостей: пользователь меняет id в запросе и получает чужие данные. Тест:

```java
@Test
void otherUsersOrderIsHidden() {
    var admin = Specs.auth("admin", "admin123");
    given(admin).body(Map.of("productId", 12, "quantity", 1)).post("/api/cart/items").then().statusCode(200);
    int orderId = given(admin).body("{}").post("/api/orders").then().statusCode(201).extract().path("id");

    given(Specs.auth("user", "user123")).get("/api/orders/" + orderId).then().statusCode(404);
}
```

- первый пользователь создаёт ресурс;
- второй пытается к нему обратиться → должен получить отказ.

Это BUG-204. Такие тесты стоит писать для **каждой** ручки, где в пути или теле есть id чужого ресурса.

## Покрытие Swagger тестами

Хорошая систематическая задача — пройти по всей документации API (Swagger / OpenAPI) и для каждой ручки написать набор тестов:

| Ручка | Позитивный | Валидация | 401 | 403 | 404 | Бизнес-правила |
|---|---|---|---|---|---|---|
| `POST /api/auth/register` | ✅ | длина пароля, email | — | — | — | дубликаты |
| `POST /api/cart/items` | ✅ | 0, 100, тип | ✅ | — | товар | склад, суммирование |
| `GET /api/orders/{id}` | ✅ | — | ✅ | — | ✅ | чужой заказ |
| `POST /api/orders/{id}/pay` | ✅ | — | ✅ | — | ✅ | повтор, ошибка оплаты |
| `POST /api/products` | ✅ | поля | ✅ | ✅ | — | — |
| ... | | | | | | |

Такая таблица — **матрица покрытия API** (урок 1.7): сразу видно, что не проверено. Инструменты вроде **swagger-coverage** считают покрытие автоматически по запросам, которые отправили тесты.

> 🧪 **Попробуй сам.** Откройте Swagger QA Burger и составьте такую таблицу для всех ручек раздела «Заказы». Отметьте, какие клетки уже покрыты тестами из Test Lab.

```task
id: m09-t06
title: Авторизация и чужие заказы
module: exercises/m09-rest-assured
test: OrdersAuthApiTestCheck
file: exercises/m09-rest-assured/src/test/java/ru/qacourse/m09/tasks/OrdersAuthApiTest.java
solution: solutions/exercises/m09-rest-assured/src/test/java/ru/qacourse/m09/tasks/OrdersAuthApiTest.java
text: |
  Тесты в `OrdersAuthApiTest`:
  - `GET /api/orders` без токена → 401;
  - с испорченным токеном → 401;
  - заказ admin недоступен user → 404.

  Проверка ловит BUG-204.
hint: |
  Для пустого JSON-тела заказа: `.body("{}")`. Испорченный токен — `token + "x"`.
```

```quiz
- q: Что делает given().auth().oauth2(token)?
  options: ["Добавляет заголовок Authorization: Bearer token", "Выполняет полный OAuth-вход", "Шифрует токен"]
  answer: 0
- q: Зачем preemptive() в Basic Auth?
  options:
    - "Отправить логин сразу, без лишнего запроса с ответом 401"
    - "Зашифровать пароль"
    - "Для HTTPS"
  answer: 0
- q: Что такое IDOR?
  options:
    - "Уязвимость: подмена id в запросе даёт доступ к чужим данным"
    - "Формат токена"
    - "Тип базы данных"
  answer: 0
- q: Зачем тестировать токен с испорченной подписью?
  options:
    - "Убедиться, что сервер проверяет подпись, а не просто читает содержимое токена"
    - "Чтобы ускорить тесты"
    - "Это не нужно"
  answer: 0
```
