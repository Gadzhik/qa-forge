---
title: "5.3 Коллекции: Set и Map"
minutes: 40
---

## Set — множество без дубликатов

```java
Set<String> categories = new HashSet<>();
categories.add("DRINK");
categories.add("BURGER");
boolean added = categories.add("DRINK");    // false — уже есть
System.out.println(categories.size());       // 2
```

`add` возвращает `false`, если элемент уже был. Уникальность определяется через `equals` и `hashCode` — вот почему их так важно правильно переопределять (урок 4.4).

### Три реализации

| Класс | Порядок обхода | Когда использовать |
|---|---|---|
| `HashSet` | Непредсказуемый | По умолчанию, самый быстрый |
| `LinkedHashSet` | Порядок добавления | Нужны уникальные элементы и важен порядок |
| `TreeSet` | Отсортированный | Нужна сортировка |

```java
List<String> raw = List.of("DRINK", "BURGER", "DRINK", "SIDE");
new HashSet<>(raw);         // [SIDE, DRINK, BURGER] — порядок может быть любым!
new LinkedHashSet<>(raw);   // [DRINK, BURGER, SIDE] — как появлялись
new TreeSet<>(raw);         // [BURGER, DRINK, SIDE] — по алфавиту
```

> ⚠️ Никогда не полагайтесь на порядок `HashSet` и `HashMap` в тестах. Сегодня тест прошёл, а на другой версии Java или с другими данными порядок изменится — и тест «замигает».

**Типичные задачи в тестах:**
- проверить, что в ответе нет дубликатов: `assertThat(new HashSet<>(ids)).hasSameSizeAs(ids);` или проще `assertThat(ids).doesNotHaveDuplicates();`
- сравнить наборы без учёта порядка: `assertThat(actualSet).isEqualTo(expectedSet);`

## Map — словарь «ключ → значение»

```java
Map<String, Integer> stock = new HashMap<>();
stock.put("Кола", 200);                 // положить пару
stock.put("Чизкейк", 12);
stock.put("Кола", 150);                 // ключ уже есть → значение ЗАМЕНЯЕТСЯ
int cola = stock.get("Кола");           // 150
Integer pizza = stock.get("Пицца");     // null — такого ключа нет!
```

- `Map<String, Integer>` — ключи строки, значения целые;
- `put(k, v)` — добавить или заменить;
- `get(k)` — получить значение или `null`.

> ⚠️ `int x = stock.get("Пицца");` — **NullPointerException**: `get` вернул `null`, а распаковать `null` в `int` нельзя. Используйте `getOrDefault`.

Основные методы:

```java
stock.getOrDefault("Пицца", 0);     // 0 — значение по умолчанию, если ключа нет
stock.containsKey("Кола");          // true
stock.containsValue(12);            // true
stock.remove("Кола");               // удалить пару
stock.size();
stock.keySet();                     // Set всех ключей
stock.values();                     // коллекция всех значений
stock.entrySet();                   // Set пар (ключ, значение)
stock.putIfAbsent("Кола", 100);     // положить, только если ключа нет
stock.merge("Кола", 1, Integer::sum); // прибавить 1 к существующему (или положить 1)
```

### Перебор Map

```java
for (Map.Entry<String, Integer> entry : stock.entrySet()) {
    System.out.println(entry.getKey() + " → " + entry.getValue());
}

stock.forEach((name, qty) -> System.out.println(name + " → " + qty));
```

`Map.Entry` — пара «ключ-значение»; `getKey()` и `getValue()` достают части.

### Подсчёт — классический приём

```java
Map<String, Integer> counts = new HashMap<>();
for (String w : words) {
    counts.put(w, counts.getOrDefault(w, 0) + 1);
}
```

Для каждого слова: взять текущее число (или 0, если слова ещё не было), прибавить 1, положить обратно.

### Реализации Map

| Класс | Порядок ключей |
|---|---|
| `HashMap` | Непредсказуемый |
| `LinkedHashMap` | Порядок добавления |
| `TreeMap` | Отсортированный |

`Map.of("Кола", 200, "Фри", 100)` — неизменяемая Map (до 10 пар).

## Map в автотестах

Map — это по сути JSON-объект. В REST Assured часто передают тело запроса или параметры как Map:

```java
Map<String, Object> body = Map.of(
        "productId", 1,
        "quantity", 2);
given().body(body).post("/api/cart/items");
```

А заголовки ответа, cookies, query-параметры — тоже Map.

Проверки в AssertJ:

```java
assertThat(stock).containsEntry("Кола", 200)
                 .containsKey("Фри")
                 .doesNotContainKey("Пицца")
                 .hasSize(2);
```

```task
id: m05-t03
title: Set и Map
module: exercises/m05-java-advanced
test: Task03SetsMapsCheck
file: exercises/m05-java-advanced/src/main/java/ru/qacourse/m05/Task03SetsMaps.java
solution: solutions/exercises/m05-java-advanced/src/main/java/ru/qacourse/m05/Task03SetsMaps.java
text: |
  - `uniqueCategories(categories)` — уникальные в порядке появления;
  - `countWords(words)` — Map «слово → сколько раз»;
  - `stockOf(stock, name)` — остаток или 0;
  - `outOfStock(stock)` — названия с нулевым остатком по алфавиту.
hint: |
  - Порядок появления сохраняет `LinkedHashSet`.
  - `getOrDefault` — ваш друг в `countWords` и `stockOf`.
  - В `outOfStock` переберите `entrySet()`, соберите ключи в `ArrayList`, затем `Collections.sort(...)`.
```

```quiz
- q: Какой Set сохраняет порядок добавления?
  options: [HashSet, LinkedHashSet, TreeSet]
  answer: 1
- q: Что вернёт map.get(k), если ключа k нет?
  options: [0, null, Бросит исключение]
  answer: 1
- q: Что произойдёт при put с уже существующим ключом?
  options: [Значение заменится, Будет дубликат ключа, Исключение]
  answer: 0
- q: Почему нельзя полагаться на порядок HashMap в тестах?
  options:
    - Он непредсказуем и может измениться, тест станет нестабильным
    - HashMap всегда сортирует
    - Можно полагаться
  answer: 0
- q: Как перебрать и ключи, и значения Map?
  options: ["entrySet()", "keySet() и потом get()", "Оба варианта работают, но entrySet() удобнее и быстрее"]
  answer: 2
```
