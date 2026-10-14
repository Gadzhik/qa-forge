---
title: "5.5 Stream API: основы"
minutes: 45
---

**Stream API** — способ обрабатывать коллекции «конвейером»: отфильтровать → преобразовать → отсортировать → собрать результат. Код становится коротким и читается как описание задачи.

Сравните. Задача: названия напитков по алфавиту.

```java
// Циклом
List<String> result = new ArrayList<>();
for (Item i : items) {
    if (i.category().equals("DRINK")) {
        result.add(i.name());
    }
}
Collections.sort(result);
```

```java
// Стримом
List<String> result = items.stream()
        .filter(i -> i.category().equals("DRINK"))
        .map(Item::name)
        .sorted()
        .toList();
```

В автотестах стримы повсюду: отфильтровать товары из ответа API, достать список цен, проверить, что все элементы удовлетворяют условию.

## Как устроен стрим

```text
источник  →  промежуточные операции  →  терминальная операция
items.stream()  .filter(...).map(...).sorted()   .toList()
```

1. **Источник** — `collection.stream()`, `Stream.of(a, b, c)`, `Arrays.stream(array)`.
2. **Промежуточные** операции возвращают новый стрим; их можно цеплять. Они **ленивые**: ничего не делают, пока не вызвана терминальная.
3. **Терминальная** операция запускает конвейер и выдаёт результат (список, число, boolean...).

Стрим **одноразовый**: после терминальной операции его нельзя использовать снова. Исходная коллекция **не меняется**.

## Промежуточные операции

| Операция | Что делает | Пример |
|---|---|---|
| `filter(predicate)` | Оставить подходящие | `.filter(i -> i.stock() > 0)` |
| `map(function)` | Преобразовать каждый | `.map(Item::name)` |
| `sorted()` | Сортировать (естественный порядок) | `.sorted()` |
| `sorted(comparator)` | Сортировать по правилу | `.sorted(Comparator.comparing(Item::price))` |
| `distinct()` | Убрать дубликаты | `.distinct()` |
| `limit(n)` | Первые n | `.limit(3)` |
| `skip(n)` | Пропустить n | `.skip(5)` |
| `peek(action)` | Подсмотреть (для отладки) | `.peek(System.out::println)` |
| `mapToInt / mapToDouble` | В числовой стрим | `.mapToInt(Item::stock)` |

## Терминальные операции

| Операция | Результат | Пример |
|---|---|---|
| `toList()` | `List` (неизменяемый) | `.toList()` |
| `collect(Collectors...)` | Что угодно (урок 5.6) | `.collect(Collectors.toSet())` |
| `count()` | Количество | `.count()` |
| `anyMatch(p)` | Хоть один подходит? | `.anyMatch(i -> i.stock() == 0)` |
| `allMatch(p)` | Все подходят? | `.allMatch(i -> i.price() > 0)` |
| `noneMatch(p)` | Ни один не подходит? | `.noneMatch(i -> i.price() < 0)` |
| `findFirst()` | `Optional` первого | `.findFirst()` |
| `min / max(comparator)` | `Optional` | `.max(Comparator.comparing(Item::price))` |
| `forEach(action)` | Действие для каждого | `.forEach(System.out::println)` |
| `sum / average` (у `IntStream` и т.п.) | Число | `.mapToInt(Item::stock).sum()` |
| `reduce` | Свёртка в одно значение | `.reduce(0, Integer::sum)` |

## Разбор примеров из задания

**Суммарный остаток:**

```java
return items.stream()          // стрим из списка Item
        .mapToInt(Item::stock) // IntStream остатков: 50, 40, 100, ...
        .sum();                // сумма
```

`mapToInt` нужен, потому что у обычного `Stream<Integer>` нет метода `sum()`, а у числового `IntStream` — есть.

**Три самых дорогих:**

```java
return items.stream()
        .sorted((a, b) -> Double.compare(b.price(), a.price()))  // по убыванию цены
        .limit(3)                                               // первые три
        .map(Item::name)                                        // только названия
        .toList();
```

`Double.compare(b, a)` (b и a переставлены) даёт обратный порядок. То же: `.sorted(Comparator.comparingDouble(Item::price).reversed())`.

> ⚠️ **Порядок операций важен.** `.limit(3).sorted(...)` — взять первые три как попало, потом отсортировать их; `.sorted(...).limit(3)` — отсортировать всё и взять три лучших.

## Отладка стримов

Если цепочка выдаёт не то, вставьте `peek`:

```java
items.stream()
        .filter(i -> i.price() < 100)
        .peek(i -> System.out.println("после filter: " + i))
        .map(Item::name)
        .toList();
```

А в IDEA при отладке есть кнопка **Trace Current Stream Chain** в окне Debug — она показывает, что происходило на каждом шаге.

## Стримы в API-тестах — превью

```java
List<ProductDto> products = getProducts();

assertThat(products.stream().map(ProductDto::price).toList()).isSortedAccordingTo(Comparator.reverseOrder());
assertThat(products.stream().allMatch(p -> p.category().equals("DRINK"))).isTrue();
long outOfStock = products.stream().filter(p -> p.stock() == 0).count();
```

```task
id: m05-t05
title: Stream API — основы
module: exercises/m05-java-advanced
test: Task05StreamsCheck
file: exercises/m05-java-advanced/src/main/java/ru/qacourse/m05/Task05Streams.java
solution: solutions/exercises/m05-java-advanced/src/main/java/ru/qacourse/m05/Task05Streams.java
text: |
  Каждый метод — одна цепочка `items.stream()...`:
  - `namesInCategory` — названия категории по алфавиту;
  - `totalStock` — сумма остатков;
  - `anyOutOfStock` / `allPricesPositive`;
  - `top3Expensive` — названия трёх самых дорогих.
hint: |
  `filter` → `map` → `sorted` → `toList`; `mapToInt(...).sum()`; `anyMatch`/`allMatch`; `sorted(...)` → `limit(3)` → `map` → `toList`.
```

```quiz
- q: Что произойдёт, если вызвать только промежуточные операции без терминальной?
  options: [Ничего — стрим ленивый, Элементы обработаются, Ошибка компиляции]
  answer: 0
- q: Меняет ли stream().filter(...) исходный список?
  options: ["Да", "Нет"]
  answer: 1
- q: Какой метод проверяет, что ВСЕ элементы удовлетворяют условию?
  options: [anyMatch, allMatch, noneMatch]
  answer: 1
- q: Почему для суммы используют mapToInt(...).sum()?
  options:
    - У числового IntStream есть sum(), у Stream<Integer> — нет
    - Так быстрее печатается
    - sum() есть у любого стрима
  answer: 0
- q: Чем отличаются .sorted().limit(3) и .limit(3).sorted()?
  options:
    - Первое — три лучших из всех, второе — три первых, потом отсортированные
    - Ничем
    - Второе не скомпилируется
  answer: 0
```
