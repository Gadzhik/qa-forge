---
title: "9.4 PUT, DELETE и полный цикл CRUD"
minutes: 35
---

## CRUD-сценарий

Классический API-тест для ресурса — пройти весь жизненный цикл: **создать → прочитать → изменить → удалить → убедиться, что удалено**. Управлять товарами в QA Burger может только администратор.

```java
private Map<String, Object> product(double price) {
    Map<String, Object> body = new HashMap<>();
    body.put("name", "Тестовый бургер");
    body.put("description", "Создан автотестом");
    body.put("price", price);
    body.put("category", "BURGER");
    body.put("stock", 5);
    return body;
}

@Test
void adminCrud() {
    var admin = Specs.auth("admin", "admin123");

    int id = given(admin).body(product(100.0))
            .post("/api/products")
            .then().statusCode(201).body("name", equalTo("Тестовый бургер"))
            .extract().path("id");

    given(admin).body(product(150.5))
            .put("/api/products/" + id)
            .then().statusCode(200).body("price", equalTo(150.5f));

    given(admin).delete("/api/products/" + id).then().statusCode(204);
    given(admin).get("/api/products/" + id).then().statusCode(404);
}
```

Разбор:
- `HashMap` для тела — его удобно менять под разные запросы (здесь меняется цена);
- `var admin = Specs.auth(...)` — спецификация с токеном администратора (класс `Specs` разберём в следующем уроке; `var` — тип выведется сам);
- **`given(admin)`** — начать запрос на основе готовой спецификации (адрес, JSON, токен уже внутри);
- `post` → **201** и извлекаем `id` созданного товара;
- **`put`** — полная замена товара: отправляем все поля, цена другая → **200** и новая цена в ответе;
- **`delete`** → **204** (No Content — тела нет);
- контрольный **`get`** → **404**: товар действительно удалён. Без этого шага тест поверил бы ответу «удалено» на слово.

## Права доступа

```java
@Test
void userCannotCreate() {
    given(Specs.auth("user", "user123")).body(product(100.0))
            .post("/api/products")
            .then().statusCode(403);
}
```

Каждую ручку, требующую особых прав, проверяют под разными ролями: без токена (401), с недостаточной ролью (403), с правильной (успех).

## Уборка за собой

Тесты, создающие данные, оставляют «мусор»: тестовые товары, пользователей, заказы. На общем стенде это мешает другим тестам и людям. Приёмы:
- удалять созданное в самом тесте или в `@AfterEach` (храните id созданного в поле);
- использовать уникальные имена с префиксом (`autotest_...`), чтобы мусор было легко найти и почистить;
- сбрасывать данные служебной ручкой (у QA Burger — `POST /api/admin/reset`) — только на своём локальном стенде!
- поднимать для тестов **отдельное окружение** (Docker, модуль 15) — так делают проверки курса.

## PATCH

В QA Burger нет PATCH, но в других API встретится: частичное изменение — отправляют только меняемые поля:

```java
given(admin).body(Map.of("price", 150.5)).patch("/api/products/" + id).then().statusCode(200);
```

## Проверка идемпотентности

`PUT` и `DELETE` должны быть идемпотентны (урок 8.2). Хорошие дополнительные проверки:
- повторный `PUT` с тем же телом → тот же результат;
- повторный `DELETE` уже удалённого → обычно 404 (по требованиям), но точно не 500.

```task
id: m09-t04
title: CRUD товара администратором
module: exercises/m09-rest-assured
test: AdminProductsApiTestCheck
file: exercises/m09-rest-assured/src/test/java/ru/qacourse/m09/tasks/AdminProductsApiTest.java
solution: solutions/exercises/m09-rest-assured/src/test/java/ru/qacourse/m09/tasks/AdminProductsApiTest.java
text: |
  1. Под `admin` — создать товар (201), изменить цену PUT (200), удалить (204), убедиться, что GET даёт 404.
  2. Под `user` — POST товара → 403.
hint: |
  Используйте `Specs.auth("admin", "admin123")` и `given(spec)`. Цену после PUT сравнивайте как `equalTo(150.5f)` (float).
```

```quiz
- q: Зачем после DELETE делать контрольный GET?
  options:
    - "Убедиться, что ресурс действительно удалён, а не поверить ответу на слово"
    - "Чтобы восстановить ресурс"
    - "Так требует REST Assured"
  answer: 0
- q: Какой код вернёт QA Burger на POST /api/products под обычным пользователем?
  options: ["401", "403", "201"]
  answer: 1
- q: Чем PUT отличается от PATCH?
  options:
    - "PUT заменяет ресурс целиком, PATCH меняет часть полей"
    - "Ничем"
    - "PATCH удаляет ресурс"
  answer: 0
- q: Как не оставлять мусор на общем стенде? (несколько)
  options:
    - "Удалять созданные данные в тесте или @AfterEach"
    - "Использовать уникальные префиксы имён"
    - "Поднимать отдельное окружение для тестов"
    - "Не тестировать создание данных"
  answer: [0, 1, 2]
```
