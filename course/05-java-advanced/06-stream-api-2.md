---
title: "5.6 Stream API: группировка и Collectors"
minutes: 40
---

`toList()` собирает элементы в список. Но часто нужен другой результат: Map, строка, сумма по группам. Для этого есть терминальная операция `collect(...)` и готовые «сборщики» из класса **`Collectors`**.

```java
import java.util.stream.Collectors;
```

## Основные сборщики

| Сборщик | Результат | Пример |
|---|---|---|
| `Collectors.toList()` | `List` (изменяемый) | `.collect(Collectors.toList())` |
| `Collectors.toSet()` | `Set` | `.collect(Collectors.toSet())` |
| `Collectors.joining(", ")` | Строка через разделитель | `.map(Item::name).collect(Collectors.joining(", "))` |
| `Collectors.toMap(k, v)` | `Map` | `.collect(Collectors.toMap(Item::name, Item::price))` |
| `Collectors.groupingBy(f)` | `Map<ключ, List<элемент>>` | `.collect(Collectors.groupingBy(Item::category))` |
| `Collectors.groupingBy(f, downstream)` | `Map<ключ, итог по группе>` | см. ниже |
| `Collectors.partitioningBy(p)` | `Map<Boolean, List>` — две группы | `.collect(Collectors.partitioningBy(i -> i.stock() > 0))` |
| `Collectors.counting()` | Количество | (внутри groupingBy) |
| `Collectors.summingInt/Double(f)` | Сумма | (внутри groupingBy) |
| `Collectors.averagingDouble(f)` | Среднее | (внутри groupingBy) |

## joining — склейка строк

```java
String names = items.stream()
        .map(Item::name)
        .collect(Collectors.joining(", "));
// "Классический бургер, Чизбургер двойной, Картофель фри, ..."
```

Есть версия с префиксом и суффиксом: `joining(", ", "[", "]")` → `"[a, b]"`.

## toMap — в словарь

```java
Map<String, Double> priceByName = items.stream()
        .collect(Collectors.toMap(Item::name, Item::price));
```

Первая функция — **ключ**, вторая — **значение**.

> ⚠️ Если два элемента дадут одинаковый ключ, `toMap` бросит `IllegalStateException: Duplicate key`. Решение — третий аргумент, говорящий, что делать при совпадении: `toMap(Item::name, Item::price, (a, b) -> a)` — оставить первое.

## groupingBy — группировка

Самый мощный сборщик. Разбивает элементы на группы по ключу:

```java
Map<String, List<Item>> byCategory = items.stream()
        .collect(Collectors.groupingBy(Item::category));
// {BURGER=[...2 товара...], DRINK=[...4...], SIDE=[...], DESSERT=[...]}
```

С **вторым аргументом** (downstream-сборщиком) вместо списка получаем итог по каждой группе:

```java
Map<String, Long> countByCategory = items.stream()
        .collect(Collectors.groupingBy(Item::category, Collectors.counting()));
// {BURGER=2, DRINK=4, SIDE=1, DESSERT=2}

Map<String, Double> avgPrice = items.stream()
        .collect(Collectors.groupingBy(Item::category, Collectors.averagingDouble(Item::price)));

Map<String, Integer> stockByCategory = items.stream()
        .collect(Collectors.groupingBy(Item::category, Collectors.summingInt(Item::stock)));

Map<String, List<String>> namesByCategory = items.stream()
        .collect(Collectors.groupingBy(Item::category,
                Collectors.mapping(Item::name, Collectors.toList())));
```

Читается так: «сгруппируй по категории, а внутри каждой группы — посчитай / усредни / сложи / собери имена».

`counting()` возвращает `Long`, поэтому тип `Map<String, Long>`.

## partitioningBy — на две кучки

```java
Map<Boolean, List<Item>> inStock = items.stream()
        .collect(Collectors.partitioningBy(i -> i.stock() > 0));
List<Item> available = inStock.get(true);
List<Item> soldOut = inStock.get(false);
```

## min/max и Optional

```java
Optional<Item> cheapestDrink = items.stream()
        .filter(i -> i.category().equals("DRINK"))
        .min(Comparator.comparingDouble(Item::price));
```

`min` и `max` возвращают **`Optional`** — потому что стрим мог оказаться пустым (напитков нет), и тогда минимума не существует. Что такое `Optional` — в следующем уроке.

## Практическое применение в тестах

Проверка ответа API «в каждой категории хотя бы один товар в наличии»:

```java
Map<String, Long> available = products.stream()
        .filter(p -> p.stock() > 0)
        .collect(Collectors.groupingBy(ProductDto::category, Collectors.counting()));
assertThat(available).containsKeys("BURGER", "SIDE", "DRINK", "DESSERT");
```

Отчёт о результатах прогона, разбор логов, подготовка тестовых данных — всё это задачи для `groupingBy`.

```task
id: m05-t06
title: Группировка и Collectors
module: exercises/m05-java-advanced
test: Task06CollectorsCheck
file: exercises/m05-java-advanced/src/main/java/ru/qacourse/m05/Task06Collectors.java
solution: solutions/exercises/m05-java-advanced/src/main/java/ru/qacourse/m05/Task06Collectors.java
text: |
  - `countByCategory` — `groupingBy` + `counting`;
  - `averagePriceByCategory` — `groupingBy` + `averagingDouble`;
  - `joinNames` — `joining(", ")`;
  - `priceByName` — `toMap`;
  - `cheapestIn(items, category)` — `filter` + `min`, результат `Optional`.
hint: |
  Все пять методов — по одной цепочке. Примеры почти дословно есть в уроке.
```

```quiz
- q: "Что вернёт `groupingBy(Item::category)` без второго аргумента?"
  options: ["Map<String, List<Item>>", "Map<String, Long>", "List<String>"]
  answer: 0
- q: Что произойдёт в toMap при двух одинаковых ключах (без функции слияния)?
  options: [IllegalStateException Duplicate key, Останется последнее значение, Останется первое]
  answer: 0
- q: Какой сборщик разделит элементы на две группы по условию?
  options: [groupingBy, partitioningBy, joining]
  answer: 1
- q: Почему min() возвращает Optional?
  options:
    - Стрим может быть пустым — минимума тогда нет
    - Это ошибка API
    - Чтобы было медленнее
  answer: 0
```
