---
title: "5.7 Optional"
minutes: 25
---

## Проблема null

Метод «найти товар по названию». Что вернуть, если товара нет? Раньше возвращали `null`, и вызывающий код должен был помнить про проверку — а забывал, и получал `NullPointerException` где-то далеко.

**`Optional<T>`** — «коробка», в которой **может быть** значение типа T, а может быть пусто. Тип результата `Optional<Item>` честно говорит: «товара может не оказаться, обработай этот случай».

## Создание

```java
Optional<String> some = Optional.of("SALE10");        // точно есть значение (null → NPE)
Optional<String> none = Optional.empty();             // пусто
Optional<String> maybe = Optional.ofNullable(value);  // value может быть null → тогда пусто
```

Стримы возвращают Optional сами: `findFirst()`, `min()`, `max()`.

## Как достать значение

| Метод | Если значение есть | Если пусто |
|---|---|---|
| `isPresent()` / `isEmpty()` | `true` / `false` | `false` / `true` |
| `get()` | значение | 💥 `NoSuchElementException` |
| `orElse(x)` | значение | `x` |
| `orElseGet(() -> x)` | значение | вычислить `x` (лениво) |
| `orElseThrow()` | значение | 💥 `NoSuchElementException` |
| `orElseThrow(() -> new MyEx(...))` | значение | ваше исключение |
| `ifPresent(action)` | выполнить действие | ничего |

```java
double price = findByName(items, "Пицца")
        .map(Item::price)        // Optional<Item> → Optional<Double>
        .orElse(0.0);            // если товара нет — 0.0
```

- `map` применяет функцию к значению **внутри** коробки, если оно есть; если пусто — остаётся пусто;
- `filter(условие)` — оставляет значение, только если оно подходит.

```java
String category = findByName(items, name)
        .map(Item::category)
        .orElseThrow(() -> new IllegalArgumentException("Нет товара: " + name));
```

> ⚠️ Не пишите `if (opt.isPresent()) { x = opt.get(); }` везде — это тот же `null`-чек, только длиннее. Используйте `map`, `orElse`, `orElseThrow`.

> ⚠️ `get()` без проверки — то же самое, что разыменовать `null`. В современном коде вместо него пишут `orElseThrow()` — смысл тот же, но явно видно, что здесь может быть исключение.

## Цепочка: строка из настроек

```java
Optional<String> promo = Optional.ofNullable(System.getenv("PROMO"))
        .map(String::trim)
        .filter(s -> !s.isEmpty());
```

`null`, пустая строка и строка из пробелов превращаются в `Optional.empty()`, а нормальное значение — в обрезанную строку. Это задание `nonBlank`.

## Где не надо использовать Optional

- Для **полей** классов и **параметров** методов — не принято.
- Для **коллекций**: вместо `Optional<List<X>>` возвращайте пустой список.

Optional — для **возвращаемых значений**, которых может не быть.

## Optional в тестах

```java
assertThat(findByName(items, "Чизкейк")).isPresent();
assertThat(findByName(items, "Пицца")).isEmpty();
assertThat(cheapestIn(items, "DRINK")).map(Item::name).hasValue("Вода без газа");
```

```task
id: m05-t07
title: Optional
module: exercises/m05-java-advanced
test: Task07OptionalCheck
file: exercises/m05-java-advanced/src/main/java/ru/qacourse/m05/Task07Optional.java
solution: solutions/exercises/m05-java-advanced/src/main/java/ru/qacourse/m05/Task07Optional.java
text: |
  - `findByName` — `stream().filter(...).findFirst()`;
  - `priceOrZero` — через `findByName`, `map` и `orElse`;
  - `categoryOrThrow` — `orElseThrow` со своим исключением;
  - `nonBlank` — `ofNullable` → `map(String::trim)` → `filter`.
hint: |
  Ни в одном методе не нужны `if` и `get()`.
```

```quiz
- q: Что вернёт Optional.ofNullable(null)?
  options: [Optional.empty(), NullPointerException, Optional с null внутри]
  answer: 0
- q: Чем orElse отличается от orElseGet?
  options:
    - orElseGet вычисляет значение по умолчанию лениво — только если коробка пуста
    - Ничем
    - orElse бросает исключение
  answer: 0
- q: Что сделает .map(f) на пустом Optional?
  options: [Вернёт пустой Optional, Бросит NPE, Вызовет f с null]
  answer: 0
- q: Где Optional использовать НЕ принято?
  options: [Как тип поля класса, Как возвращаемое значение поиска]
  answer: 0
```
