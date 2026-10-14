---
title: "6.3 Запуск тестов из терминала, параметры, профили"
minutes: 35
---

На сервере CI нет IDE — тесты запускаются только командами. Поэтому команды запуска нужно знать уверенно.

## Выбор тестов: -Dtest

`-D` передаёт Maven **системное свойство** `имя=значение`. Плагин surefire понимает свойство `test`:

```bash
./mvnw test -Dtest=ConfigCheck                     # один класс
./mvnw test -Dtest=ConfigCheck#systemWins          # один метод
./mvnw test -Dtest='ConfigCheck#own*'              # методы по маске
./mvnw test -Dtest='*Check'                        # классы по маске
./mvnw test -Dtest='ConfigCheck,JsonTasksCheck'    # несколько через запятую
```

> ⚠️ **Кавычки.** Символы `*` и `#` могут интерпретироваться оболочкой (zsh, PowerShell). Берите значение в одинарные кавычки — в bash/zsh — или двойные в PowerShell: `.\mvnw.cmd test "-Dtest=*Check"`. В PowerShell весь аргумент с `-D` вообще лучше брать в кавычки, иначе PowerShell может разрезать его на части по точке.

Если указанного теста нет, surefire падает с «No tests matching pattern» — у нас в курсе это выключено настройкой `failIfNoSpecifiedTests=false`.

## Свои параметры в тестах

Через `-D` можно передать что угодно, а тест прочитает это через `System.getProperty`:

```bash
./mvnw test -Dbase.url=http://test-stand:8081 -Dbrowser=firefox
```

```java
String baseUrl = System.getProperty("base.url", "http://localhost:8081");
```

Второй аргумент — значение по умолчанию, если свойство не передали. Так один и тот же набор тестов запускают на разных стендах и в разных браузерах. В уроке 6.7 сделаем удобный класс конфигурации.

> 💡 Surefire запускает тесты в **отдельном процессе Java** (fork). Свойства из командной строки Maven он передаёт в этот процесс автоматически. Если вдруг свойство не доходит — его указывают в `<systemPropertyVariables>` конфигурации surefire (так сделано в `testlab/pom.xml` для `selenide.headless`).

## Переменные окружения

Альтернатива `-D` — переменные окружения ОС. Их удобно задавать в CI:

```tabs
Windows: |
  ~~~powershell
  $env:SHOP_URL = "http://test-stand:8081"
  .\mvnw.cmd test
  ~~~
macOS / Linux: |
  ~~~bash
  SHOP_URL=http://test-stand:8081 ./mvnw test
  ~~~

  Запись `ПЕРЕМЕННАЯ=значение команда` задаёт переменную только для этой одной команды.
```

```java
String url = System.getenv("SHOP_URL");   // null, если не задана
```

Загляните в `exercises/test-support/.../ShopConfig.java` — именно так курс находит QA Burger: сначала свойство `shop.url`, потом переменная `SHOP_URL`, потом значение по умолчанию.

## Профили

**Профиль** — именованный набор настроек, который включается по требованию. Например, «smoke» — только быстрые тесты, «stage» — адрес тестового стенда:

```xml
<profiles>
    <profile>
        <id>smoke</id>
        <properties>
            <groups>smoke</groups>                   <!-- surefire: только тесты с @Tag("smoke") -->
        </properties>
    </profile>
    <profile>
        <id>stage</id>
        <properties>
            <base.url>http://stage.qaburger.local</base.url>
        </properties>
    </profile>
</profiles>
```

Включение: `./mvnw test -Psmoke,stage` (`-P` — профили через запятую).

## Отчёты surefire

После прогона в `target/surefire-reports/`:
- `*.txt` — краткий итог по классу;
- `TEST-*.xml` — подробный отчёт в формате **JUnit XML**. Этот формат понимают все CI-системы (GitLab, Jenkins, GitHub Actions) и сами строят по нему таблицу результатов. Портал курса, кстати, читает именно эти XML-файлы, когда вы нажимаете «Проверить».

Красивый HTML-отчёт делает **Allure** — модуль 14.

## Перезапуск упавших тестов

Иногда тест падает «случайно»: сеть моргнула, стенд притормозил. У surefire есть встроенный перезапуск:

```bash
./mvnw test -Dsurefire.rerunFailingTestsCount=2
```

Упавший тест будет запущен ещё до 2 раз. Если прошёл со второй попытки — он помечается как **flaky** (нестабильный). Подробнее о перезапусках и о том, почему ими нельзя злоупотреблять, — в уроке 7.7.

## Код возврата — как CI понимает результат

Каждая программа, завершаясь, сообщает ОС **код возврата** (exit code): `0` — успех, не ноль — ошибка. Maven возвращает `1`, если хоть один тест упал. CI смотрит именно на него, чтобы покрасить сборку в красный.

```tabs
Windows: |
  ~~~powershell
  .\mvnw.cmd test
  echo $LASTEXITCODE
  ~~~
macOS / Linux: |
  ~~~bash
  ./mvnw test
  echo $?
  ~~~

  `$?` — код возврата последней команды.
```

```quiz
- q: Как запустить только метод own в классе ConfigCheck?
  options: ["-Dtest=ConfigCheck#own", "-Dtest=own", "-Dmethod=own"]
  answer: 0
- q: Как прочитать в тесте параметр, переданный через -Dbrowser=firefox?
  options: ['System.getProperty("browser")', 'System.getenv("browser")', "args[0]"]
  answer: 0
- q: Как включить профиль smoke?
  options: ["-Psmoke", "-Dsmoke", "--profile smoke"]
  answer: 0
- q: Какой формат отчётов понимают все CI-системы?
  options: [JUnit XML (TEST-*.xml), PDF, Excel]
  answer: 0
- q: Какой код возврата у Maven, если тест упал?
  options: ["0", "Не ноль (обычно 1)", "Код возврата не используется"]
  answer: 1
```
