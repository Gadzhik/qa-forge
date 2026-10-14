---
title: "4.6 Интерфейсы"
minutes: 35
---

## Идея

**Интерфейс** — это **контракт**: список того, что объект **умеет делать**, без описания, **как**.

В QA Burger есть разные виды скидок: процентная (10%), фиксированная (150 ₽), «без скидки». Код оформления заказа не должен знать подробности каждой — ему нужно только «примени скидку к сумме». Опишем это интерфейсом:

```java
public interface DiscountRule {
    long apply(long amountKopecks);
    String title();
}
```

- `interface` вместо `class`;
- методы без тела — только сигнатуры. Они автоматически `public` и `abstract`.

## Реализация

```java
public class PercentDiscount implements DiscountRule {
    private final int percent;

    public PercentDiscount(int percent) {
        this.percent = percent;
    }

    @Override
    public long apply(long amountKopecks) {
        return amountKopecks - amountKopecks * percent / 100;
    }

    @Override
    public String title() {
        return "Скидка " + percent + "%";
    }
}
```

- `implements DiscountRule` — «реализует контракт DiscountRule». Класс **обязан** реализовать все методы интерфейса, иначе ошибка компиляции (IDEA подскажет: Alt+Enter → *Implement methods*);
- класс может реализовать **несколько** интерфейсов: `class X implements A, B, C` (в отличие от одного родителя в `extends`).

## Тип переменной — интерфейс

```java
DiscountRule rule = new PercentDiscount(10);
long total = rule.apply(100_000);   // 90 000 копеек
```

Переменная имеет тип **интерфейса**, а объект — конкретного класса. Через переменную доступны только методы контракта. Это и есть главная сила интерфейсов (подробнее — следующий урок).

## Интерфейс или абстрактный класс?

| | Интерфейс | Абстрактный класс |
|---|---|---|
| Сколько можно «унаследовать» | Несколько | Один |
| Поля (состояние) | Только константы | Любые |
| Конструктор | Нет | Есть |
| Смысл | «Умеет делать» (can-do) | «Является разновидностью» (is-a) с общим кодом |

Правило: начинайте с интерфейса. Абстрактный класс — когда у наследников много общего кода и состояния.

## default-методы

С Java 8 интерфейс может содержать методы с реализацией по умолчанию:

```java
public interface DiscountRule {
    long apply(long amountKopecks);
    String title();

    default boolean isFree(long amountKopecks) {
        return apply(amountKopecks) == 0;
    }
}
```

Реализации получают `isFree` бесплатно, но могут переопределить.

## Интерфейсы вокруг вас

Вы будете постоянно работать с интерфейсами из библиотек, часто не замечая этого:

| Интерфейс | Где | Что значит |
|---|---|---|
| `List`, `Set`, `Map` | Коллекции (модуль 5) | `List<String> names = new ArrayList<>();` — тип интерфейс, объект — реализация |
| `Comparable`, `Comparator` | Сортировка | «Умеет сравниваться» |
| `Runnable`, `Function` | Лямбды (модуль 5) | «Умеет выполниться / превратить X в Y» |
| `WebDriver` | Selenium (модуль 10) | `WebDriver driver = new ChromeDriver();` — тест не зависит от браузера |
| `RequestSpecification` | REST Assured | Настройки запроса |
| `Extension` | JUnit 5 | Расширения тестов (модуль 7) |

Строка `WebDriver driver = new ChromeDriver();` — та же идея, что `DiscountRule rule = new PercentDiscount(10);`. Поменяете `ChromeDriver` на `FirefoxDriver` — остальной код теста не изменится.

## Анонимный класс

Реализацию можно создать «на месте», без отдельного файла:

```java
DiscountRule free = new DiscountRule() {
    @Override
    public long apply(long amount) { return 0; }
    @Override
    public String title() { return "Бесплатно"; }
};
```

Такую запись вы увидите в проверке задания 4.7. В модуле 5 для интерфейсов с одним методом это сократится до лямбды `x -> 0`.

```task
id: m04-t06
title: Правила скидок
module: exercises/m04-java-oop
test: DiscountCheck
file: exercises/m04-java-oop/src/main/java/ru/qacourse/m04/PercentDiscount.java
solution: solutions/exercises/m04-java-oop/src/main/java/ru/qacourse/m04/PercentDiscount.java
text: |
  Реализуйте два класса, которые уже объявлены как `implements DiscountRule`:
  - `PercentDiscount` — `apply` (отбрасывая дробные копейки) и `title()` вида «Скидка 10%»;
  - `FixedDiscount` (файл рядом) — `apply` не уходит ниже 0, `title()` вида «Скидка 150 ₽».

  Посмотрите на готовую реализацию `NoDiscount` как на образец.
hint: |
  - Процент: `amount - amount * percent / 100` (все long — деление целочисленное, это и нужно).
  - Фиксированная: `Math.max(0, amount - discountKopecks)`; рубли — `discountKopecks / 100`.
```

```quiz
- q: Что такое интерфейс?
  options:
    - Контракт — набор методов, которые обязан реализовать класс
    - Графический интерфейс программы
    - Родительский класс с полями
  answer: 0
- q: Сколько интерфейсов может реализовать класс?
  options: ["Только один", "Сколько угодно"]
  answer: 1
- q: "Почему пишут `WebDriver driver = new ChromeDriver();`, а не `ChromeDriver driver = ...`?"
  options:
    - Код теста зависит только от контракта WebDriver, браузер легко заменить
    - Так короче
    - ChromeDriver нельзя присвоить переменной своего типа
  answer: 0
- q: Что будет, если класс implements интерфейс, но не реализует один из его методов?
  options: [Ошибка компиляции, Метод вернёт null, Ничего]
  answer: 0
```
