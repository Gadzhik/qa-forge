---
title: "4.8 enum и record"
minutes: 35
---

## enum — перечисление

Статус заказа может быть только одним из шести: NEW, PAYMENT_FAILED, PAID, COOKING, READY, CANCELLED. Хранить его строкой опасно: опечатка `"PAYED"` скомпилируется и тихо сломает логику. Для фиксированного набора значений есть **enum**:

```java
public enum OrderStatus {
    NEW, PAYMENT_FAILED, PAID, COOKING, READY, CANCELLED
}
```

```java
OrderStatus status = OrderStatus.PAID;
if (status == OrderStatus.PAID) { ... }   // enum можно сравнивать через ==
OrderStatus typo = OrderStatus.PAYED;     // ❌ ошибка компиляции — опечатка поймана сразу
```

- Значения enum — константы, пишутся ЗАГЛАВНЫМИ;
- каждое значение существует в единственном экземпляре, поэтому `==` безопасно;
- enum отлично работает в `switch`.

Полезные методы:

```java
OrderStatus.values()              // массив всех значений по порядку
OrderStatus.valueOf("PAID")       // из строки → OrderStatus.PAID (неизвестная строка → IllegalArgumentException)
OrderStatus.PAID.name()           // "PAID"
OrderStatus.PAID.ordinal()        // 2 — номер по порядку (с 0)
```

`valueOf` пригодится в API-тестах: сервер прислал `"status": "PAID"` — превращаем в enum.

### enum с полями и методами

enum — это полноценный класс: у него могут быть поля, конструктор и методы.

```java
public enum OrderStatus {
    NEW("Новый"),
    PAID("Оплачен"),
    READY("Готов");

    private final String title;

    OrderStatus(String title) {        // конструктор enum всегда private
        this.title = title;
    }

    public String title() {
        return title;
    }

    public boolean isFinal() {
        return this == READY;
    }
}
```

`NEW("Новый")` — создание значения с вызовом конструктора. Список значений должен идти первым и заканчиваться `;`.

Метод `canTransitionTo` в задании — это диаграмма состояний из урока 1.6, записанная кодом. `switch (this)` внутри enum перебирает текущее значение.

## record — класс для данных

Часто класс нужен просто чтобы **хранить данные**: товар из ответа API, тестовый пользователь. Писать поля, конструктор, геттеры, `equals`, `hashCode`, `toString` — 50 строк шаблонного кода. С Java 16 есть **record**:

```java
public record ProductDto(long id, String name, double price, String category) { }
```

Одна строка, и Java сама создаёт:
- `private final` поля `id`, `name`, `price`, `category`;
- конструктор со всеми полями;
- методы доступа **`id()`, `name()`, `price()`** — без префикса `get`!
- `equals`, `hashCode` по всем полям;
- `toString`: `ProductDto[id=12, name=Кола, price=99.0, category=DRINK]`.

```java
ProductDto cola = new ProductDto(12, "Кола", 99.0, "DRINK");
System.out.println(cola.name());     // Кола
System.out.println(cola);            // ProductDto[id=12, name=Кола, ...]
cola.equals(new ProductDto(12, "Кола", 99.0, "DRINK"));   // true
```

**record неизменяем**: сеттеров нет, поля `final`. Чтобы «изменить» — создают копию:

```java
public ProductDto withPrice(double newPrice) {
    return new ProductDto(id, name, newPrice, category);
}
```

Внутри record можно добавлять свои методы и проверки в **компактном конструкторе**:

```java
public record ProductDto(long id, String name, double price, String category) {
    public ProductDto {
        if (price < 0) throw new IllegalArgumentException("Цена не может быть отрицательной");
    }
}
```

> 💡 В модуле 9 мы будем описывать ответы API QA Burger как record'ы, а Jackson будет сам превращать JSON в эти объекты. Тренажёр [**POJO Builder**](#/game/pojo-builder) — как раз про это.

```task
id: m04-t08
title: Статусы заказа и ProductDto
module: exercises/m04-java-oop
test: EnumRecordCheck
file: exercises/m04-java-oop/src/main/java/ru/qacourse/m04/OrderStatus.java
solution: solutions/exercises/m04-java-oop/src/main/java/ru/qacourse/m04/OrderStatus.java
text: |
  - `OrderStatus.canTransitionTo(next)` — разрешённые переходы описаны в комментарии к методу;
  - `OrderStatus.isFinal()` — только READY и CANCELLED;
  - `ProductDto.withPrice(newPrice)` (файл рядом) — копия с новой ценой;
  - `ProductDto.isDrink()` — категория `"DRINK"` (осторожно с `null`).
hint: |
  ~~~java
  return switch (this) {
      case NEW -> next == PAID || next == PAYMENT_FAILED || next == CANCELLED;
      case PAYMENT_FAILED -> ...;
      ...
  };
  ~~~
  Для `isDrink` — `"DRINK".equals(category)`.
```

```quiz
- q: Почему статус лучше хранить в enum, а не в String?
  options:
    - Опечатки ловит компилятор, набор значений фиксирован
    - Так быстрее работает сеть
    - String нельзя сравнивать
  answer: 0
- q: Как называется метод доступа к полю name у record?
  options: [getName(), name(), getname()]
  answer: 1
- q: Что вернёт OrderStatus.valueOf("PAYED"), если такого значения нет?
  options: [null, Бросит IllegalArgumentException, OrderStatus.PAID]
  answer: 1
- q: Можно ли изменить поле record после создания?
  options: ["Да, через сеттер", "Нет, record неизменяем"]
  answer: 1
- q: Что record генерирует автоматически? (несколько)
  options: [Конструктор, equals и hashCode, toString, Сеттеры]
  answer: [0, 1, 2]
```
