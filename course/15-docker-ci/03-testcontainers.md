---
title: "15.3 Testcontainers"
minutes: 40
---

`docker compose up` нужно не забыть выполнить перед тестами, а данные в базе копятся между прогонами. **Testcontainers** — Java-библиотека, которая **прямо из теста** запускает контейнер, отдаёт его адрес и удаляет после теста. Каждый прогон — с чистого листа.

## Подключение

```xml
<dependency><groupId>org.testcontainers</groupId><artifactId>postgresql</artifactId><scope>test</scope></dependency>
<dependency><groupId>org.testcontainers</groupId><artifactId>junit-jupiter</artifactId><scope>test</scope></dependency>
```

Версии — из `testcontainers-bom` (в `exercises/pom.xml`). Есть модули для PostgreSQL, MySQL, Kafka, Redis, MongoDB, Selenium, LocalStack (AWS) и универсальный `GenericContainer` для любого образа. Нужен запущенный Docker.

## Пример задания

```java
static PostgreSQLContainer<?> pg;
static ShopAppLauncher.Running app;

@BeforeAll
static void start() {
    pg = new PostgreSQLContainer<>("postgres:16-alpine");
    pg.start();
    app = ShopAppLauncher.start(Map.of(
            "spring.datasource.url", pg.getJdbcUrl(),
            "spring.datasource.username", pg.getUsername(),
            "spring.datasource.password", pg.getPassword()));
}

@AfterAll
static void stop() {
    if (app != null) app.close();
    if (pg != null) pg.stop();
}
```

Разбор:
- **`new PostgreSQLContainer<>("postgres:16-alpine")`** — описание контейнера (образ с тегом);
- **`pg.start()`** — скачать образ (первый раз), запустить, **дождаться готовности** базы. Порт выбирается **случайный свободный** — конфликтов с вашим PostgreSQL на 5432 не будет;
- **`pg.getJdbcUrl()`** — `jdbc:postgresql://localhost:54831/test` — настоящий адрес с выбранным портом; `getUsername()`, `getPassword()` — учётные данные;
- `ShopAppLauncher.start(...)` — запускаем QA Burger прямо в тесте (так работают все проверки курса), переопределяя адрес базы. Flyway создаст таблицы в пустой базе;
- `@AfterAll` — остановить приложение и **удалить** контейнер.

Тест — обычный: заказ через API, проверка строк в `orders`/`order_items` через JDBC к `pg.getJdbcUrl()`.

## Аннотации JUnit 5

Короче — через расширение:

```java
@Testcontainers
class PostgresTest {
    @Container
    static PostgreSQLContainer<?> pg = new PostgreSQLContainer<>("postgres:16-alpine");
    ...
}
```

- `@Testcontainers` на классе подключает расширение;
- `@Container` на **static** поле — контейнер стартует один раз на класс и останавливается после; на обычном поле — новый контейнер для **каждого** теста (медленно, но полная изоляция).

## GenericContainer — любой образ

```java
GenericContainer<?> wiremock = new GenericContainer<>("wiremock/wiremock:3.13.2")
        .withExposedPorts(8080)
        .waitingFor(Wait.forHttp("/__admin/health"));
wiremock.start();
String url = "http://" + wiremock.getHost() + ":" + wiremock.getMappedPort(8080);
```

- `withExposedPorts(8080)` — какой порт контейнера сделать доступным; `getMappedPort(8080)` — на какой случайный порт компьютера он попал;
- `waitingFor(...)` — стратегия ожидания готовности: HTTP-ответ, строка в логе (`Wait.forLogMessage(".*Started.*", 1)`), открытый порт.

## Reaper (Ryuk)

Testcontainers запускает служебный контейнер **Ryuk**, который удаляет все контейнеры теста, даже если тест «упал» вместе с JVM. В некоторых CI его отключают (`TESTCONTAINERS_RYUK_DISABLED=true`) — тогда за уборку отвечаете вы.

## Когда что

| Подход | Когда |
|---|---|
| `docker compose up` вручную | Изучение, ручное тестирование, общая инфраструктура для многих тестов |
| Testcontainers | Изолированные интеграционные тесты, CI без ручной подготовки |
| Общий тестовый стенд | Системные/E2E тесты на окружении, близком к продакшену |

```task
id: m15-t03
title: Testcontainers + QA Burger на PostgreSQL
module: exercises/m15-docker-ci
test: PostgresIntegrationTestCheck
file: exercises/m15-docker-ci/src/test/java/ru/qacourse/m15/tasks/PostgresIntegrationTest.java
solution: solutions/exercises/m15-docker-ci/src/test/java/ru/qacourse/m15/tasks/PostgresIntegrationTest.java
text: |
  Нужен запущенный Docker. В `@BeforeAll` поднимите PostgreSQL через Testcontainers и запустите на нём QA Burger
  (`ShopAppLauncher.start` с адресом базы контейнера). Тест: заказ 2 × Кола (198.00) через API → проверка в `orders` и `order_items` через JDBC.
hint: |
  Код `@BeforeAll`/`@AfterAll` — в уроке. Адрес приложения — `app.baseUrl()`, базы — `pg.getJdbcUrl()`.
```

```quiz
- q: Какой порт получает контейнер PostgreSQL в Testcontainers?
  options: ["Случайный свободный — узнать через getJdbcUrl()/getMappedPort()", "Всегда 5432", "8080"]
  answer: 0
- q: Чем @Container на static-поле отличается от обычного поля?
  options:
    - "static — один контейнер на класс, обычное поле — новый на каждый тест"
    - "Ничем"
    - "static не работает"
  answer: 0
- q: Зачем waitingFor(...) у GenericContainer?
  options: ["Дождаться, что сервис в контейнере готов, а не просто запущен", "Задержать тест на минуту", "Скачать образ"]
  answer: 0
- q: Что делает служебный контейнер Ryuk?
  options: ["Удаляет контейнеры теста, даже если JVM упала", "Запускает тесты", "Пишет отчёт"]
  answer: 0
```
