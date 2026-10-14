---
title: "9.2 Параметры запроса и извлечение данных из ответа"
minutes: 45
---

## Параметры

### Query-параметры

```java
given()
        .baseUri(ShopConfig.baseUrl())
        .queryParam("category", "DRINK")
        .queryParam("sort", "price,desc")
        .queryParam("size", 100)
        .get("/api/products");
```

`queryParam(имя, значение)` добавляет `?category=DRINK&sort=price,desc&size=100`. REST Assured сам закодирует спецсимволы и русские буквы (урок 8.1). Несколько сразу: `.queryParams(Map.of("category", "DRINK", "size", 100))`.

### Path-параметры

```java
given()
        .baseUri(ShopConfig.baseUrl())
        .pathParam("id", 1)
        .get("/api/products/{id}");
```

`{id}` в пути заменится значением. Короче: `.get("/api/products/{id}", 1)` — значения подставляются по порядку.

> 💡 Path-параметры лучше склеивания строк (`"/api/products/" + id`): путь остаётся шаблоном, который видно в логах и отчётах, и значения корректно кодируются.

### Заголовки и cookies

```java
.header("Authorization", "Bearer " + token)
.header("X-Request-Id", UUID.randomUUID().toString())
.cookie("design", "v2")
.contentType(ContentType.JSON)        // то же, что header("Content-Type", "application/json")
.accept(ContentType.JSON)
```

## Извлечение данных: extract()

Проверок внутри `then()` часто недостаточно: нужно **сохранить значение** — чтобы использовать в следующем запросе или проверить сложной логикой.

```java
int id = given().baseUri(url).body(order).post("/api/orders")
        .then().statusCode(201)
        .extract().path("id");
```

- `.extract()` — «достать из ответа»;
- `.path("id")` — значение по пути; тип выведется из переменной слева (`int`, `String`, `List<...>`).

Варианты:

```java
Response response = given()...get(...).then().extract().response();   // весь ответ
String body = response.asString();             // тело строкой
int status = response.statusCode();
String header = response.header("Content-Type");
JsonPath json = response.jsonPath();           // удобный разбор тела
ProductDto p = response.as(ProductDto.class);  // в объект (урок 9.3)
```

## GPath — пути в JSON

REST Assured использует синтаксис **GPath** (из языка Groovy). Для ответа:

```json
{
  "content": [
    { "id": 12, "name": "Кола 0.5", "price": 99.0, "category": "DRINK" },
    { "id": 13, "name": "Лимонад домашний", "price": 149.9, "category": "DRINK" }
  ],
  "totalElements": 2
}
```

| Путь | Результат |
|---|---|
| `totalElements` | `2` |
| `content[0].name` | `"Кола 0.5"` |
| `content[-1].name` | `"Лимонад домашний"` (последний) |
| `content.name` | `["Кола 0.5", "Лимонад домашний"]` — поле у **каждого** элемента! |
| `content.size()` | `2` |
| `content.find { it.price > 100 }.name` | `"Лимонад домашний"` — первый подходящий |
| `content.findAll { it.price < 100 }.id` | `[12]` — все подходящие |
| `content.collect { it.price }.sum()` | `248.9` |
| `content.max { it.price }.name` | самый дорогой |

`it` — «текущий элемент» в фигурных скобках (как параметр лямбды). Работает и в `body(...)`, и в `extract().path(...)`, и в `jsonPath().get(...)`.

> 💡 Сложные GPath-выражения трудно читать. Часто понятнее извлечь список объектов (урок 9.3) и обработать Stream API.

## JsonPath — разбор ответа

```java
JsonPath json = given()...get("/api/products?category=DRINK&size=100")
        .then().statusCode(200)
        .extract().jsonPath();

List<String> categories = json.getList("content.category");
int total = json.getInt("totalElements");
List<ProductDto> products = json.getList("content", ProductDto.class);
float price = json.getFloat("content[0].price");
String priceText = json.getString("content[0].price");   // "99.0"
```

Методы с типом (`getInt`, `getList`, `getString`) снимают вопрос «какой тип вернётся».

## Проверка сортировки и фильтров

Это задание. Сортировка по убыванию цены:

```java
List<Float> prices = given()
        .baseUri(ShopConfig.baseUrl())
        .queryParam("sort", "price,desc")
        .queryParam("size", 100)
        .get("/api/products")
        .then().statusCode(200)
        .extract().path("content.price");

assertThat(prices).isNotEmpty().isSortedAccordingTo(Comparator.reverseOrder());
```

- извлекаем **список всех цен** путём `content.price`;
- `isNotEmpty()` — защита от «пустой» проверки: пустой список «отсортирован» всегда, и тест прошёл бы, даже если API вернул ничего;
- `isSortedAccordingTo(Comparator.reverseOrder())` — по убыванию.

> ⚠️ **Ловушка «пустой проверки».** `assertThat(list).allMatch(...)`, `containsOnly(...)`, `isSorted()` — все проходят на пустом списке! Всегда проверяйте, что данные вообще есть.

Фильтр:

```java
JsonPath json = ...extract().jsonPath();
List<String> categories = json.getList("content.category");
assertThat(categories).isNotEmpty().containsOnly("DRINK");
assertThat(categories).hasSize(json.getInt("totalElements"));
```

## Сравнение двух ответов

Ещё приём из практики: сравнить два списка, полученных разными способами (например, поиск «бургер» и «Бургер» — BUG-103; REST и GraphQL — BUG-901):

```java
List<String> lower = getNames("бургер");
List<String> upper = getNames("Бургер");
assertThat(lower).isNotEmpty().containsExactlyInAnyOrderElementsOf(upper);
```

```task
id: m09-t02
title: Сортировка и фильтр каталога
module: exercises/m09-rest-assured
test: CatalogApiTestCheck
file: exercises/m09-rest-assured/src/test/java/ru/qacourse/m09/tasks/CatalogApiTest.java
solution: solutions/exercises/m09-rest-assured/src/test/java/ru/qacourse/m09/tasks/CatalogApiTest.java
text: |
  Два теста в `CatalogApiTest`:
  1. `sort=price,desc` — цены по убыванию;
  2. `category=DRINK` — только напитки и их ровно `totalElements`.

  Проверка ловит BUG-102 и BUG-105.
hint: |
  `extract().path("content.price")` → `List<Float>`; `extract().jsonPath()` для нескольких значений из одного ответа.
```

```quiz
- q: "Что вернёт путь content.name для ответа со списком товаров?"
  options: ["Список названий всех товаров", "Название первого товара", "Ошибку"]
  answer: 0
- q: Зачем проверять isNotEmpty() перед isSorted()?
  options:
    - "Пустой список всегда «отсортирован» — тест прошёл бы, даже если API ничего не вернул"
    - "Иначе isSorted не скомпилируется"
    - "Для красоты"
  answer: 0
- q: Как подставить path-параметр?
  options: ['pathParam("id", 1) и путь "/api/products/{id}"', 'queryParam("id", 1)', 'header("id", 1)']
  answer: 0
- q: Как сохранить id созданного заказа в переменную?
  options: ['.then().extract().path("id")', '.then().body("id")', '.save("id")']
  answer: 0
- q: "Что делает GPath content.findAll { it.price < 100 }.id?"
  options: ["Список id товаров дешевле 100", "Первый id", "Сумму цен"]
  answer: 0
```
