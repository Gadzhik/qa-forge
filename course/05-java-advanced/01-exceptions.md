---
title: "5.1 Исключения"
minutes: 45
---

## Что такое исключение

Программа работает, и вдруг что-то идёт не так: строку «два» нельзя превратить в число, файла нет, сервер не ответил, обратились к `null`. В Java в такой момент **бросается исключение** (exception) — объект, описывающий проблему. Если его никто не обработает, программа (или тест) аварийно завершится и напечатает **стектрейс**.

Для автоматизатора исключения — повседневность: каждый упавший тест — это исключение. `assertThat(...)` при несовпадении бросает `AssertionError`, Selenide при ненайденном элементе — `ElementNotFound`, REST Assured при неверном статусе — тоже `AssertionError`. Поэтому важно уметь их **читать**.

## Читаем стектрейс

```text
Exception in thread "main" java.lang.NumberFormatException: For input string: "два"
	at java.base/java.lang.NumberFormatException.forInputString(NumberFormatException.java:67)
	at java.base/java.lang.Integer.parseInt(Integer.java:662)
	at java.base/java.lang.Integer.parseInt(Integer.java:778)
	at ru.qacourse.m05.Task01Exceptions.parseQuantity(Task01Exceptions.java:24)
	at ru.qacourse.m05.Demo.main(Demo.java:7)
```

1. **Первая строка** — тип исключения (`NumberFormatException`) и сообщение (`For input string: "два"`). Часто уже этого достаточно.
2. **Строки `at ...`** — стек вызовов, **сверху — где случилось**, снизу — откуда всё началось. Читайте сверху вниз и ищите **первую строку из вашего кода** (не `java.base/...`): здесь это `Task01Exceptions.java:24`. В IDEA она кликабельна.
3. Если ниже есть **`Caused by: ...`** — это первопричина. В многослойных системах самое полезное часто в **последнем** `Caused by`.

