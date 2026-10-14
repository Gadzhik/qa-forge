---
title: "6.4 Gradle"
minutes: 45
---

Многие проекты автотестов собираются **Gradle**. Разберём его на демо-проекте `exercises/m06-build-tools/gradle-demo` — откройте его в IDEA отдельным окном (**File → Open** → папка `gradle-demo`), IDEA распознает Gradle-проект.

## Структура проекта

```text
gradle-demo/
├── settings.gradle          ← имя проекта (и список модулей в многомодульных)
├── build.gradle             ← описание сборки (аналог pom.xml)
├── gradlew, gradlew.bat     ← Gradle Wrapper
├── gradle/wrapper/          ← версия Gradle и jar самого wrapper
└── src/main/java, src/test/java   ← та же структура, что в Maven
```

## build.gradle построчно

Файл написан на языке **Groovy** — это настоящий код, а не XML. Есть и вариант на **Kotlin** (`build.gradle.kts`) — синтаксис чуть строже, смысл тот же.

```groovy
plugins {
    id 'java'
}
```

Подключаем плагин `java`: он добавляет задачи `compileJava`, `test`, `jar`, `build` и стандартную структуру папок. Для приложений ещё бывает `application`, для Allure — `io.qameta.allure` (модуль 14).

```groovy
group = 'ru.qacourse'
version = '1.0.0'
```

Координаты проекта, как `groupId` и `version` в Maven.

```groovy
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}
```

**Toolchain** — какой версией Java собирать. Gradle найдёт установленный JDK 21 (а с плагином foojay может и сам скачать).

```groovy
repositories {
    mavenCentral()
}
```

Откуда качать библиотеки — тот же Maven Central. Gradle и Maven используют одни и те же хранилища и одинаковые координаты.

```groovy
dependencies {
    testImplementation platform('org.junit:junit-bom:5.13.4')
    testImplementation 'org.junit.jupiter:junit-jupiter'
    testImplementation 'org.assertj:assertj-core:3.27.7'
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
}
```

- координаты записываются одной строкой `'groupId:artifactId:version'`;
- **конфигурации** — аналог scope в Maven:

| Gradle | Maven | Смысл |
|---|---|---|
| `implementation` | compile | Нужна коду |
| `testImplementation` | test | Нужна тестам |
| `compileOnly` | provided | Только при компиляции (Lombok) |
| `runtimeOnly` / `testRuntimeOnly` | runtime | Только при запуске |
| `annotationProcessor` | annotationProcessorPaths | Обработчики аннотаций (Lombok) |

- `platform(...)` — подключение BOM: дальше версию JUnit можно не писать.

```groovy
test {
    useJUnitPlatform {
        excludeTags 'slow'
    }
    testLogging {
        events 'passed', 'skipped', 'failed'
    }
}
```

Настройка встроенной задачи `test`:
- `useJUnitPlatform()` — **обязательно** для JUnit 5 (без этого Gradle тесты не найдёт — классическая ошибка);
- `excludeTags 'slow'` — пропускать тесты с `@Tag("slow")`;
- `testLogging` — печатать результат каждого теста в консоль (по умолчанию Gradle молчит).

```groovy
tasks.register('smokeTest', Test) {
    description = 'Запускает только smoke-тесты'
    group = 'verification'
    testClassesDirs = sourceSets.test.output.classesDirs
    classpath = sourceSets.test.runtimeClasspath
    useJUnitPlatform {
        includeTags 'smoke'
    }
}
```

**Своя задача** `smokeTest` типа `Test`:
- `tasks.register('имя', Тип)` — объявить задачу;
- `testClassesDirs` и `classpath` — где взять скомпилированные тесты и библиотеки (те же, что у стандартной `test`);
- `includeTags 'smoke'` — только тесты с `@Tag("smoke")`.

Запуск: `./gradlew smokeTest`. Так в проектах делают отдельные команды для smoke, регресса, API- и UI-тестов.

```groovy
tasks.withType(Test).configureEach {
    systemProperty 'shop.url', System.getProperty('shop.url', 'http://localhost:8081')
}
```

**Важное отличие от Maven:** Gradle **не передаёт** `-D` из командной строки в процесс с тестами автоматически. Нужно явно пробросить: «для всех задач типа Test установить системное свойство `shop.url` из свойства, переданного Gradle, или по умолчанию localhost».

## Команды

```tabs
Windows: |
  ~~~powershell
  .\gradlew.bat test
  ~~~
macOS / Linux: |
  ~~~bash
  ./gradlew test
  ~~~
```

| Команда | Что делает |
|---|---|
| `./gradlew test` | Запустить тесты |
| `./gradlew clean test` | Удалить `build/` и запустить |
| `./gradlew smokeTest` | Своя задача |
| `./gradlew test --tests 'ru.qacourse.gradle.PriceCalculatorTest'` | Один класс |
| `./gradlew test --tests '*tenPercent'` | По маске имени метода |
| `./gradlew test -Dshop.url=http://stage:8081` | Передать свойство (пробрасывается благодаря настройке выше) |
| `./gradlew test --rerun-tasks` | Не брать результат из кэша, выполнить заново |
| `./gradlew tasks` | Список всех задач |
| `./gradlew dependencies` | Дерево зависимостей |
| `./gradlew build` | Скомпилировать, протестировать, собрать jar |

> 💡 **Up-to-date.** Если ничего не менялось, Gradle напишет `Task :test UP-TO-DATE` и **не запустит** тесты повторно — результат берётся из кэша. Это ускоряет сборку, но удивляет новичков. Нужен повторный прогон — `--rerun-tasks` или `clean`.

Результаты: `build/reports/tests/test/index.html` — готовый HTML-отчёт (откройте в браузере), `build/test-results/test/*.xml` — JUnit XML для CI.

## Практика

1. Откройте терминал в папке `exercises/m06-build-tools/gradle-demo`.
2. `./gradlew test` — первый раз Gradle скачает сам себя (~130 МБ) и библиотеки. Убедитесь, что запустились 3 теста, а медленный пропущен.
3. `./gradlew smokeTest` — запустится 1 тест.
4. Откройте `build/reports/tests/test/index.html`.
5. Добавьте в `build.gradle` задачу `slowTest`, запускающую только тесты с тегом `slow`, и запустите её.

```quiz
- q: Какой аналог Maven scope test в Gradle?
  options: [implementation, testImplementation, compileOnly]
  answer: 1
- q: Что обязательно указать в Gradle, чтобы запускались тесты JUnit 5?
  options: ["useJUnitPlatform()", "junit5 = true", "Ничего"]
  answer: 0
- q: Gradle написал Task :test UP-TO-DATE. Что это значит?
  options:
    - Ничего не изменилось, тесты не запускались — результат взят из кэша
    - Все тесты упали
    - Нужно обновить Gradle
  answer: 0
- q: Передаются ли -D свойства из командной строки Gradle в тесты автоматически?
  options: ["Да, всегда", "Нет, их нужно явно пробросить через systemProperty"]
  answer: 1
- q: Где Gradle кладёт HTML-отчёт о тестах?
  options: [build/reports/tests/test/index.html, target/surefire-reports, out/]
  answer: 0
```
