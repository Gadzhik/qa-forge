---
title: "5.4 Лямбды и функциональные интерфейсы"
minutes: 40
---

## Проблема: передать «действие» как значение

Хотим метод, который фильтрует товары. Но по какому условию? Дешевле 100? Нет в наличии? Категория «напитки»? Писать отдельный метод под каждое условие — дублирование. Хочется передать **само условие** как параметр.

В уроке 4.6 мы делали так с интерфейсом и анонимным классом:

```java
DiscountRule free = new DiscountRule() {
    @Override
    public long apply(long amount) { return 0; }
    ...
};
```

Громоздко. **Лямбда** — короткая запись того же самого для интерфейсов с **одним** абстрактным методом.

## Синтаксис лямбды

```java
Predicate<Item> cheap = item -> item.price() < 100;
```

- `Predicate<Item>` — тип: «условие для Item» (встроенный интерфейс, о нём ниже);
- `item` — параметр (тип выведется сам — `Item`);
- `->` — «стрелка»: слева параметры, справа тело;
- `item.price() < 100` — тело; его значение и есть результат.

Варианты записи:

```java
() -> 42                                  // без параметров
x -> x * 2                                // один параметр — скобки не нужны
(a, b) -> a + b                           // несколько — в скобках
(String s) -> s.length()                  // с явным типом
x -> {                                    // несколько строк — фигурные скобки и return
    int doubled = x * 2;
    return doubled + 1;
}
```

## Функциональные интерфейсы

**Функциональный интерфейс** — интерфейс с ровно одним абстрактным методом. Лямбду можно подставить везде, где ожидается такой интерфейс. Самые нужные лежат в `java.util.function`:

| Интерфейс | Метод | Что делает | Пример |
|---|---|---|---|
| `Predicate<T>` | `boolean test(T t)` | Условие | `i -> i.stock() == 0` |
| `Function<T, R>` | `R apply(T t)` | Превращает T в R | `i -> i.name()` |
| `Consumer<T>` | `void accept(T t)` | Делает что-то с T | `s -> System.out.println(s)` |
| `Supplier<T>` | `T get()` | Поставляет значение | `() -> new ArrayList<>()` |
| `BiFunction<T, U, R>` | `R apply(T t, U u)` | Из двух значений одно | `(a, b) -> a + b` |
| `UnaryOperator<T>` | `T apply(T t)` | T → T | `s -> s.trim()` |
| `Comparator<T>` | `int compare(T a, T b)` | Сравнение для сортировки | `(a, b) -> a.name().compareTo(b.name())` |
| `Runnable` | `void run()` | Просто действие | `() -> click()` |
| `Callable<T>` | `T call()` | Действие с результатом | `() -> fetchStatus()` |

### Пример: фильтр с условием-параметром

```java
public static List<Item> filter(List<Item> items, Predicate<Item> condition) {
    List<Item> result = new ArrayList<>();
    for (Item item : items) {
        if (condition.test(item)) {      // вызываем переданное условие
            result.add(item);
        }
    }
    return result;
}

filter(menu, i -> i.price() < 100);          // дешёвые
filter(menu, i -> i.stock() == 0);           // закончились
filter(menu, i -> i.category().equals("DRINK"));
```

Один метод — бесконечно много условий.

## Ссылки на методы

Если лямбда просто вызывает существующий метод, её можно записать короче через `::`:

| Лямбда | Ссылка на метод |
|---|---|
| `s -> s.toUpperCase()` | `String::toUpperCase` |
| `i -> i.name()` | `Item::name` |
| `s -> System.out.println(s)` | `System.out::println` |
| `s -> Integer.parseInt(s)` | `Integer::parseInt` |
| `() -> new ArrayList<>()` | `ArrayList::new` |

## Comparator — сортировка по правилам

```java
List<Item> sorted = new ArrayList<>(menu);
sorted.sort(Comparator.comparingDouble(Item::price));                 // по цене
sorted.sort(Comparator.comparing(Item::name));                         // по названию
sorted.sort(Comparator.comparingDouble(Item::price).reversed());      // по цене по убыванию
sorted.sort(Comparator.comparingDouble(Item::price)
                      .thenComparing(Item::name));                     // по цене, при равенстве — по названию
```

- `Comparator.comparing(функция)` — сравнивать по значению, которое возвращает функция;
- `comparingDouble`/`comparingInt` — то же для чисел (без лишней упаковки);
- `.reversed()` — обратный порядок;
- `.thenComparing(...)` — дополнительный критерий при равенстве.

Метод `compare(a, b)` возвращает отрицательное число, если `a` «меньше» (должен идти раньше), 0 — если равны, положительное — если больше.

## Захват переменных

Лямбда может использовать переменные снаружи, но только **effectively final** — те, что после присваивания не меняются:

```java
double maxPrice = 100;
Predicate<Item> cheap = i -> i.price() < maxPrice;   // ✅

int count = 0;
menu.forEach(i -> count++);                          // ❌ ошибка: count меняется
```

## Лямбды в автотестах

Вы будете писать их постоянно:

```java
assertThatThrownBy(() -> service.validate(0))                     // передаём «вызов»
        .isInstanceOf(IllegalArgumentException.class);

await().until(() -> getStatus(orderId).equals("READY"));          // Awaitility: ждать условие

$$(".product-card").filter(visible).shouldHave(size(20));         // Selenide

assertThat(products).allSatisfy(p -> assertThat(p.price()).isPositive());
```

```task
id: m05-t04
title: Лямбды и Comparator
module: exercises/m05-java-advanced
test: Task04LambdasCheck
file: exercises/m05-java-advanced/src/main/java/ru/qacourse/m05/Task04Lambdas.java
solution: solutions/exercises/m05-java-advanced/src/main/java/ru/qacourse/m05/Task04Lambdas.java
text: |
  - `cheaperThan(maxPrice)` — вернуть лямбду-условие «цена строго меньше»;
  - `filter(items, condition)` — цикл + `condition.test(item)`;
  - `sortByPriceThenName(items)` — новый отсортированный список;
  - `map(source, f)` — применить функцию к каждому элементу.
hint: |
  - `return item -> item.price() < maxPrice;`
  - `copy.sort(Comparator.comparingDouble(Item::price).thenComparing(Item::name));`
  - В `map`: `result.add(f.apply(t));`
```

```quiz
- q: Что такое функциональный интерфейс?
  options:
    - Интерфейс с ровно одним абстрактным методом
    - Интерфейс с методами-функциями
    - Любой интерфейс
  answer: 0
- q: Какой интерфейс подходит для условия «true/false»?
  options: [Function, Predicate, Supplier]
  answer: 1
- q: Как короче записать `s -> s.trim()`?
  options: ["String::trim", "s::trim", "trim()"]
  answer: 0
- q: Что вернёт compare(a, b), если a должен идти раньше b?
  options: [Отрицательное число, Ноль, Положительное число]
  answer: 0
- q: Почему в assertThatThrownBy передают лямбду, а не результат вызова?
  options:
    - Чтобы AssertJ сам выполнил вызов и поймал исключение
    - Так короче
    - Лямбды работают быстрее
  answer: 0
```
