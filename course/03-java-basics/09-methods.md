---
title: "3.9 Методы"
minutes: 40
---

**Метод** — именованный кусок кода, который можно вызывать много раз. Методы позволяют:
- не повторять код (правило **DRY** — Don't Repeat Yourself);
- дать действию понятное имя (`login(...)`, `addToCart(...)`) — код читается как текст;
- разбить большую задачу на маленькие.

В автотестах методы — всё: шаги (`openCatalog()`, `addProduct(id)`), проверки, подготовка данных.

## Анатомия метода

```java
public static double priceWithDiscount(double price, int percent) {
    return price - price * percent / 100.0;
}
```

| Часть | Значение |
|---|---|
| `public` | модификатор доступа: метод виден из других классов |
| `static` | метод класса, вызывается без создания объекта (модуль 4) |
| `double` | **тип возвращаемого значения** |
| `priceWithDiscount` | **имя** — глагол или фраза, camelCase |
| `(double price, int percent)` | **параметры**: тип и имя каждого, через запятую |
| `{ ... }` | **тело** |
| `return ...;` | вернуть результат и **сразу** выйти из метода |

Объявление `public static double priceWithDiscount(double price, int percent)` называют **сигнатурой** (строго говоря, сигнатура — имя + типы параметров).

## Вызов

```java
double result = priceWithDiscount(200, 10);    // 180.0
System.out.println(priceWithDiscount(99, 50)); // 49.5
```

При вызове значения **аргументов** (`200`, `10`) копируются в **параметры** (`price`, `percent`). Порядок и типы должны совпадать с объявлением.

Метод того же класса вызывается просто по имени. Из другого класса — через имя класса: `Task09Methods.priceWithDiscount(200, 10)`. Именно так проверки (`Task09MethodsCheck`) вызывают ваши методы.

## void — метод без результата

```java
public static void printReceipt(String product, double total) {
    System.out.println(product + ": " + total + " ₽");
}
```

`void` — ничего не возвращает. `return;` без значения можно использовать для досрочного выхода.

## Параметры передаются по значению

```java
public static void tryToChange(int x) {
    x = 100;             // меняем КОПИЮ
}

int a = 5;
tryToChange(a);
System.out.println(a);   // 5
```

Для ссылочных типов копируется **ссылка** — поэтому изменение **содержимого** объекта видно снаружи:

```java
public static void fill(int[] arr) {
    arr[0] = 100;        // меняем объект, на который указывают обе ссылки
}
int[] data = {1, 2};
fill(data);
System.out.println(data[0]);   // 100
```

Классический вопрос собеседования: «В Java передача по значению или по ссылке?» Ответ: **всегда по значению**; для объектов значением является ссылка.

## Перегрузка (overloading)

Несколько методов с **одним именем**, но **разными параметрами**:

```java
public static double priceWithDiscount(double price, int percent) {
    return price - price * percent / 100.0;
}

public static double priceWithDiscount(double price) {
    return priceWithDiscount(price, 0);        // переиспользуем первую версию
}
```

Компилятор выбирает нужную версию по количеству и типам аргументов. Вы уже пользовались перегрузкой: `System.out.println` умеет печатать и `int`, и `String`, и `double` — это разные методы с одним именем.

> ⚠️ Перегрузка только по типу возвращаемого значения невозможна: `int f()` и `double f()` вместе — ошибка компиляции.

## Разбиение на маленькие методы

Сравните:

```java
// ❌ всё в одном месте
public static double cartTotal(double[] prices, int[] quantities) {
    double total = 0;
    for (int i = 0; i < prices.length; i++) {
        total += prices[i] * quantities[i];
    }
    return total;
}
```

```java
// ✅ логика строки — в отдельном методе с понятным именем
public static double cartTotal(double[] prices, int[] quantities) {
    double total = 0;
    for (int i = 0; i < prices.length; i++) {
        total += lineTotal(prices[i], quantities[i]);
    }
    return total;
}

public static double lineTotal(double price, int quantity) {
    return price * quantity;
}
```

Теперь `lineTotal` можно протестировать отдельно и переиспользовать. Хороший метод делает **одну вещь** и помещается на экран.

## Рекурсия

Метод может вызывать сам себя:

```java
public static long factorial(int n) {
    if (n <= 1) return 1;              // базовый случай — остановка
    return n * factorial(n - 1);       // рекурсивный вызов
}
```

Без базового случая вызовы будут бесконечными, и программа упадёт со `StackOverflowError`. В автотестах рекурсия почти не встречается, но на собеседованиях спрашивают.

## Именование методов

- глагол: `calculateTotal`, `addToCart`, `isValid`, `hasDiscount`;
- методы, возвращающие `boolean`, начинают с `is`, `has`, `can`, `should`;
- в тестах имена описывают проверку: `paidOrderCannotBeCancelled()`.

```task
id: m03-t09
title: Методы и перегрузка
module: exercises/m03-java-basics
test: Task09MethodsCheck
file: exercises/m03-java-basics/src/main/java/ru/qacourse/m03/Task09Methods.java
solution: solutions/exercises/m03-java-basics/src/main/java/ru/qacourse/m03/Task09Methods.java
text: |
  - `lineTotal(price, quantity)` — цена × количество;
  - `cartTotal(prices, quantities)` — сумма по всем строкам, **используя `lineTotal`**;
  - `priceWithDiscount(price, percent)` — цена со скидкой;
  - `priceWithDiscount(price)` — перегрузка со скидкой 0%, вызывающая первую версию.
hint: |
  Скидка: `price - price * percent / 100.0`. Делим на `100.0`, а не на `100` — так деление гарантированно дробное
  (вспомните ловушку целочисленного деления из урока 3.3: `10 / 100` в int равно 0).
  Перегрузка: `return priceWithDiscount(price, 0);`
```

```quiz
- q: Что означает void в объявлении метода?
  options: [Метод ничего не возвращает, Метод пустой, Метод статический]
  answer: 0
- q: "Что напечатает код: `static void inc(int x) { x++; }` ... `int a = 1; inc(a); println(a);`"
  options: ["1", "2", "Ошибку"]
  answer: 0
  explain: Примитив передаётся копией.
- q: Что такое перегрузка методов?
  options:
    - Несколько методов с одним именем, но разными параметрами
    - Метод, который вызывает сам себя
    - Слишком длинный метод
  answer: 0
- q: Как назвать метод, проверяющий, оплачен ли заказ?
  options: ["paid", "isPaid", "checkPaidOrNotAndReturnTrueIfPaid"]
  answer: 1
- q: Что произойдёт, если у рекурсии нет базового случая?
  options: [StackOverflowError, Метод вернёт 0, Ничего]
  answer: 0
```
