---
title: "7.7 Перезапуск упавших тестов"
minutes: 40
---

## Flaky-тесты и зачем перезапуск

**Flaky** (нестабильный, «мигающий») тест иногда проходит, иногда падает — без изменений в коде. Причины: медленный стенд, сетевой сбой, гонка в асинхронной системе, тесты мешают друг другу, плохие ожидания в UI.

Перезапуск упавших тестов — **временная страховка**, а не лечение:
- ✅ ночной прогон 2000 тестов не краснеет из-за одного сетевого сбоя;
- ❌ если бездумно перезапускать всё, вы начнёте пропускать настоящие баги, которые воспроизводятся «иногда» (а гонки — это тоже баги!).

Правило: перезапуск включаем, но **каждый flaky-тест — задача на исправление**, и отчёт должен показывать, что тест прошёл не с первого раза.

## Способ 1: встроенный перезапуск Maven Surefire

```bash
./mvnw test -Dsurefire.rerunFailingTestsCount=2
```

Упавший тест перезапускается до 2 раз сразу же. В итоге surefire пишет:

```text
[WARNING] Flakes:
[WARNING] CartApiTest.addToCart
[WARNING]   Run 1: expected 200 but was 503
[INFO]   Run 2: PASS
[WARNING] Tests run: 120, Failures: 0, Errors: 0, Skipped: 0, Flakes: 1
```

Тест прошёл, но помечен как **Flake** — видно, что с ним проблемы. Самый простой и популярный вариант для Maven + JUnit 5.

Gradle — плагин `org.gradle.test-retry`:

```groovy
plugins {
    id 'org.gradle.test-retry' version '1.6.2'
}
test {
    retry {
        maxRetries = 2
        failOnPassedAfterRetry = false
    }
}
```

## Способ 2: сохранить упавшие тесты в файл и перезапустить отдельно

Иногда удобнее прогнать всё, а потом **отдельным шагом** перезапустить только упавшие — например, в CI: первый шаг «все тесты», второй шаг «перезапуск упавших», и в отчёте видно оба этапа.

### Шаг 1. Расширение, которое собирает упавшие тесты

Используем интерфейс `TestWatcher` — JUnit вызывает его методы по итогам каждого теста: `testSuccessful`, `testFailed`, `testAborted`, `testDisabled`.

```java
public class FailedTestsCollector implements TestWatcher {

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        String line = context.getRequiredTestClass().getSimpleName()
                + "#" + context.getRequiredTestMethod().getName();
        Files.writeString(Path.of("target/failed-tests.txt"), line + System.lineSeparator(),
                StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }
}
```

- `context.getRequiredTestClass().getSimpleName()` — имя класса без пакета (`CartTest`);
- `getRequiredTestMethod().getName()` — имя метода;
- формат `Класс#метод` — ровно тот, что понимает `-Dtest` у Maven;
- `StandardOpenOption.CREATE, APPEND` — создать файл, если его нет, и дописывать в конец (каждый упавший тест — новая строка);
- `System.lineSeparator()` — перевод строки, правильный для ОС.

(`Files.writeString` бросает `IOException` — в задании это обёрнуто в `try/catch`.)

Подключаем ко всем тестам — `@ExtendWith(FailedTestsCollector.class)` на базовом классе или глобально через autodetection.

### Шаг 2. Перезапуск из файла

Превращаем строки файла в аргумент `-Dtest=A#x,B#y`:

