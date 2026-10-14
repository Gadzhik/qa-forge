---
title: "6.6 Jackson — JSON и Java-объекты"
minutes: 40
---

## JSON за 2 минуты

**JSON** — текстовый формат обмена данными, в котором общаются почти все современные API. QA Burger отвечает именно им:

```json
{
  "id": 12,
  "name": "Кола 0.5",
  "price": 99.0,
  "category": "DRINK",
  "in_stock": true,
  "tags": ["cold", "sweet"],
  "maker": null
}
```

| Элемент | Запись | В Java |
|---|---|---|
| Объект | `{ "ключ": значение, ... }` | класс / record / `Map` |
| Массив | `[ ... ]` | `List` / массив |
| Строка | `"текст"` (только двойные кавычки!) | `String` |
| Число | `12`, `99.0` | `int`, `long`, `double`, `BigDecimal` |
| Логическое | `true` / `false` | `boolean` |
| Пусто | `null` | `null` |

Подробнее о JSON и API — в модуле 8.

## Jackson

**Jackson** — самая популярная Java-библиотека для JSON. Её используют Spring (внутри QA Burger), REST Assured (модуль 9) и почти все тестовые фреймворки. Две операции:

- **сериализация** — объект → JSON-строка;
- **десериализация** — JSON-строка → объект.

Всё делает класс **`ObjectMapper`**:

```java
private static final ObjectMapper MAPPER = new ObjectMapper();
```

Создают его **один раз** (`static final`): он потокобезопасен, а создание дорогое.

### Объект → JSON

```java
String json = MAPPER.writeValueAsString(new ProductJson(1, "Бургер", 289.9, "BURGER", false));
// {"id":1,"name":"Бургер","price":289.9,"category":"BURGER","in_stock":false}
```

Красиво, с отступами: `MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(obj)`.

### JSON → объект

```java
ProductJson cola = MAPPER.readValue(json, ProductJson.class);
```

- второй аргумент — **класс**, в объект которого превращать (`.class` — объект, описывающий класс).

Как Jackson сопоставляет поля:
- у **обычного класса** — через конструктор без параметров + сеттеры (или поля), при записи — через геттеры. Отсюда важность соглашений `getName()`/`setName()` и `@NoArgsConstructor` у Lombok-моделей;
- у **record** — через канонический конструктор.

### Имена полей не совпадают

В JSON `in_stock` (стиль snake_case), а в Java принято `inStock` (camelCase). Аннотация `@JsonProperty` связывает их:

```java
public record ProductJson(
        long id,
        String name,
        double price,
        String category,
        @JsonProperty("in_stock") boolean inStock) {
}
```

Если для всего класса стиль snake_case — проще одна аннотация на класс: `@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)`.

### Лишние поля в JSON

По умолчанию, если в JSON есть поле, которого нет в классе, Jackson бросит `UnrecognizedPropertyException: Unrecognized field "maker"`.

Что выбрать в автотестах — вопрос философии:
- **строго** (по умолчанию): тест падает при появлении нового поля в API. Вы узнаёте об изменении контракта, но тесты чаще ломаются;
- **мягко**: `@JsonIgnoreProperties(ignoreUnknown = true)` на классе или `MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)`. Новые поля не ломают тесты. Обычно выбирают это, а контракт проверяют отдельно — JSON-схемой (модуль 9).

### Списки и обобщённые типы

```java
List<ProductJson> products = MAPPER.readValue(json, new TypeReference<List<ProductJson>>() { });
```

Почему не `List.class`? Из-за особенности Java (стирание типов) во время выполнения `List<ProductJson>.class` не существует — Jackson не узнал бы, что внутри списка товары, и создал бы список `Map`. `new TypeReference<...>() { }` — анонимный класс, который «запоминает» полный тип. Выглядит странно, но это стандартный приём.

## JsonNode — без классов

Иногда создавать класс ради одного поля лень. Можно читать JSON как дерево:

```java
JsonNode root = MAPPER.readTree(pageJson);
String firstName = root.get("content").get(0).get("name").asText();
int total = root.get("totalElements").asInt();
boolean has = root.has("content");
JsonNode maybe = root.path("missing");   // path вместо get — не вернёт null, а «пустой» узел
```

- `get("поле")` — дочерний узел объекта (или `null`, если нет);
- `get(0)` — элемент массива;
- `asText()`, `asInt()`, `asDouble()`, `asBoolean()` — значение.

> 💡 В REST Assured есть свой, ещё более удобный способ — **JsonPath**: `response.path("content[0].name")`. Но понимать устройство через Jackson полезно.

## Деньги и BigDecimal

QA Burger отдаёт цены числом: `"price": 289.90`. Если читать их в `double`, вернётся неточность (урок 3.2). В тестах денег надёжнее `BigDecimal`:

```java
public record OrderDto(long id, BigDecimal total) { }
```

Jackson умеет читать числа в `BigDecimal`. Сравнивайте через `isEqualByComparingTo("289.90")`: `BigDecimal("289.9").equals(new BigDecimal("289.90"))` — `false` (разный масштаб!), а `compareTo` — `0`.

## Тренажёр POJO Builder

В разделе **Тренажёры** есть игра [**POJO Builder**](#/game/pojo-builder): дают JSON — вы пишете Java-класс, портал компилирует его и проверяет, что Jackson читает JSON без потерь. Отличная практика после этого урока.

```task
id: m06-t06
title: Jackson
module: exercises/m06-build-tools
test: JsonTasksCheck
file: exercises/m06-build-tools/src/main/java/ru/qacourse/m06/JsonTasks.java
solution: solutions/exercises/m06-build-tools/src/main/java/ru/qacourse/m06/JsonTasks.java
text: |
  1. В `ProductJson.java` свяжите поле `inStock` с JSON-полем `in_stock`.
  2. В `JsonTasks.java` реализуйте `toJson`, `parseProduct`, `parseProducts` (список через `TypeReference`) и `firstProductName` (через `JsonNode`).
hint: |
  - `@JsonProperty("in_stock") boolean inStock` + `import com.fasterxml.jackson.annotation.JsonProperty;`
  - `MAPPER.writeValueAsString(value)`, `MAPPER.readValue(json, ProductJson.class)`
  - `MAPPER.readTree(pageJson).get("content").get(0).get("name").asText()`
```

```quiz
- q: Что такое десериализация?
  options: [JSON → объект, Объект → JSON, Удаление JSON]
  answer: 0
- q: Как связать JSON-поле in_stock с Java-полем inStock?
  options: ['@JsonProperty("in_stock")', '@Name("in_stock")', "Никак, нужно переименовать поле в in_stock"]
  answer: 0
- q: Почему для списка используют TypeReference, а не List.class?
  options:
    - Из-за стирания типов List.class не знает тип элементов
    - List.class не существует
    - Так короче
  answer: 0
- q: Что сделает Jackson по умолчанию, встретив в JSON неизвестное поле?
  options: [Бросит UnrecognizedPropertyException, Проигнорирует, Создаст поле в классе]
  answer: 0
- q: "`new BigDecimal(\"289.9\").equals(new BigDecimal(\"289.90\"))` — ?"
  options: ["true", "false"]
  answer: 1
  explain: equals учитывает масштаб. Сравнивайте деньги через compareTo / isEqualByComparingTo.
```
