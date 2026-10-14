---
title: "5.8 Файлы и даты"
minutes: 40
---

## Файлы: java.nio.file

Автотесты часто работают с файлами: читают тестовые данные, сохраняют скачанный файл, проверяют лог, загружают файл через API. Современный способ — пакет `java.nio.file`.

### Path — путь

```java
Path file = Path.of("data", "menu.txt");            // относительный: data/menu.txt (или data\menu.txt в Windows)
Path abs = file.toAbsolutePath();                    // полный путь
Path dir = file.getParent();                         // data
String name = file.getFileName().toString();         // "menu.txt"
Path report = Path.of("target").resolve("report.txt"); // target/report.txt
```

- `Path.of(...)` склеивает части **правильным для текущей ОС разделителем**. Поэтому код одинаково работает в Windows, macOS и Linux;
- `resolve(...)` — «дописать» к пути.

> ⚠️ Никогда не собирайте пути строками: `"data" + "/" + "menu.txt"` или `"C:\\Users\\..."`. Такой код сломается на другой ОС или у другого человека. Абсолютные пути в тестах — тоже зло: у коллеги нет вашей папки `C:\Users\Anna`.

**Относительно чего** считается относительный путь? Относительно **рабочей папки** процесса (`System.getProperty("user.dir")`). При запуске через Maven это папка модуля, в IDEA — обычно тоже. Для надёжности тестовые файлы кладут в **ресурсы** (см. ниже).

### Чтение и запись — класс Files

```java
List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);   // все строки списком
String text = Files.readString(path);                                    // весь файл одной строкой (UTF-8)
Files.writeString(path, "Привет");                                      // записать строку
Files.write(path, List.of("a", "b"), StandardCharsets.UTF_8);           // записать строки
boolean exists = Files.exists(path);
Files.createDirectories(path.getParent());                               // создать папки (все недостающие)
Files.deleteIfExists(path);
long size = Files.size(path);
```

Все эти методы бросают проверяемое `IOException` — значит, нужно `throws IOException` или `try/catch`.

**Кодировка:** всегда указывайте или используйте методы, где по умолчанию UTF-8 (`readString`, `writeString`). Иначе в Windows русский текст может превратиться в «кракозябры».

### Большие файлы — построчно, через стрим

```java
try (Stream<String> lines = Files.lines(log, StandardCharsets.UTF_8)) {
    long errors = lines.filter(l -> l.contains("ERROR")).count();
}
```

`Files.lines` читает файл лениво, не загружая целиком в память. Стрим держит файл открытым, поэтому — **try-with-resources** (урок 5.1).

### Ресурсы — файлы рядом с тестами

Файлы для тестов кладут в `src/test/resources`. Maven копирует их в `target/test-classes`, и их можно найти независимо от рабочей папки:

```java
Path log = Path.of(getClass().getResource("/shop-sample.log").toURI());
```

- `getClass().getResource("/имя")` — найти ресурс по пути от корня `resources` (начальный `/` обязателен);
- `.toURI()` → `Path.of(...)` — превратить в Path.

Загляните в `exercises/m05-java-advanced/src/test/resources/shop-sample.log` — это настоящий кусок лога QA Burger.

### Временные файлы в тестах

JUnit 5 создаёт временную папку и удаляет её после теста:

```java
@TempDir
Path tmp;

@Test
void savesFile() throws IOException {
    Path file = tmp.resolve("menu.txt");
    ...
}
```

## Даты и время: java.time

Старые классы `Date` и `Calendar` неудобны и полны ловушек — в новом коде используйте пакет **`java.time`** (Java 8+).

| Класс | Что хранит | Пример |
|---|---|---|
| `LocalDate` | Дата без времени | 2024-05-12 |
| `LocalTime` | Время без даты | 12:30 |
| `LocalDateTime` | Дата и время без часового пояса | 2024-05-12T12:30 |
| `ZonedDateTime` | С часовым поясом | 2024-05-12T12:30+03:00[Europe/Moscow] |
| `Instant` | Момент времени (UTC) | 2024-05-12T09:30:00Z |
| `Duration` | Промежуток в секундах/наносекундах | PT2S (2 секунды) |
| `Period` | Промежуток в днях/месяцах/годах | P1M (1 месяц) |

