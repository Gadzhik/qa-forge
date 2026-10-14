---
title: "9.10 Тестовые данные: Datafaker и мягкие проверки"
minutes: 35
---

## Проблема жёстко заданных данных

```java
RegisterRequest request = new RegisterRequest("anna", "anna@test.local", "Secret1");
```

Тест прошёл один раз. Второй запуск на том же стенде — **409**: `anna` уже зарегистрирована. Тесты, создающие данные, должны генерировать **уникальные** значения. А ещё полезно тестировать на разнообразных, «живых» данных — имена с дефисами, длинные email и т.д.

## Datafaker

**Datafaker** — библиотека генерации правдоподобных данных (наследник JavaFaker):

```xml
<dependency>
    <groupId>net.datafaker</groupId>
    <artifactId>datafaker</artifactId>
    <scope>test</scope>
</dependency>
```

```java
Faker faker = new Faker();                        // английские данные
Faker ru = new Faker(new Locale("ru"));           // русские: имена, адреса, телефоны

faker.name().firstName();              // "Joan"
ru.name().fullName();                  // "Анна Сергеевна Иванова"
faker.internet().emailAddress();       // "joan.smith@example.com"
faker.internet().password(8, 16);      // случайный пароль длиной 8–15
ru.phoneNumber().cellPhone();          // "+7 (912) 345-67-89"
ru.address().fullAddress();            // "г. Казань, ул. Ленина, д. 12"
faker.number().numberBetween(1, 100);  // 1..99
faker.number().digits(6);              // "394821"
faker.lorem().sentence();              // текст для отзыва
faker.food().dish();                   // название блюда
faker.options().option("BURGER", "DRINK");  // одно из значений
```

## Генерация пользователя

```java
private final Faker faker = new Faker();

private RegisterRequest randomUser() {
    String username = faker.name().firstName().toLowerCase().replaceAll("[^a-z]", "") + faker.number().digits(6);
    return RegisterRequest.builder()
            .username(username)
            .email(faker.internet().emailAddress(username))
            .password("Pw" + faker.internet().password(6, 12) + "1")
            .build();
}
```

Разбор:
- `firstName().toLowerCase()` + `replaceAll("[^a-z]", "")` — имя латиницей без апострофов и пробелов (`O'Neil` → `oneil`); правила имени пользователя у API строгие;
- `+ digits(6)` — шесть случайных цифр для уникальности;
- `emailAddress(username)` — email на основе имени;
- пароль: `"Pw"` + случайная часть + `"1"` — гарантируем заглавные/строчные буквы и цифру независимо от случайности. **Генератор не знает ваших правил валидации** — всегда подгоняйте данные под требования.

> 💡 **Воспроизводимость.** Случайные данные — это хорошо, пока тест не упал «только на этих данных». Логируйте сгенерированное (или прикладывайте в отчёт Allure), а для отладки можно зафиксировать зерно: `new Faker(new Random(42))`.

> ⚠️ Не генерируйте случайно то, что влияет на **ожидаемый результат**, если не можете это ожидание посчитать. «Случайная цена товара и проверка суммы» — нормально, если сумма вычисляется в тесте из той же цены. «Случайный промокод и ожидание скидки» — нет.

## Мягкие проверки для ответа

Ответ регистрации — 4 поля. Обычные проверки остановятся на первом несовпадении; мягкие покажут все проблемы сразу (урок 7.3):

```java
UserResponse created = given(Specs.base()).body(request).post("/api/auth/register")
        .then().statusCode(201).extract().as(UserResponse.class);

SoftAssertions.assertSoftly(softly -> {
    softly.assertThat(created.id()).isPositive();
    softly.assertThat(created.username()).isEqualTo(request.getUsername());
    softly.assertThat(created.email()).isEqualTo(request.getEmail());
    softly.assertThat(created.role()).isEqualTo("USER");
});
```

Статус проверяем **жёстко** (`statusCode(201)`) — без созданного пользователя нет смысла проверять поля.

## Проверка регистра пароля

```java
@Test
void passwordIsCaseSensitive() {
    RegisterRequest request = randomUser();
    given(Specs.base()).body(request).post("/api/auth/register").then().statusCode(201);

    given(Specs.base()).body(new LoginRequest(request.getUsername(), request.getPassword()))
            .post("/api/auth/login").then().statusCode(200);
    given(Specs.base()).body(new LoginRequest(request.getUsername(), request.getPassword().toUpperCase()))
            .post("/api/auth/login").then().statusCode(401);
}
```

Сначала **позитивная** проверка того же пароля (200) — без неё 401 на втором запросе мог бы означать что угодно (например, пользователь не создался). Это общий приём: **докажите, что негативный результат вызван именно тем, что вы проверяете**.

```task
id: m09-t10
title: Datafaker и SoftAssertions
module: exercises/m09-rest-assured
test: FakerSoftTestCheck
file: exercises/m09-rest-assured/src/test/java/ru/qacourse/m09/tasks/FakerSoftTest.java
solution: solutions/exercises/m09-rest-assured/src/test/java/ru/qacourse/m09/tasks/FakerSoftTest.java
text: |
  1. Генератор случайного пользователя на Datafaker и тест регистрации с мягкими проверками всех полей.
  2. Тот же пользователь не входит паролем в верхнем регистре (401), но входит правильным (200).

  Проверка ловит BUG-203.
hint: |
  Импорты: `net.datafaker.Faker`, `org.assertj.core.api.SoftAssertions`. Следите, чтобы username был 3–30 символов, а пароль — не короче 6 и с цифрой.
```

```quiz
- q: Почему в тестах создания данных генерируют уникальные значения?
  options:
    - "Повторный запуск с теми же данными упадёт (например, 409 — уже существует)"
    - "Так красивее"
    - "Datafaker обязателен"
  answer: 0
- q: Что делает new Faker(new Locale("ru"))?
  options: ["Генерирует русские имена, адреса, телефоны", "Переводит код", "Ничего"]
  answer: 0
- q: Зачем перед проверкой «неверный пароль → 401» проверить «верный пароль → 200»?
  options:
    - "Доказать, что 401 вызван именно регистром пароля, а не другой проблемой"
    - "Для ускорения"
    - "Это лишнее"
  answer: 0
- q: Как сделать случайные данные воспроизводимыми?
  options: ["Логировать их или зафиксировать зерно new Faker(new Random(42))", "Никак", "Запускать тест дважды"]
  answer: 0
```
