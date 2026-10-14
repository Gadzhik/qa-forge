---
title: "7.4 Параметризованные тесты"
minutes: 40
---

Граничные значения пароля: длина 5, 6, 30, 31, без цифры, `null`, пустая строка... Восемь почти одинаковых тестов, которые различаются только входными данными. **Параметризованный тест** — один метод, который JUnit запускает много раз с разными данными.

Нужна библиотека `junit-jupiter-params` — она уже входит в `junit-jupiter`.

## @ValueSource — список значений

```java
@ParameterizedTest(name = "«{0}» — корректный")
@ValueSource(strings = {"abcde1", "secret12", "123456789012345678901234567890"})
void valid(String password) {
    assertThat(PasswordValidator.isValid(password)).isTrue();
}
```

- `@ParameterizedTest` вместо `@Test`;
- `@ValueSource(strings = {...})` — источник данных: метод выполнится для каждого значения; есть `ints`, `longs`, `doubles`, `booleans`...
- параметр метода `String password` получает очередное значение;
- `name = "«{0}» — корректный"` — как назвать каждый запуск в отчёте: `{0}` — первый аргумент, `{index}` — номер запуска. Без `name` будет `[1] abcde1`, `[2] secret12`.

В IDEA каждый набор данных виден как отдельный тест.

## Пустые значения

```java
@ParameterizedTest
@NullSource                      // один запуск с null
@EmptySource                     // один запуск с ""
@ValueSource(strings = {"abcd1", "abcdefgh"})
void invalid(String password) {
    assertThat(PasswordValidator.isValid(password)).isFalse();
}
```

`@NullAndEmptySource` — оба сразу. `@ValueSource` не умеет передавать `null`, поэтому эти аннотации полезны.

## @CsvSource — несколько аргументов

```java
@ParameterizedTest(name = "{0} → {1}")
@CsvSource({
        "abcde1,   true",
        "abcd1,    false",
        "abcdefgh, false",
        "'',       false"
})
void validation(String password, boolean expected) {
    assertThat(PasswordValidator.isValid(password)).isEqualTo(expected);
}
```

- каждая строка — один запуск; значения через запятую;
- пробелы вокруг значений обрезаются;
- `''` — пустая строка; пустое место (`,  ,`) — `null`;
- JUnit сам преобразует текст в типы параметров (`"true"` → `boolean`, `"5"` → `int`, `"PAID"` → enum).

Удобно держать большие таблицы в файле: `@CsvFileSource(resources = "/passwords.csv", numLinesToSkip = 1)`.

## @MethodSource — данные из метода

Когда данные сложные (объекты, вычисления):

```java
static Stream<Arguments> orders() {
    return Stream.of(
            Arguments.of("SALE10", new BigDecimal("180.00")),
            Arguments.of(null, new BigDecimal("200.00"))
    );
}

@ParameterizedTest
@MethodSource("orders")
void totalWithPromo(String promo, BigDecimal expected) {
    Cart cart = new Cart();
    cart.add("Бургер", "200.00", 1);
    if (promo != null) cart.applyPromo(promo);
    assertThat(cart.total()).isEqualByComparingTo(expected);
}
```

- метод-источник `static`, возвращает `Stream<Arguments>` (или `List`, или массив);
- `Arguments.of(...)` — набор аргументов для одного запуска;
- в `@MethodSource("orders")` — имя метода.

## @EnumSource — все значения enum

```java
@ParameterizedTest
@EnumSource(value = OrderStatus.class, names = {"READY", "CANCELLED"})
void finalStatuses(OrderStatus status) {
    assertThat(status.isFinal()).isTrue();
}
```

Без `names` — по одному запуску на каждое значение enum.

## Параметризация в API-тестах

Самое частое применение — негативные сценарии:

```java
@ParameterizedTest(name = "quantity={0} → 400")
@ValueSource(ints = {0, -1, 100})
void invalidQuantity(int quantity) {
    given().spec(auth).body(Map.of("productId", 1, "quantity", quantity))
            .post("/api/cart/items")
            .then().statusCode(400);
}
```

Три теста граничных значений одним методом.

> ⚠️ Не превращайте один параметризованный тест в «комбайн» с `if (expected) ... else ...` внутри. Если логика проверки разная, напишите два метода: «корректные» и «некорректные». Так и в задании.

```task
id: m07-t04
title: Параметризованные тесты пароля
module: exercises/m07-junit
test: PasswordValidatorTestCheck
file: exercises/m07-junit/src/test/java/ru/qacourse/m07/tasks/PasswordValidatorTest.java
solution: solutions/exercises/m07-junit/src/test/java/ru/qacourse/m07/tasks/PasswordValidatorTest.java
text: |
  Протестируйте `PasswordValidator.isValid` параметризованными тестами (хотя бы один `@ParameterizedTest`).
  Требования: длина 6–30 включительно, хотя бы одна цифра, `null` — некорректен.
  Проверка включит ошибки «минимум 5», «максимум 31» и «цифра не нужна».
hint: |
  Граничные значения длины: 5 и 6, 30 и 31 символ. Пароль из 30 символов: `"123456789012345678901234567890"`.
  Для проверки цифры нужен пароль нормальной длины без цифр: `"abcdefgh"`.
```

```quiz
- q: Какой источник передаст в тест null?
  options: ["@NullSource", "@ValueSource(strings = {null})", "@CsvSource({\"null\"})"]
  answer: 0
- q: Как в @CsvSource передать пустую строку?
  options: ["''", "Пустое место между запятыми", "null"]
  answer: 0
- q: Каким должен быть метод для @MethodSource?
  options: [static и возвращает Stream/List аргументов, private void, Любым]
  answer: 0
- q: Что означает {0} в name = "{0} → ок"?
  options: [Первый аргумент запуска, Номер запуска, Имя метода]
  answer: 0
```