Тренажёр [**Log Detective**](#/game/log-detective) в разделе «Тренажёры» целиком посвящён этому навыку.

## Иерархия исключений

```text
                 Throwable
                /         \
           Error           Exception
   (OutOfMemoryError,     /          \
    StackOverflowError,  RuntimeException   IOException, SQLException...
    AssertionError)      (непроверяемые)    (проверяемые)
                         NullPointerException
                         IllegalArgumentException
                         IllegalStateException
                         IndexOutOfBoundsException
                         NumberFormatException
```

- **Error** — серьёзные проблемы JVM, их не обрабатывают. Любопытно: `AssertionError` (падение проверки в тесте) — тоже `Error`. Поэтому `catch (Exception e)` не «проглотит» упавшую проверку.
- **Checked** (проверяемые) — наследники `Exception`, кроме `RuntimeException`. Компилятор **заставляет** их обработать или объявить в `throws`. Пример: `IOException` при работе с файлами.
- **Unchecked** (непроверяемые) — наследники `RuntimeException`. Обычно это ошибки программиста: `NullPointerException`, `IllegalArgumentException`. Компилятор не заставляет их ловить.

## try / catch / finally

```java
try {
    int q = Integer.parseInt(text);          // может бросить исключение
    System.out.println("Количество: " + q);
} catch (NumberFormatException e) {
    System.out.println("Не число: " + text); // выполнится, только если было исключение этого типа
} finally {
    System.out.println("Проверка завершена"); // выполнится ВСЕГДА
}
```

- **`try`** — блок, где может случиться проблема. Как только бросилось исключение, остаток `try` пропускается.
- **`catch (Тип e)`** — перехват исключения указанного типа (и его наследников). `e` — сам объект исключения: `e.getMessage()` — сообщение.
- **`finally`** — выполняется в любом случае: и при успехе, и при исключении, и даже при `return` внутри `try`. Используют для освобождения ресурсов (закрыть браузер, соединение).

Несколько типов в одном `catch`: `catch (NumberFormatException | NullPointerException e)`.

> ⚠️ **Никогда не «глотайте» исключения молча:**
> ```java
> try { ... } catch (Exception e) { }   // ❌ ошибка исчезла бесследно
> ```
> В тестах особенно опасно: проблема скрыта, тест «зелёный», баг пропущен.

## throw — бросить своё исключение

```java
public static void validate(int quantity) {
    if (quantity < 1 || quantity > 99) {
        throw new IllegalArgumentException("Количество должно быть от 1 до 99, а передано: " + quantity);
    }
}
```

- `throw` — бросить исключение;
- `new IllegalArgumentException("...")` — создать объект исключения с понятным сообщением.

**Хорошее сообщение** говорит, что ожидалось и что пришло. «Ошибка» — плохо. «Количество должно быть от 1 до 99, а передано: 0» — хорошо.

## throws — объявить, что метод может бросить

Проверяемые исключения нужно либо поймать, либо пробросить дальше, указав `throws`:

```java
public static List<String> readMenu(Path file) throws IOException {
    return Files.readAllLines(file);       // может бросить IOException
}
```

В тестах обычно просто пишут `throws Exception` у тестового метода — если что-то пошло не так, тест упадёт, и это правильно.

## Своё исключение

```java
public class InvalidQuantityException extends RuntimeException {
    public InvalidQuantityException(String message) {
        super(message);
    }

    public InvalidQuantityException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

Наследуемся от `RuntimeException` (непроверяемое) и передаём сообщение в конструктор родителя через `super`.

**Цепочка причин (cause):** когда ловите низкоуровневое исключение и бросаете своё, передавайте исходное как причину — иначе потеряется информация о том, что на самом деле случилось:

```java
try {
    value = Integer.parseInt(text.trim());
} catch (NumberFormatException e) {
    throw new InvalidQuantityException("Не число: " + text, e);   // e — причина
}
```

В стектрейсе появится `Caused by: java.lang.NumberFormatException: ...`.

## try-with-resources

Для ресурсов, которые нужно закрывать (файлы, соединения с БД, HTTP-клиенты):

```java
try (BufferedReader reader = Files.newBufferedReader(path)) {
    return reader.readLine();
}   // reader.close() вызовется автоматически, даже при исключении
```

В круглых скобках после `try` объявляется ресурс (класс должен реализовать интерфейс `AutoCloseable`). Мы будем так работать с JDBC в модуле 11.

## Проверка исключений в тестах

Тестировать нужно и «плохие» сценарии: что метод **бросает** правильное исключение. В AssertJ:

```java
assertThatThrownBy(() -> Task01Exceptions.validate(0))
        .isInstanceOf(InvalidQuantityException.class)
        .hasMessage("Количество должно быть от 1 до 99, а передано: 0");
```

`() -> ...` — **лямбда** (урок 5.4): мы передаём не результат вызова, а сам вызов, чтобы AssertJ выполнил его внутри и поймал исключение. Загляните в `Task01ExceptionsCheck` — там все варианты.

```task
id: m05-t01
title: Исключения
module: exercises/m05-java-advanced
test: Task01ExceptionsCheck
file: exercises/m05-java-advanced/src/main/java/ru/qacourse/m05/Task01Exceptions.java
solution: solutions/exercises/m05-java-advanced/src/main/java/ru/qacourse/m05/Task01Exceptions.java
text: |
  - `validate(quantity)` — бросить `InvalidQuantityException` с точным сообщением из комментария;
  - `parseQuantity(text)` — `Integer.parseInt(text.trim())`, при ошибке — `InvalidQuantityException("Не число: " + text, e)`; затем `validate`;
  - `parseOrDefault(text, default)` — вызвать `parseQuantity`, поймать `InvalidQuantityException`, вернуть значение по умолчанию.
hint: |
  Сообщение должно совпадать посимвольно — скопируйте его из комментария.
  В `parseOrDefault` один `try { return parseQuantity(text); } catch (InvalidQuantityException e) { return defaultValue; }`.
```

```quiz
- q: Где в стектрейсе искать место ошибки в вашем коде?
  options:
    - Первая сверху строка at ... из вашего пакета
    - Самая последняя строка
    - В названии исключения
  answer: 0
- q: Что выполнится всегда — и при успехе, и при исключении?
  options: [try, catch, finally]
  answer: 2
- q: Какие исключения компилятор заставляет обработать?
  options: [Проверяемые (checked), Непроверяемые (RuntimeException), Error]
  answer: 0
- q: Почему опасен пустой catch (Exception e) {}?
  options:
    - Ошибка исчезает бесследно, тест может стать зелёным, пропустив баг
    - Он замедляет программу
    - Это ошибка компиляции
  answer: 0
- q: Зачем передавать cause при перебрасывании исключения?
  options:
    - Чтобы в стектрейсе сохранилась первопричина (Caused by)
    - Иначе не скомпилируется
    - Чтобы исключение стало проверяемым
  answer: 0
- q: AssertionError — это...
  options: [Наследник Error, Наследник RuntimeException, Проверяемое исключение]
  answer: 0
```