```java
LocalDate today = LocalDate.now();
LocalDate until = LocalDate.of(2020, 8, 31);       // год, месяц (1-12!), день
LocalDate nextWeek = today.plusDays(7);
boolean expired = today.isAfter(until);
long days = ChronoUnit.DAYS.between(today, until); // разница в днях
DayOfWeek dow = today.getDayOfWeek();               // MONDAY...
```

Объекты `java.time` **неизменяемы**: `today.plusDays(7)` возвращает новую дату, `today` не меняется (как строки).

### Форматирование и разбор

```java
DateTimeFormatter f = DateTimeFormatter.ofPattern("dd.MM.yyyy");
String text = LocalDate.of(2020, 8, 31).format(f);   // "31.08.2020"
LocalDate parsed = LocalDate.parse("01.02.2024", f);
LocalDate iso = LocalDate.parse("2024-02-01");       // ISO-формат — без форматтера
```

Буквы шаблона: `dd` — день, `MM` — месяц (**заглавные!** `mm` — минуты), `yyyy` — год, `HH` — часы, `mm` — минуты, `ss` — секунды.

### Даты в тестах — источник багов

- **Граница «включительно».** Промокод действует до 31.08 — 31.08 он ещё работает? Требование должно это уточнять; тест проверяет ровно границу. В задании `isPromoValid` — включительно: `!today.isAfter(validUntil)`.
- **Часовые пояса.** Сервер в UTC, пользователь в Москве (+3): заказ в 01:00 по Москве — это ещё «вчера» по UTC. В API QA Burger время приходит в UTC (`2024-05-12T09:30:00Z`, `Z` = UTC).
- **Високосный год**, 29 февраля, переход на летнее время (в некоторых странах), конец месяца (31 января + 1 месяц = ?).
- **«Сейчас» в тестах.** Тест, который зависит от `LocalDate.now()`, может падать в определённые дни. Поэтому методы принимают дату параметром (`today`), а не вызывают `now()` внутри — так их легко тестировать. Обратите внимание на сигнатуры в задании.

```task
id: m05-t08
title: Файлы и даты
module: exercises/m05-java-advanced
test: Task08FilesDatesCheck
file: exercises/m05-java-advanced/src/main/java/ru/qacourse/m05/Task08FilesDates.java
solution: solutions/exercises/m05-java-advanced/src/main/java/ru/qacourse/m05/Task08FilesDates.java
text: |
  - `writeLines(file, lines)` — создать папки и записать UTF-8;
  - `countErrors(logFile)` — `Files.lines` + `filter` + `count` в try-with-resources;
  - `isPromoValid(validUntil, today)` — включительно;
  - `daysBetween(from, to)` — `ChronoUnit.DAYS.between`;
  - `format` / `parse` — шаблон `dd.MM.yyyy`.
hint: |
  `Files.createDirectories(file.toAbsolutePath().getParent());` — `toAbsolutePath()` нужен, чтобы у относительного пути из одного имени был родитель.
```

```quiz
- q: Почему пути собирают через Path.of(...), а не строками с "/"?
  options:
    - Path.of использует разделитель текущей ОС — код работает везде
    - Так быстрее
    - Строки нельзя использовать как пути
  answer: 0
- q: Где хранить файлы с тестовыми данными?
  options: [src/test/resources, На рабочем столе, В C:\\data]
  answer: 0
- q: Что означает MM в шаблоне даты?
  options: [Месяц, Минуты, Миллисекунды]
  answer: 0
- q: "`LocalDate d = LocalDate.of(2024,1,1); d.plusDays(1);` — чему равна d?"
  options: ["2024-01-01", "2024-01-02"]
  answer: 0
  explain: Объекты java.time неизменяемы.
- q: Почему метод лучше принимать дату параметром, а не вызывать LocalDate.now() внутри?
  options:
    - Его можно протестировать на любой дате, тест не зависит от сегодняшнего дня
    - Так короче
    - now() не работает в тестах
  answer: 0
```
