---
title: "9.3 POST-запросы и модели (POJO)"
minutes: 45
---

## Тело запроса — три способа

**1. Строкой** — быстро, но хрупко (опечатки в кавычках, нет подсказок IDE):

```java
given().contentType(ContentType.JSON)
        .body("""
              {"username": "anna", "email": "anna@test.local", "password": "Secret1"}
              """)
        .post("/api/auth/register");
```

**2. Map** — гибко, удобно для простых тел и для негативных тестов (можно положить что угодно):

```java
Map<String, Object> body = new HashMap<>();
body.put("username", "anna");
body.put("email", "anna@test.local");
body.put("password", "Secret1");
given().contentType(ContentType.JSON).body(body).post("/api/auth/register");
```

**3. Объект (POJO)** — стандарт для проектов автотестов:

```java
RegisterRequest request = RegisterRequest.builder()
        .username("anna")
        .email("anna@test.local")
        .password("Secret1")
        .build();
given().contentType(ContentType.JSON).body(request).post("/api/auth/register");
```

REST Assured сам превратит объект в JSON с помощью Jackson (урок 6.6) — если Jackson есть в зависимостях.

> ⚠️ Не забывайте **`contentType(ContentType.JSON)`**: без него сервер не поймёт формат (415) или REST Assured отправит объект не так.

**POJO** (Plain Old Java Object) — «простой Java-объект»: класс только с полями, геттерами/сеттерами. В автотестах их называют **моделями**: модель запроса, модель ответа.

## Модель запроса — Lombok

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    private String username;
    private String email;
    private String password;
}
```

- `@Data` — геттеры (Jackson по ним строит JSON) и сеттеры (удобно менять одно поле в негативных тестах: `request.setPassword("12345")`);
- `@Builder` — читаемое создание тестовых данных;
- оба конструктора — для Jackson и билдера.

## Модель ответа — record

```java
@JsonIgnoreProperties(ignoreUnknown = true)
public record UserResponse(long id, String username, String email, String role) {
}
```

```java
UserResponse created = given()
        .baseUri(ShopConfig.baseUrl())
        .contentType(ContentType.JSON)
        .body(request)
        .post("/api/auth/register")
        .then().statusCode(201)
        .extract().as(UserResponse.class);

assertThat(created.username()).isEqualTo("anna");
assertThat(created.role()).isEqualTo("USER");
```

- `.extract().as(UserResponse.class)` — тело ответа → объект;
- `@JsonIgnoreProperties(ignoreUnknown = true)` — в ответе есть `createdAt`, которого нет в модели; без аннотации Jackson упадёт (урок 6.6).

Список объектов:

```java
List<ProductDto> products = given()...get("/api/products?size=100")
        .then().extract().jsonPath().getList("content", ProductDto.class);
```

## Генерация моделей из JSON

Писать модели вручную для больших ответов долго. Помощники:
- плагин IDEA **RoboPOJOGenerator** или **JsonToPojo**: правый клик по пакету → New → Generate POJO from JSON → вставить пример ответа → выбрать Lombok/record, Jackson;
- сайты https://www.jsonschema2pojo.org, https://json2csharp.com/code-converters/json-to-pojo;
- по OpenAPI-описанию генератор **openapi-generator** создаёт модели целиком.

После генерации всегда просматривайте результат: тип денег (`BigDecimal` лучше `double`), даты, enum'ы, имена полей.

Тренажёр [**POJO Builder**](#/game/pojo-builder) — именно про это: по JSON написать модель так, чтобы Jackson прочитал её без потерь.

## Негативные сценарии с моделью

С сеттерами легко делать варианты:

```java
RegisterRequest request = user("vera");
request.setPassword("12345");                     // слишком короткий
given()...body(request).post("/api/auth/register").then().statusCode(400);
```

А для «сломанного» JSON (поле другого типа, лишнее поле, null) удобнее Map или строка — модель такого не позволит.

## Данные тестов не должны пересекаться

Регистрация с одним и тем же `username` в двух тестах → второй получит 409 «уже занято» и упадёт. В задании мы используем разные имена (`anna`, `boris`, `vera`), а в уроке 9.10 начнём генерировать уникальные.

```task
id: m09-t03
title: Регистрация с моделями
module: exercises/m09-rest-assured
test: RegistrationApiTestCheck
file: exercises/m09-rest-assured/src/test/java/ru/qacourse/m09/tasks/RegistrationApiTest.java
solution: solutions/exercises/m09-rest-assured/src/test/java/ru/qacourse/m09/tasks/RegistrationApiTest.java
text: |
  1. Допишите модели в `src/main/java/ru/qacourse/m09/models`: `RegisterRequest` (Lombok) и `UserResponse` (record).
  2. В `RegistrationApiTest` — три теста: успешная регистрация (201 + проверка полей через модель), повтор email (409), пароль короче 6 (400).

  Проверка ловит BUG-201 и BUG-202.
hint: |
  Для повторного email: зарегистрируйте `boris`, затем создайте запрос для `boris2` и `setEmail(...)` первого.
```

```quiz
- q: Что делает .extract().as(UserResponse.class)?
  options: ["Превращает тело ответа в объект UserResponse", "Отправляет объект", "Проверяет статус"]
  answer: 0
- q: Чего не хватает, если POST с объектом вернул 415?
  options: ["contentType(ContentType.JSON)", "Токена", "queryParam"]
  answer: 0
- q: Зачем @JsonIgnoreProperties(ignoreUnknown = true) у модели ответа?
  options:
    - "Чтобы лишние поля ответа (которых нет в модели) не ломали разбор"
    - "Чтобы игнорировать все поля"
    - "Для ускорения"
  answer: 0
- q: Какой способ задать тело удобнее для «сломанного» JSON (поле не того типа)?
  options: ["Map или строка", "Только POJO", "Никакой"]
  answer: 0
```
