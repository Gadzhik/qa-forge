---
title: "5.2 Коллекции: List"
minutes: 40
---

Массив неудобен: длина фиксирована, добавить или удалить элемент сложно. В реальном коде почти всегда используют **коллекции** — классы из пакета `java.util`, которые растут и сжимаются сами.

Главные интерфейсы коллекций:

| Интерфейс | Что это | Порядок | Дубликаты | Основная реализация |
|---|---|---|---|---|
| **List** | Список | Есть, по индексу | Можно | `ArrayList` |
| **Set** | Множество | Зависит от реализации | Нельзя | `HashSet`, `LinkedHashSet`, `TreeSet` |
| **Map** | Словарь «ключ → значение» | Зависит от реализации | Ключи уникальны | `HashMap`, `LinkedHashMap`, `TreeMap` |

В автотестах: список товаров из ответа API — `List`, набор уникальных категорий — `Set`, заголовки HTTP или тело запроса — `Map`.

## ArrayList

```java
List<String> cart = new ArrayList<>();
```

- `List<String>` — тип переменной: **интерфейс** «список строк» (помните урок 4.6?);
- `new ArrayList<>()` — конкретная реализация: список на основе массива, который сам увеличивается;
- `<String>` — тип элементов (generics, урок 4.9). Хранить примитивы нельзя: `List<int>` — ошибка, нужно `List<Integer>`.

```java
cart.add("Кола");                  // добавить в конец        → [Кола]
cart.add("Фри");                   //                          → [Кола, Фри]
cart.add(0, "Бургер");             // вставить по индексу 0    → [Бургер, Кола, Фри]
String first = cart.get(0);        // получить по индексу      → "Бургер"
cart.set(1, "Лимонад");            // заменить                 → [Бургер, Лимонад, Фри]
cart.remove("Фри");                // удалить по значению      → [Бургер, Лимонад]
cart.remove(0);                    // удалить по индексу       → [Лимонад]
int size = cart.size();            // размер                   → 1
boolean has = cart.contains("Кола"); // есть ли               → false
int idx = cart.indexOf("Лимонад"); // индекс или -1           → 0
boolean empty = cart.isEmpty();    // пустой ли
cart.clear();                      // очистить
```

> ⚠️ `list.remove(1)` для `List<Integer>` удалит элемент **по индексу 1**, а не число 1. Чтобы удалить значение: `list.remove(Integer.valueOf(1))`.

## Неизменяемые списки

```java
List<String> categories = List.of("BURGER", "SIDE", "DRINK");
categories.add("PIZZA");   // 💥 UnsupportedOperationException
```

`List.of(...)` создаёт **неизменяемый** список — удобно для констант и тестовых данных. Если нужен изменяемый — оберните: `new ArrayList<>(List.of(...))`.

`Arrays.asList(...)` — старый способ, список фиксированного размера (менять элементы можно, добавлять нельзя). Встречается в старом коде.

## Перебор

```java
for (String item : cart) {                       // for-each — чаще всего
    System.out.println(item);
}

for (int i = 0; i < cart.size(); i++) {           // с индексом
    System.out.println(i + ": " + cart.get(i));
}

cart.forEach(item -> System.out.println(item));   // лямбда (урок 5.4)
```

> ⚠️ **Нельзя удалять элементы из списка внутри for-each** — будет `ConcurrentModificationException`:
> ```java
> for (String s : lines) {
>     if (s.isBlank()) lines.remove(s);   // 💥
> }
> ```
> Правильно: `lines.removeIf(s -> s.isBlank());` или `lines.removeIf(String::isBlank);`.

## Полезные операции

```java
List<Integer> nums = new ArrayList<>(List.of(5, 2, 9, 1));
Collections.sort(nums);                         // [1, 2, 5, 9] — на месте
nums.sort(Comparator.reverseOrder());           // [9, 5, 2, 1]
Collections.max(nums);                          // 9
List<Integer> part = nums.subList(0, 2);        // [9, 5] — «окно» в исходный список!
List<Integer> copy = new ArrayList<>(nums);     // независимая копия
nums.addAll(List.of(7, 8));                     // добавить все
```

`subList` возвращает не копию, а «вид» на часть исходного списка: изменения видны в обе стороны. Для независимой части: `new ArrayList<>(nums.subList(0, 2))`.

## Сравнение списков в тестах

```java
assertThat(actualNames).containsExactly("Кола", "Фри");          // те же элементы в том же порядке
assertThat(actualNames).containsExactlyInAnyOrder("Фри", "Кола"); // порядок не важен
assertThat(actualNames).contains("Кола");                          // хотя бы этот
assertThat(actualNames).hasSize(2).doesNotContain("Пиво");
assertThat(prices).isSorted();                                     // по возрастанию
```

`List.equals` сравнивает поэлементно с учётом порядка, поэтому `assertThat(list1).isEqualTo(list2)` тоже работает.

## LinkedList — для общего развития

Вторая реализация `List` — связный список. Быстрые вставки в начало и середину, медленный доступ по индексу. На практике в 99% случаев выбирают `ArrayList`. Вопрос «чем ArrayList отличается от LinkedList» любят на собеседованиях: ArrayList — на массиве (быстрый `get(i)`, медленная вставка в середину), LinkedList — цепочка узлов (быстрая вставка при наличии позиции, медленный `get(i)`).

```task
id: m05-t02
title: Работа со списками
module: exercises/m05-java-advanced
test: Task02ListsCheck
file: exercises/m05-java-advanced/src/main/java/ru/qacourse/m05/Task02Lists.java
solution: solutions/exercises/m05-java-advanced/src/main/java/ru/qacourse/m05/Task02Lists.java
text: |
  - `addIfAbsent(names, name)` — добавить, если ещё нет; вернуть, добавили ли;
  - `cheaperThan(prices, max)` — **новый** список цен ≤ max;
  - `removeBlank(lines)` — удалить пустые строки прямо в переданном списке;
  - `firstN(items, n)` — первые n элементов новым списком.
hint: |
  - `removeBlank`: `lines.removeIf(String::isBlank);`
  - `firstN`: `new ArrayList<>(items.subList(0, Math.min(n, items.size())))`.
```

```quiz
- q: Чем List отличается от Set?
  options:
    - В List есть порядок по индексу и дубликаты, в Set дубликатов нет
    - Ничем
    - Set быстрее растёт
  answer: 0
- q: Что произойдёт при List.of("a").add("b")?
  options: [Добавится, UnsupportedOperationException, Ошибка компиляции]
  answer: 1
- q: Как безопасно удалить элементы по условию?
  options: ["removeIf(...)", "remove() внутри for-each", "Нельзя"]
  answer: 0
- q: Какой ассерт проверит элементы с учётом порядка?
  options: [contains, containsExactly, containsExactlyInAnyOrder]
  answer: 1
- q: Почему пишут `List<String> x = new ArrayList<>()`, а не `ArrayList<String> x`?
  options:
    - Код зависит от интерфейса и не привязан к реализации
    - Так требует компилятор
    - ArrayList нельзя использовать как тип
  answer: 0
```