```tabs
macOS / Linux (bash): |
  ~~~bash
  #!/usr/bin/env bash
  # rerun-failed.sh — перезапустить тесты из target/failed-tests.txt
  FILE=target/failed-tests.txt
  if [ ! -s "$FILE" ]; then
    echo "Упавших тестов нет"
    exit 0
  fi
  TESTS=$(sort -u "$FILE" | paste -sd, -)
  cp "$FILE" target/failed-tests-first-run.txt
  rm "$FILE"
  ./mvnw test -Dtest="$TESTS"
  ~~~

  - `[ ! -s "$FILE" ]` — «файл не существует или пустой» (`-s` — существует и не пуст, `!` — отрицание);
  - `sort -u` — отсортировать строки и убрать повторы (`-u` — unique);
  - `paste -sd, -` — склеить все строки в одну через запятую (`-s` — последовательно, `-d,` — разделитель `,`, `-` — читать из входа);
  - `$( ... )` — подставить результат команды в переменную `TESTS`;
  - перед перезапуском сохраняем копию и удаляем файл, чтобы он заполнился заново только тестами, упавшими повторно.
Windows (PowerShell): |
  ~~~powershell
  # rerun-failed.ps1 — перезапустить тесты из target\failed-tests.txt
  $file = "target\failed-tests.txt"
  if (-not (Test-Path $file) -or (Get-Item $file).Length -eq 0) {
      Write-Host "Упавших тестов нет"
      exit 0
  }
  $tests = (Get-Content $file | Sort-Object -Unique) -join ","
  Copy-Item $file "target\failed-tests-first-run.txt"
  Remove-Item $file
  .\mvnw.cmd test "-Dtest=$tests"
  ~~~

  - `Test-Path` — существует ли файл; `-not` — отрицание;
  - `Get-Content` — строки файла; `Sort-Object -Unique` — без повторов;
  - `-join ","` — склеить через запятую;
  - `"-Dtest=$tests"` в кавычках — чтобы PowerShell не разрезал аргумент.
```

## TestNG: IRetryAnalyzer

В TestNG (урок 7.8) перезапуск встроен в сам фреймворк:

```java
public class RetryAnalyzer implements IRetryAnalyzer {
    private int attempt = 0;
    private static final int MAX = 2;

    @Override
    public boolean retry(ITestResult result) {
        return attempt++ < MAX;      // true — перезапустить ещё раз
    }
}

@Test(retryAnalyzer = RetryAnalyzer.class)
public void flakyTest() { ... }
```

TestNG ещё и сам записывает упавшие тесты в `test-output/testng-failed.xml`: перезапуск — `./mvnw test -Dsurefire.suiteXmlFiles=test-output/testng-failed.xml`.

## Как лечить flaky-тесты (а не только перезапускать)

| Причина | Лечение |
|---|---|
| `Thread.sleep` и фиксированные паузы | Ожидание условия: Awaitility (модуль 13), умные ожидания Selenide (модуль 10) |
| Тесты делят данные | Каждый тест создаёт свои данные, уникальные имена |
| Зависимость от порядка | Независимые тесты, подготовка в `@BeforeEach` |
| Нестабильный стенд/внешний сервис | Заглушки (WireMock, модуль 12), health-check перед прогоном |
| Хрупкие локаторы | `data-testid` (модуль 10) |
| Время и даты | Не использовать «сейчас» в проверках, учитывать часовые пояса |

```task
id: m07-t07
title: Сбор упавших тестов в файл
module: exercises/m07-junit
test: FailedTestsCollectorCheck
file: exercises/m07-junit/src/main/java/ru/qacourse/m07/ext/FailedTestsCollector.java
solution: solutions/exercises/m07-junit/src/main/java/ru/qacourse/m07/ext/FailedTestsCollector.java
text: |
  Реализуйте `testFailed` в `FailedTestsCollector`: дописывайте в файл `FailedTestsCollector.file()` строку `ИмяКласса#имяМетода`.
  Не забудьте создать папки и дописывать (APPEND), а не перезаписывать файл.

  Проверка запустит образец `checks/CollectorSample.java` (1 тест проходит, 2 падают).
hint: |
  Код почти целиком есть в уроке. `Files.createDirectories(f.toAbsolutePath().getParent())`,
  `Files.writeString(f, line + System.lineSeparator(), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND)`,
  а `IOException` оберните в `UncheckedIOException`.
```

```quiz
- q: Что такое flaky-тест?
  options:
    - Тест, который то проходит, то падает без изменений в коде
    - Тест, который всегда падает
    - Медленный тест
  answer: 0
- q: Как включить перезапуск упавших тестов в Maven Surefire?
  options: ["-Dsurefire.rerunFailingTestsCount=2", "-Dretry=2", "-Prerun"]
  answer: 0
- q: Какой интерфейс JUnit сообщает о результате теста (прошёл/упал)?
  options: [TestWatcher, ParameterResolver, BeforeEachCallback]
  answer: 0
- q: Почему перезапуск — не лечение?
  options:
    - Он маскирует проблему, а «иногда воспроизводимые» баги тоже баги
    - Он не работает
    - Он замедляет компиляцию
  answer: 0
- q: "Что делает `paste -sd, -`?"
  options: [Склеивает строки в одну через запятую, Удаляет запятые, Сортирует строки]
  answer: 0
```
