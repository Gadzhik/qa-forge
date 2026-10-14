---
title: "3.8 Массивы"
minutes: 35
---

**Массив** — набор значений **одного типа** фиксированной длины, у каждого элемента есть номер — **индекс** (с нуля).

## Создание

```java
int[] quantities = new int[3];              // [0, 0, 0]
double[] prices = {289.9, 99.0, 129.0};      // сразу со значениями
String[] names = {"Кола", "Фри", "Бургер"};
```

- `int[]` — тип «массив целых»; квадратные скобки после типа означают массив;
- `new int[3]` — создать массив на 3 элемента. Элементы получают **значения по умолчанию**: `0` для чисел, `false` для boolean, `null` для объектов;
- `{...}` — создать и заполнить сразу (длина определяется по количеству значений).

Массив — ссылочный тип: переменная хранит ссылку (вспомните урок 3.3).

## Доступ к элементам

```java
double first = prices[0];        // 289.9
prices[1] = 89.0;                // заменить второй элемент
int len = prices.length;         // 3 — длина (поле, а не метод: без скобок!)
double last = prices[prices.length - 1];   // последний элемент
```

> ⚠️ Индексы: от `0` до `length - 1`. Обращение к `prices[3]` при длине 3 — ошибка **`ArrayIndexOutOfBoundsException: Index 3 out of bounds for length 3`**. Это родственник off-by-one.

> ⚠️ Длину массива изменить нельзя. Нужен «растущий» список — используйте `List` (модуль 5). В автотестах `List` встречается намного чаще массивов.

## Перебор

```java
for (int i = 0; i < prices.length; i++) {
    System.out.println(i + ": " + prices[i]);   // индекс нужен
}

for (double p : prices) {
    System.out.println(p);                      // индекс не нужен
}
```

## Типичные алгоритмы

**Максимум:**

```java
int max = numbers[0];             // начинаем с первого элемента, а не с 0!
for (int n : numbers) {
    if (n > max) {
        max = n;
    }
}
```

Почему не `int max = 0`? Если все числа отрицательные (`{-5, -2, -8}`), ответ `0` будет неверным — его даже нет в массиве. Такая ошибка спрятана в задании 3.10.

**Проверка сортировки:**

```java
for (int i = 1; i < numbers.length; i++) {
    if (numbers[i] < numbers[i - 1]) {
        return false;             // нашли пару «по убыванию» — не отсортирован
    }
}
return true;
```

Цикл с `i = 1`, потому что сравниваем каждый элемент с **предыдущим**. Именно так мы будем проверять сортировку товаров в API-тесте (BUG-102).

**Разворот в новый массив:**

```java
int[] result = new int[numbers.length];
for (int i = 0; i < numbers.length; i++) {
    result[i] = numbers[numbers.length - 1 - i];
}
```

Для `i = 0` берём последний (`length - 1`), для `i = 1` — предпоследний и т.д.

## Класс Arrays

Готовые утилиты в `java.util.Arrays` (нужен `import java.util.Arrays;` в начале файла — IDEA добавит по Alt+Enter):

```java
int[] nums = {5, 2, 9, 1};
System.out.println(nums);                    // [I@1b6d3586 — адрес, а не содержимое!
System.out.println(Arrays.toString(nums));   // [5, 2, 9, 1]
Arrays.sort(nums);                           // сортирует на месте: [1, 2, 5, 9]
int[] copy = Arrays.copyOf(nums, nums.length);
boolean same = Arrays.equals(nums, copy);    // true — сравнение по содержимому
```

> 💡 `nums == copy` — `false` (разные объекты), `Arrays.equals(nums, copy)` — `true`. Та же история, что со строками.

## Двумерные массивы

Массив массивов — таблица:

```java
int[][] table = {
    {1, 2, 3},
    {4, 5, 6}
};
int x = table[1][2];   // строка 1, столбец 2 → 6
```

Встречаются редко; в тестах вместо них — списки объектов.

## Параметр main — тоже массив

`String[] args` в `main` — массив аргументов командной строки:

```java
public static void main(String[] args) {
    System.out.println("Аргументов: " + args.length);
}
```

`java Hello раз два` → `Аргументов: 2`.

```task
id: m03-t08
title: Массивы
module: exercises/m03-java-basics
test: Task08ArraysCheck
file: exercises/m03-java-basics/src/main/java/ru/qacourse/m03/Task08Arrays.java
solution: solutions/exercises/m03-java-basics/src/main/java/ru/qacourse/m03/Task08Arrays.java
text: |
  - `max(numbers)` — максимум (работает и для отрицательных);
  - `reversed(numbers)` — **новый** массив в обратном порядке, исходный не менять;
  - `isSorted(numbers)` — по неубыванию; пустой массив считается отсортированным;
  - `countAbove(prices, threshold)` — сколько цен строго больше порога.
hint: |
  Все четыре алгоритма разобраны в уроке. Для `reversed` создайте `new int[numbers.length]`.
```

```quiz
- q: Индекс последнего элемента массива arr — это...
  options: ["arr.length", "arr.length - 1", "arr.length()"]
  answer: 1
- q: Что содержит `new boolean[2]`?
  options: ["[true, true]", "[false, false]", "[null, null]"]
  answer: 1
- q: Что напечатает `System.out.println(new int[]{1, 2})`?
  options: ["[1, 2]", "Что-то вроде [I@1b6d3586", "1 2"]
  answer: 1
  explain: Для печати содержимого используйте Arrays.toString(...).
- q: Почему максимум ищут, начиная с numbers[0], а не с 0?
  options:
    - Если все элементы отрицательные, 0 даст неверный ответ
    - Так быстрее
    - Без разницы
  answer: 0
- q: Можно ли увеличить длину существующего массива?
  options: ["Да, через arr.length = 10", "Нет, длина массива фиксирована"]
  answer: 1
```
