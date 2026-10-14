---
title: "4.9 Generics (обобщения)"
minutes: 30
---

## Проблема

Нужен класс «пара значений»: товар и количество, логин и пароль, ожидаемое и фактическое. Без обобщений пришлось бы писать `StringIntPair`, `StringStringPair`... или хранить `Object` и постоянно приводить типы с риском ошибки.

**Generics** (обобщения) позволяют написать класс один раз, а тип указать при использовании.

## Обобщённый класс

```java
public class Pair<A, B> {
    private final A first;
    private final B second;

    public Pair(A first, B second) {
        this.first = first;
        this.second = second;
    }

    public A getFirst() { return first; }
    public B getSecond() { return second; }
}
```

- `<A, B>` после имени класса — **параметры типа**: «какие-то два типа, назовём их A и B». Принято обозначать одной заглавной буквой: `T` (Type), `E` (Element), `K`/`V` (Key/Value);
- внутри класса `A` и `B` используются как обычные типы.

```java
Pair<String, Integer> item = new Pair<>("Кола", 2);
String name = item.getFirst();     // компилятор знает, что это String — приведение не нужно
int qty = item.getSecond();        // Integer → int автоматически
```

- `Pair<String, Integer>` — подставили конкретные типы;
- `new Pair<>(...)` — «ромб» `<>`: компилятор сам выводит типы из левой части.

> ⚠️ Параметром типа может быть только **ссылочный** тип: `Pair<String, int>` — ошибка, нужен `Integer`. Вот где пригождаются обёртки из урока 3.3.

## Обобщённый метод

Параметр типа можно объявить и у отдельного метода:

```java
public static <T> T firstNonNull(T a, T b) {
    return a != null ? a : b;
}
```

`<T>` перед типом результата — объявление параметра типа метода. Вызов: `firstNonNull(null, "b")` — компилятор выведет `T = String`.

## Где вы встретите generics (постоянно)

```java
List<String> names = new ArrayList<>();          // список строк
Map<String, Integer> stock = new HashMap<>();    // ключ — название, значение — остаток
Optional<User> user = findUser("anna");          // «может быть, пользователь» (модуль 5)
```

И в тестовых библиотеках:

```java
List<ProductDto> products = response.jsonPath().getList("content", ProductDto.class);
ProductDto product = response.as(ProductDto.class);
```

`Class<T>` в последних примерах — способ передать тип в метод, чтобы библиотека знала, в объекты какого класса превращать JSON.

## Ограничения типа (для общего развития)

```java
public static <T extends Comparable<T>> T max(T a, T b) {
    return a.compareTo(b) >= 0 ? a : b;
}
```

`T extends Comparable<T>` — «любой тип, который умеет сравниваться». Без ограничения у `T` нельзя было бы вызвать `compareTo`.

Знак вопроса `?` (wildcard) встречается в сигнатурах библиотек: `List<? extends Number>` — «список чего-то, что является числом». Писать такое самому придётся редко, а читать — иногда.

## Static и generics

Обобщения — про типы, `static` — про принадлежность классу. Статический обобщённый метод (`firstNonNull`) объявляет свой `<T>`, потому что параметры типа класса (`A`, `B`) у статического метода недоступны: они относятся к конкретному объекту `Pair<String, Integer>`, а статический метод работает без объекта.

## Итоги модуля

Вы освоили основы ООП:
- **класс и объект**, поля, конструкторы, методы;
- **инкапсуляция**: `private` поля, доступ через методы;
- **static**: общее для класса;
- **equals/hashCode/toString**;
- **наследование** (`extends`, `super`, `abstract`);
- **интерфейсы** и **полиморфизм**;
- **enum** и **record**;
- **generics**.

С этим набором можно читать код любого тестового фреймворка. В следующем модуле — инструменты Java, без которых не обходится ни один автотест: коллекции, исключения, лямбды и Stream API.

```task
id: m04-t09
title: Обобщённая пара
module: exercises/m04-java-oop
test: PairCheck
file: exercises/m04-java-oop/src/main/java/ru/qacourse/m04/Pair.java
solution: solutions/exercises/m04-java-oop/src/main/java/ru/qacourse/m04/Pair.java
text: |
  - `swap()` — новая пара с переставленными значениями (обратите внимание на тип результата `Pair<B, A>`);
  - `firstNonNull(a, b)` — первый аргумент, если он не `null`, иначе второй.
hint: |
  `return new Pair<>(second, first);`
```

```quiz
- q: Что означает `<T>` в объявлении `class Box<T>`?
  options:
    - Параметр типа — конкретный тип подставят при использовании
    - Тег HTML
    - Тип Text
  answer: 0
- q: Почему нельзя написать `List<int>`?
  options:
    - Параметром типа может быть только ссылочный тип — нужен Integer
    - int слишком маленький
    - Можно, это корректно
  answer: 0
- q: Что такое «ромб» в `new Pair<>("a", 1)`?
  options:
    - Компилятор сам выводит параметры типа
    - Пустая пара
    - Ошибка
  answer: 0
- q: "`Map<String, Integer>` — что здесь ключ?"
  options: [String, Integer, Map]
  answer: 0
```
