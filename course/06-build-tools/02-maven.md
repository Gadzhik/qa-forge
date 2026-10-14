---
title: "6.2 Maven: pom.xml и жизненный цикл"
minutes: 45
---

## pom.xml — построчно

**POM** — Project Object Model, «модель проекта». Откройте `exercises/m06-build-tools/pom.xml` и читайте вместе с уроком.

```xml
<?xml version="1.0" encoding="UTF-8"?>
```

Обычная первая строка XML-файла: версия формата и кодировка.

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" ...>
    <modelVersion>4.0.0</modelVersion>
```

Корневой элемент. Атрибуты `xmlns...` описывают схему (IDEA по ним подсказывает теги). `modelVersion` всегда `4.0.0`.

### Родитель

```xml
<parent>
    <groupId>ru.qacourse</groupId>
    <artifactId>exercises-parent</artifactId>
    <version>1.0.0</version>
</parent>
```

Модуль **наследует** настройки от родительского pom (`exercises/pom.xml`): версии библиотек, настройки плагинов, `groupId` и `version`. Так в десятке модулей не повторяют одно и то же. Родителя Maven ищет в папке выше (`../pom.xml`), а если там нет — в `~/.m2`.

Популярный родитель в мире Java — `spring-boot-starter-parent` (посмотрите `shop-app/pom.xml`).

### Координаты проекта

```xml
<artifactId>m06-build-tools</artifactId>
<name>m06 — Сборка, Lombok, Jackson, конфиги</name>
```

`groupId` и `version` унаследованы. `name` — человекочитаемое название (необязательно).

### Свойства

```xml
<properties>
    <maven.compiler.release>21</maven.compiler.release>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <junit.version>5.13.4</junit.version>
</properties>
```

(В родителе.) Переменные: `${junit.version}` подставится везде, где упомянуто.
- `maven.compiler.release` — версия Java, под которую компилировать;
- `project.build.sourceEncoding` — кодировка исходников. **Обязательно UTF-8**, иначе на Windows русские строки в тестах сломаются.

### Зависимости

```xml
<dependencies>
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
    </dependency>
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

Каждая `<dependency>` — одна библиотека. `version` здесь не указана, потому что она задана в родителе в `<dependencyManagement>` — центральном списке версий.

**`scope`** — где библиотека нужна:

| scope | Компиляция основного кода | Тесты | Попадёт в итоговую программу |
|---|---|---|---|
| `compile` (по умолчанию) | ✅ | ✅ | ✅ |
| `test` | ❌ | ✅ | ❌ |
| `provided` | ✅ | ✅ | ❌ (её «предоставит» среда) |
| `runtime` | ❌ | ✅ | ✅ |

В проекте автотестов почти всё обычно в `test`: JUnit, AssertJ, REST Assured. Lombok — `provided`: он нужен только при компиляции.

### dependencyManagement — центральный список версий

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.assertj</groupId>
            <artifactId>assertj-core</artifactId>
            <version>${assertj.version}</version>
        </dependency>
    </dependencies>
</dependencyManagement>
```

(В `exercises/pom.xml`.) Здесь **не подключают** библиотеки, а только **фиксируют версии**. Модули, которые подключат `assertj-core` без версии, получат эту.

### Плагины

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <configuration>
                <annotationProcessorPaths>...</annotationProcessorPaths>
            </configuration>
        </plugin>
    </plugins>
</build>
```

Всю работу в Maven делают **плагины**: компилирует `maven-compiler-plugin`, запускает тесты `maven-surefire-plugin`, упаковывает `maven-jar-plugin`. В `<configuration>` их настраивают. Здесь мы говорим компилятору подключить Lombok (урок 6.5).

## Стандартная структура

```text
m06-build-tools/
├── pom.xml
├── src/main/java/        ← код
├── src/main/resources/   ← файлы для кода (config.properties)
├── src/test/java/        ← тесты
├── src/test/resources/   ← файлы для тестов
└── target/               ← результат сборки (создаётся сам, в git не кладём)
```

Maven знает эти пути без настройки — принцип **«соглашения важнее конфигурации»**.

## Жизненный цикл

