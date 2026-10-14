---
title: "7.5 Теги и запуск групп тестов"
minutes: 30
---

В проекте 2000 тестов, полный прогон идёт час. Но после каждого деплоя нужен быстрый **smoke** на 5 минут, по ночам — полный **регресс**, а перед релизом оплаты — только тесты **оплаты**. Для этого тесты помечают **тегами** и запускают группами.

## @Tag

```java
class TaggedTest {

    @Test
    @Tag("smoke")
    void newCartIsEmpty() { ... }

    @Test
    @Tag("regression")
    @Tag("promo")
    void totalWithPromo() { ... }
}
```

- у теста может быть несколько тегов;
- тег на **классе** действует на все его тесты;
- имена тегов — любые, но договоритесь в команде: `smoke`, `regression`, `api`, `ui`, `slow`, `payment`...

### Своя аннотация вместо строки

Строки легко опечатать (`"smok"`). Делают свою аннотацию-тег:

```java
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Tag("smoke")
public @interface Smoke {
}
```

- `@interface` — так объявляется **аннотация**;
- `@Target` — куда её можно ставить (класс, метод);
- `@Retention(RUNTIME)` — аннотация доступна во время выполнения (иначе JUnit её не увидит);
- `@Tag("smoke")` на самой аннотации — «мета-аннотация»: всё, что помечено `@Smoke`, получает тег `smoke`.

Теперь пишем `@Smoke` вместо `@Tag("smoke")`, а опечатку поймает компилятор.

## Запуск по тегам

**Maven** (surefire понимает свойства `groups` и `excludedGroups`):

```bash
./mvnw test -Dgroups=smoke                    # только smoke
./mvnw test -Dgroups='smoke | payment'        # smoke ИЛИ payment
./mvnw test -Dgroups='regression & !slow'     # regression И НЕ slow
./mvnw test -DexcludedGroups=slow             # всё, кроме slow
```

Это **выражения тегов**: `|` — или, `&` — и, `!` — не, скобки для группировки. Кавычки обязательны: иначе `|`, `&`, `!` перехватит терминал.

Можно зафиксировать в профиле `pom.xml` (урок 6.3):

```xml
<profile>
    <id>smoke</id>
    <properties>
        <groups>smoke</groups>
    </properties>
</profile>
```

и запускать `./mvnw test -Psmoke`.

**Gradle** — в `build.gradle` (урок 6.4):

```groovy
test {
    useJUnitPlatform {
        includeTags 'smoke'
        excludeTags 'slow'
    }
}
```

или своя задача `smokeTest`, как в демо-проекте. Через командную строку удобно так:

```groovy
test {
    useJUnitPlatform {
        if (project.hasProperty('tags')) includeTags project.property('tags')
    }
}
```

`./gradlew test -Ptags=smoke` (`-P` — свойство проекта Gradle).

**IDEA:** Run → Edit Configurations → создать JUnit-конфигурацию → *Test kind: Tags* → `smoke`.

## Наборы тестов (Suite)

Иногда нужен именованный набор: «всё из пакета api с тегом smoke». Для этого есть `@Suite`:

```java
@Suite
@SelectPackages("ru.qacourse.tests.api")
@IncludeTags("smoke")
class ApiSmokeSuite {
}
```

(нужна зависимость `junit-platform-suite`). Запуск: `./mvnw test -Dtest=ApiSmokeSuite`.

> 💡 На практике теги + профили Maven / задачи Gradle закрывают 95% потребностей. Suite встречаются реже.

```task
id: m07-t05
title: Теги
module: exercises/m07-junit
test: TaggedTestCheck
file: exercises/m07-junit/src/test/java/ru/qacourse/m07/tasks/TaggedTest.java
solution: solutions/exercises/m07-junit/src/test/java/ru/qacourse/m07/tasks/TaggedTest.java
text: |
  Пометьте `newCartIsEmpty` тегом `smoke`, а `totalWithPromo` — тегами `regression` и `promo`.
  Затем в терминале запустите только smoke-тесты класса:

  `./mvnw -f exercises/m07-junit/pom.xml test -Dtest=TaggedTest -Dgroups=smoke`

  и убедитесь, что запустился один тест.
hint: |
  `@Tag("smoke")` из пакета `org.junit.jupiter.api`. Несколько тегов — несколько аннотаций `@Tag` подряд.
```

```quiz
- q: Как запустить в Maven все тесты, кроме медленных?
  options: ["-DexcludedGroups=slow", "-Dgroups=!fast", "-Dskip=slow"]
  answer: 0
- q: "Что означает выражение `regression & !slow`?"
  options:
    - Тесты с тегом regression, у которых нет тега slow
    - Тесты regression или slow
    - Только slow
  answer: 0
- q: Зачем делать свою аннотацию @Smoke вместо @Tag("smoke")?
  options:
    - Опечатку в имени поймает компилятор
    - Так тесты быстрее
    - Без неё теги не работают
  answer: 0
- q: Зачем @Retention(RUNTIME) у своей аннотации?
  options:
    - Чтобы аннотация была видна во время выполнения тестов
    - Чтобы ускорить компиляцию
    - Это необязательно
  answer: 0
```
