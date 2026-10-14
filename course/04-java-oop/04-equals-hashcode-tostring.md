---
title: "4.4 equals, hashCode и toString"
minutes: 40
---

У каждого класса Java есть «предок» — класс **`Object`**. От него все объекты получают несколько методов. Три из них постоянно нужны в автотестах.

## toString — как объект выглядит текстом

```java
Money price = Money.of(289, 90);
System.out.println(price);    // ru.qacourse.m04.Money@6d06d69c
```

`println` вызывает у объекта `toString()`. Версия из `Object` печатает имя класса и непонятный хеш. Переопределим:

```java
@Override
public String toString() {
    return String.format("%d.%02d ₽", kopecks / 100, kopecks % 100);
}
```

- `@Override` — **аннотация** «этот метод переопределяет метод предка». Необязательна, но обязательна по смыслу: если вы ошибётесь в имени (`tostring`), компилятор скажет, что переопределять нечего. Без аннотации вы бы молча создали новый метод;
- `%d` — целое; `%02d` — целое, минимум 2 цифры, дополнить нулями слева (5 → `05`).

**Зачем в тестах:** когда проверка падает, AssertJ печатает объекты через `toString()`. Сравните сообщения:

```text
expected: ru.qacourse.m04.Money@1b6d3586  but was: ru.qacourse.m04.Money@4554617c
expected: 289.90 ₽  but was: 289.91 ₽
```

Со вторым сразу понятно, что сломалось.

## equals — когда объекты «равны»

```java
Money a = Money.of(289, 90);
Money b = Money.of(289, 90);
System.out.println(a == b);        // false — разные объекты
System.out.println(a.equals(b));   // false! — equals из Object тоже сравнивает адреса
```

Для `Money` логично считать равными две одинаковые суммы. Переопределяем:

```java
@Override
public boolean equals(Object o) {
    if (this == o) return true;                    // 1
    if (!(o instanceof Money other)) return false; // 2
    return kopecks == other.kopecks;               // 3
}
```

1. Тот же самый объект — точно равен (быстрая проверка).
2. `instanceof` — проверка типа: «является ли `o` объектом класса `Money`?». Если `o` — строка, `null` или что-то ещё, объекты не равны. Конструкция `o instanceof Money other` (Java 16+) одновременно проверяет тип и создаёт переменную `other` уже типа `Money`.
3. Сравниваем значимые поля.

Параметр — `Object`, а не `Money`: так требует сигнатура метода в `Object`. Если написать `equals(Money o)`, получится **перегрузка**, а не переопределение, и коллекции и AssertJ её не увидят (`@Override` здесь бы спас — компилятор выдал бы ошибку).

**Зачем в тестах:** `assertThat(actual).isEqualTo(expected)` вызывает `equals`. Без переопределения два одинаковых объекта «не равны», и тест падает, хотя всё правильно.

## hashCode — парный к equals

**Правило:** если `a.equals(b)`, то `a.hashCode() == b.hashCode()`. Нарушите — `HashSet` и `HashMap` (модуль 5) будут работать неправильно: «равные» объекты окажутся в разных «корзинах», и набор будет содержать дубликаты.

```java
@Override
public int hashCode() {
    return Long.hashCode(kopecks);
}
```

Для нескольких полей: `return Objects.hash(name, price, category);` (`java.util.Objects`).

> ⚠️ Переопределяете `equals` — **всегда** переопределяйте и `hashCode`. Это частый вопрос собеседования: «что будет, если переопределить только equals?» Ответ: объекты, равные по equals, будут считаться разными в HashSet/HashMap.

## Генерация в IDEA

Писать руками нужно для понимания, а в работе: **Alt+Insert** (⌘N) → **equals() and hashCode()** → выбрать поля. Аналогично для **toString()**.

> 💡 В модуле 6 библиотека **Lombok** сгенерирует всё это одной аннотацией `@Data`, а в уроке 4.8 вы увидите **record** — классы, у которых `equals`, `hashCode` и `toString` есть сразу.

## Деньги в копейках

Обратите внимание на устройство `Money`: сумма хранится в `long` **копейках**, а не в `double` рублях. Так не бывает ошибок вида `0.1 + 0.2 = 0.30000000000000004`. Это реальная практика: многие платёжные API принимают суммы в минимальных единицах (копейках, центах).

> 🧪 **Попробуй сам.** Создайте в `main` два одинаковых `Money`, положите их в `java.util.HashSet<Money>` и напечатайте размер множества. До выполнения задания будет 2, после — 1.

```task
id: m04-t04
title: Money — equals, hashCode, toString
module: exercises/m04-java-oop
test: MoneyCheck
file: exercises/m04-java-oop/src/main/java/ru/qacourse/m04/Money.java
solution: solutions/exercises/m04-java-oop/src/main/java/ru/qacourse/m04/Money.java
text: |
  Переопределите в `Money` три метода:
  - `equals` — равны, если равны копейки;
  - `hashCode` — согласованно с `equals`;
  - `toString` — `"289.90 ₽"`, 5 копеек → `"0.05 ₽"`.
hint: |
  Код всех трёх методов разобран в уроке. Не забудьте `@Override`. Для форматирования — `String.format("%d.%02d ₽", kopecks / 100, kopecks % 100)`.
```

```quiz
- q: Какой метод вызывает AssertJ в isEqualTo?
  options: [toString, equals, hashCode]
  answer: 1
- q: Что будет, если переопределить equals, но не hashCode?
  options:
    - Равные по equals объекты будут считаться разными в HashSet/HashMap
    - Ошибка компиляции
    - Ничего страшного
  answer: 0
- q: Какой тип у параметра equals?
  options: [Тип текущего класса, Object, String]
  answer: 1
- q: Зачем аннотация @Override?
  options:
    - Компилятор проверит, что метод действительно переопределяет метод предка
    - Делает метод быстрее
    - Обязательна для компиляции
  answer: 0
- q: Чем полезен хороший toString в автотестах?
  options:
    - Сообщения об упавших проверках становятся понятными
    - Тесты работают быстрее
    - Без него нельзя создать объект
  answer: 0
```
