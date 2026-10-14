---
title: "4.7 Полиморфизм"
minutes: 30
---

**Полиморфизм** («много форм») — третий кит ООП. Суть: **один и тот же код работает с объектами разных классов**, а каждый объект ведёт себя по-своему.

## Пример

```java
DiscountRule[] rules = {
        new NoDiscount(),
        new PercentDiscount(10),
        new FixedDiscount(15_000)
};

for (DiscountRule rule : rules) {
    System.out.println(rule.title() + ": " + rule.apply(100_000));
}
```

```text
Без скидки: 100000
Скидка 10%: 90000
Скидка 150 ₽: 85000
```

Цикл написан **один раз** и вызывает `rule.apply(...)`, не зная, что за правило внутри. Java во время выполнения смотрит на **реальный класс объекта** и вызывает его версию метода. Это называется **динамическое связывание** (позднее связывание).

## Статический и динамический тип

```java
DiscountRule rule = new PercentDiscount(10);
```

- **Статический** тип (тип переменной) — `DiscountRule`. Компилятор по нему решает, **какие методы можно вызвать**: только `apply` и `title`.
- **Динамический** тип (тип объекта) — `PercentDiscount`. По нему JVM решает, **какая реализация выполнится**.

То же с наследованием:

```java
User u = new Admin("boss");
u.canDeleteProducts();   // true — выполнилась версия Admin
```

## Зачем это на практике

Добавить новую скидку «каждый пятый бургер бесплатно» можно, **не меняя** ни строчки в коде, который применяет скидки: просто новый класс `implements DiscountRule`. Это принцип **открытости/закрытости** (Open/Closed): код открыт для расширения, закрыт для изменения.

В автотестах так устроены:
- **WebDriver** — тесты одни, браузеры разные;
- **Page Object** — общий интерфейс `Page` с методом `isOpened()`;
- **источники тестовых данных** — `TestDataProvider` из файла, из базы, сгенерированные.

## Приведение типов и instanceof

Иногда нужно понять, какой объект на самом деле:

```java
if (rule instanceof PercentDiscount percent) {
    System.out.println("Это процентная скидка");
}
```

`instanceof` проверяет динамический тип. Но если код постоянно спрашивает «а ты кто?», это признак плохого дизайна — лучше добавить метод в интерфейс.

Ассерт AssertJ `isInstanceOf(FixedDiscount.class)` в проверке задания — тоже проверка динамического типа.

## Перегрузка vs переопределение — частый вопрос

| | Перегрузка (overloading) | Переопределение (overriding) |
|---|---|---|
| Где | В одном классе | В наследнике / реализации |
| Параметры | Разные | Те же самые |
| Когда выбирается версия | При **компиляции** (по типам аргументов) | При **выполнении** (по типу объекта) |
| Пример | `priceWithDiscount(price)` и `priceWithDiscount(price, percent)` | `Admin.canDeleteProducts()` |

```task
id: m04-t07
title: Лучшая скидка
module: exercises/m04-java-oop
test: DiscountCalculatorCheck
file: exercises/m04-java-oop/src/main/java/ru/qacourse/m04/DiscountCalculator.java
solution: solutions/exercises/m04-java-oop/src/main/java/ru/qacourse/m04/DiscountCalculator.java
text: |
  - `best(amount, rules)` — правило, дающее самую маленькую итоговую сумму;
  - `bestPrice(amount, rules)` — итоговая сумма с этим правилом.

  Ваш код не должен знать о конкретных классах скидок — только об интерфейсе `DiscountRule`.
  Проверка подсунет ему даже правило, которого нет в проекте.
hint: |
  Поиск минимума из урока 3.8: начните с `rules[0]` и сравнивайте `rule.apply(amount)` с текущим лучшим.
  `bestPrice` — одна строка: `return best(amount, rules).apply(amount);`
```

```quiz
- q: Что такое полиморфизм?
  options:
    - Один код работает с объектами разных классов, и каждый ведёт себя по-своему
    - Наследование от нескольких классов
    - Несколько конструкторов
  answer: 0
- q: "`User u = new Admin(\"a\"); u.canDeleteProducts();` — чья версия метода выполнится?"
  options: [User, Admin, Ошибка компиляции]
  answer: 1
- q: Когда выбирается версия переопределённого метода?
  options: [При компиляции, При выполнении — по реальному типу объекта]
  answer: 1
- q: Как добавить новый вид скидки, не меняя DiscountCalculator?
  options:
    - Написать новый класс, реализующий DiscountRule
    - Добавить if в DiscountCalculator
    - Никак
  answer: 0
```
