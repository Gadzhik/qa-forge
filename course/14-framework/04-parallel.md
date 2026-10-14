---
title: "14.4 Параллельный запуск тестов"
minutes: 35
---

## Настройка JUnit 5

`exercises/m14-framework/src/test/resources/junit-platform.properties`:

```properties
junit.jupiter.execution.parallel.enabled=true
junit.jupiter.execution.parallel.mode.default=same_thread
junit.jupiter.execution.parallel.mode.classes.default=concurrent
junit.jupiter.execution.parallel.config.strategy=fixed
junit.jupiter.execution.parallel.config.fixed.parallelism=3
```

| Свойство | Значение |
|---|---|
| `parallel.enabled=true` | включить параллельность вообще |
| `mode.default=same_thread` | методы внутри класса — по очереди |
| `mode.classes.default=concurrent` | разные классы — одновременно |
| `config.strategy=fixed` + `parallelism=3` | ровно 3 потока |
| `config.strategy=dynamic` + `dynamic.factor=1` | потоков = число ядер × factor |

Варианты:
- классы параллельно, методы последовательно — **безопасный старт** (выбран у нас);
- всё параллельно (`mode.default=concurrent`) — максимальная скорость, максимум требований к независимости.

Управление отдельными тестами:

```java
@Execution(ExecutionMode.SAME_THREAD)   // этот класс/метод — не параллельно с соседями
@ResourceLock("promo-codes")            // тесты с одинаковым ресурсом не идут одновременно
@Isolated                               // этот класс идёт один, все остальные ждут
```

> 💡 Реальная история из подготовки курса: в модуле 14 две **проверки** заданий пошли параллельно (из-за этого же файла настроек) и одновременно меняли общий системный адрес приложения — одна проверка обращалась к чужому экземпляру QA Burger и получала `Connection refused`. Решение — `@Isolated` на проверках. Общее изменяемое состояние (здесь — системные свойства) и параллельность несовместимы.

## Maven Surefire: параллельность процессами

Другой уровень — несколько JVM:

```xml
<configuration>
    <forkCount>3</forkCount>
    <reuseForks>true</reuseForks>
</configuration>
```

Тест-классы распределяются по 3 процессам. Статические поля в разных процессах не пересекаются, зато процессы тяжелее потоков. Для UI-тестов процессы иногда надёжнее.

## TestNG

```xml
<suite name="regression" parallel="classes" thread-count="3">
```

`parallel="methods" | "classes" | "tests"`, плюс `@DataProvider(parallel = true)`.

## Почему тесты падают только в параллели

| Причина | Пример | Решение |
|---|---|---|
| **Общие тестовые данные** | два теста используют пользователя `user` и его корзину | свой пользователь на тест (`@NewUser`, урок 9.11) |
| **Статическое состояние** | `static String token`, `RestAssured.baseURI`, `Configuration.baseUrl` меняются в тестах | без изменяемой статики, ThreadLocal |
| **Порядок** | тест B рассчитывает на данные теста A | независимые тесты |
| **Глобальные операции** | тест вызывает `/api/admin/reset` — стирает данные соседей | изолировать (`@Isolated`) или убрать |
| **Ресурсы машины** | 10 браузеров съели память | меньше потоков / Selenium Grid |
| **Счётчики «всего»** | «в базе ровно 5 заказов» — соседи добавили ещё | проверять свои данные, а не глобальные количества |

Обратите внимание: наш `BaseTest` использует **одного** пользователя `user` для всех тестов и очищает его корзину. При параллельном запуске классов `OrderFlowTest` и `PromoTest` они **могут мешать друг другу** — добавлять товары в одну корзину. Это сознательно оставленный изъян для упражнения ниже.

> 🧪 **Попробуй сам.** Запустите все тесты модуля несколько раз: `./mvnw -f exercises/m14-framework/pom.xml test -Dtest='*Test'`. Если увидите случайное падение — это гонка за общую корзину. Исправьте `BaseTest`: регистрируйте нового пользователя на каждый тест (API `/api/auth/register` + Datafaker/UUID) — и гонка исчезнет.

## Как понять, что параллельность работает

- в отчёте Allure — вкладка **Timeline**: полосы тестов в разных потоках перекрываются по времени;
- в логах — имена потоков (`ForkJoinPool-1-worker-2`);
- время прогона уменьшилось.

```quiz
- q: Какая настройка JUnit запускает классы параллельно, а методы внутри класса — последовательно?
  options:
    - "mode.default=same_thread и mode.classes.default=concurrent"
    - "parallel.enabled=false"
    - "mode.default=concurrent"
  answer: 0
- q: Что делает @Isolated?
  options: ["Класс выполняется один, остальные ждут", "Отключает тест", "Запускает в отдельном процессе"]
  answer: 0
- q: Почему два теста с одним пользователем мешают друг другу в параллели?
  options: ["Они меняют одни и те же данные (корзину) одновременно", "JUnit запрещает", "Не мешают"]
  answer: 0
- q: Где в Allure видно, что тесты шли параллельно?
  options: ["Вкладка Timeline", "Categories", "Overview → Environment"]
  answer: 0
```