Maven собирает проект **фазами**, которые идут строго по порядку:

```text
validate → compile → test-compile → test → package → verify → install → deploy
```

| Фаза | Что происходит |
|---|---|
| `compile` | компиляция `src/main/java` → `target/classes` |
| `test-compile` | компиляция тестов → `target/test-classes` |
| `test` | запуск тестов (surefire), отчёты в `target/surefire-reports` |
| `package` | упаковка в jar → `target/*.jar` |
| `verify` | проверки после упаковки (сюда вешают интеграционные тесты) |
| `install` | копирование jar в `~/.m2` — чтобы другие проекты могли его подключить |
| `deploy` | загрузка в удалённое хранилище компании |

**Главное правило:** вызов фазы выполняет **все предыдущие**. `./mvnw package` = compile + test-compile + test + package.

Отдельно стоит **`clean`** — удаляет папку `target`.

## Команды

Во всех примерах на Windows пишите `.\mvnw.cmd` вместо `./mvnw`.

```bash
./mvnw clean test
```

Удалить старые результаты и запустить тесты. `clean` полезен, когда «что-то странное» — например, переименовали класс, а старый `.class` остался.

```bash
./mvnw -f exercises/m06-build-tools/pom.xml test
```

`-f` (*file*) — какой pom использовать, то есть какой модуль собирать.

```bash
./mvnw -pl exercises/m06-build-tools test
```

`-pl` (*projects list*) — собрать модуль **внутри общего проекта** (команда запускается из корня). Добавьте `-am` (*also make*), чтобы собрать и модули, от которых он зависит.

```bash
./mvnw package -DskipTests              # собрать jar, тесты скомпилировать, но не запускать
./mvnw package -Dmaven.test.skip=true   # тесты даже не компилировать
./mvnw -q test                          # тихий режим: только ошибки
./mvnw -o test                          # offline: не ходить в интернет
./mvnw -U test                          # принудительно обновить зависимости
./mvnw dependency:tree                  # дерево зависимостей
./mvnw help:effective-pom               # итоговый pom со всем унаследованным
```

`dependency:tree` — это не фаза, а **цель плагина** в формате `плагин:цель`.

## Как читать вывод Maven

```text
[INFO] --- surefire:3.5.6:test (default-test) @ m06-build-tools ---
[INFO] Running ru.qacourse.m06.ConfigCheck
[ERROR] Tests run: 4, Failures: 1, Errors: 0, Skipped: 0, Time elapsed: 0.12 s <<< FAILURE!
[ERROR] ru.qacourse.m06.ConfigCheck.systemWins -- Time elapsed: 0.01 s <<< FAILURE!
org.opentest4j.AssertionFailedError:
expected: "http://test-stand:8081"
 but was: "http://localhost:8081"
...
[INFO] BUILD FAILURE
```

- `--- surefire:...:test` — какой плагин работает;
- `Failures` — проверка не прошла (assert), `Errors` — тест упал с исключением;
- `expected / but was` — ожидание и факт;
- в конце `BUILD SUCCESS` или `BUILD FAILURE`.

> 🧪 **Попробуй сам.** Выполните в корне курса: `./mvnw -f exercises/m06-build-tools/pom.xml dependency:tree`. Найдите в дереве, кто притащил `jackson-core`.

```quiz
- q: Что выполнится при ./mvnw package?
  options:
    - Все фазы до package включительно, в том числе test
    - Только упаковка в jar
    - Только компиляция
  answer: 0
- q: Какой scope у JUnit в проекте автотестов?
  options: [compile, test, runtime]
  answer: 1
- q: Чем отличаются -DskipTests и -Dmaven.test.skip=true?
  options:
    - Первый компилирует тесты, но не запускает; второй не компилирует вовсе
    - Ничем
    - Второй запускает тесты дважды
  answer: 0
- q: Что делает раздел dependencyManagement?
  options:
    - Фиксирует версии библиотек, но не подключает их
    - Подключает все библиотеки
    - Удаляет старые версии
  answer: 0
- q: Куда Maven кладёт отчёты о тестах?
  options: [target/surefire-reports, src/test/reports, "~/.m2"]
  answer: 0
```
