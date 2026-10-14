---
title: "9.7 Проверка контракта: JSON Schema"
minutes: 35
---

## Зачем

Проверять каждое поле ответа через `body(...)` долго, а главное — **структуру** легко упустить: поле переименовали, цена стала строкой `"99.00"` вместо числа, обязательное поле пропало. Для фронтенда и мобильного приложения это поломка, хотя «значения правильные».

**JSON Schema** — стандарт описания структуры JSON: какие поля, каких типов, какие обязательны, какие ограничения. Тест сверяет ответ со схемой одной строкой.

## Схема товара

`exercises/m09-rest-assured/src/test/resources/schemas/product.json`:

```json
{
  "$schema": "http://json-schema.org/draft-04/schema#",
  "title": "Товар QA Burger",
  "type": "object",
  "required": ["id", "name", "description", "price", "category", "stock"],
  "properties": {
    "id": { "type": "integer", "minimum": 1 },
    "name": { "type": "string", "minLength": 1 },
    "description": { "type": "string" },
    "price": { "type": "number", "minimum": 0, "exclusiveMinimum": true },
    "category": { "type": "string", "enum": ["BURGER", "SIDE", "DRINK", "DESSERT"] },
    "stock": { "type": "integer", "minimum": 0 },
    "emoji": { "type": ["string", "null"] }
  },
  "additionalProperties": false
}
```

| Ключ | Значение |
|---|---|
| `$schema` | версия стандарта. Валидатор REST Assured поддерживает **draft-04** — пишите её |
| `type` | тип: `object`, `array`, `string`, `number`, `integer`, `boolean`, `null` (можно список) |
| `required` | обязательные поля |
| `properties` | описание каждого поля |
| `minimum` + `exclusiveMinimum: true` | число строго больше минимума (в draft-04 это булево значение) |
| `minLength`, `maxLength`, `pattern` | ограничения строк (`pattern` — regex) |
| `enum` | допустимые значения |
| `additionalProperties: false` | **запрещены** поля, не описанные в схеме — ловим неожиданные изменения API |

Для массивов: `"type": "array", "items": { ...схема элемента... }, "minItems": 1`. Для вложенных объектов — `properties` внутри `properties`. Части схемы можно переиспользовать через `"$ref"`.

## Подключение

```xml
<dependency>
    <groupId>io.rest-assured</groupId>
    <artifactId>json-schema-validator</artifactId>
    <scope>test</scope>
</dependency>
```

## Тест

```java
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

@Test
void productMatchesSchema() {
    given().baseUri(ShopConfig.baseUrl())
            .get("/api/products/1")
            .then().statusCode(200)
            .body(matchesJsonSchemaInClasspath("schemas/product.json"));
}
```

- `matchesJsonSchemaInClasspath("schemas/product.json")` — матчер, загружающий схему из ресурсов (путь от `src/test/resources`);
- если ответ не соответствует — понятное сообщение: какое поле и какое правило нарушено.

> 🧪 **Попробуй сам.** Поменяйте в схеме `"price": {"type": "string"}` и запустите тест — посмотрите на сообщение об ошибке. Верните обратно.

## Где взять схему

- сгенерировать по примеру ответа: https://www.liquid-technologies.com/online-json-to-schema-converter, плагины IDEA (затем **обязательно** проверить и ужесточить вручную — генератор не знает про `enum` и ограничения);
- взять из OpenAPI-описания (там уже есть схемы моделей; http://localhost:8081/v3/api-docs → `components.schemas`).

## Схема или модель?

- **Модель + `extract().as(...)`** — удобно работать с данными в тесте, но по умолчанию «прощает» многое (лишние поля игнорируются, типы приводятся).
- **JSON Schema** — строгий контроль структуры.

Обычно делают отдельные **контрактные тесты** со схемами на каждую ручку и «функциональные» тесты на моделях.

```task
id: m09-t07
title: JSON Schema
module: exercises/m09-rest-assured
test: ProductSchemaTestCheck
file: exercises/m09-rest-assured/src/test/java/ru/qacourse/m09/tasks/ProductSchemaTest.java
solution: solutions/exercises/m09-rest-assured/src/test/java/ru/qacourse/m09/tasks/ProductSchemaTest.java
text: |
  В `ProductSchemaTest`:
  1. ответ `GET /api/products/1` соответствует схеме `schemas/product.json`;
  2. каждый товар каталога соответствует схеме (получите список id и проверьте каждый `GET /api/products/{id}`).
hint: |
  `import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;`
  Список id: `extract().path("content.id")` от `GET /api/products?size=100`.
```

```quiz
- q: Что проверяет additionalProperties false?
  options: ["Что в ответе нет полей, не описанных в схеме", "Что поля не пустые", "Ничего"]
  answer: 0
- q: Какую версию JSON Schema понимает валидатор REST Assured?
  options: ["draft-04", "Только draft-2020-12", "Любую"]
  answer: 0
- q: Цена стала приходить строкой "99.00". Какая проверка это поймает надёжнее?
  options: ["JSON Schema с type number", "statusCode(200)", "Никакая"]
  answer: 0
- q: Где лежат схемы для matchesJsonSchemaInClasspath?
  options: ["В src/test/resources (путь указывается от этой папки)", "На сервере", "В target"]
  answer: 0
```
