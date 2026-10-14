---
title: "11.7 База данных в автотестах"
minutes: 40
---

## Три роли базы в тестах

1. **Проверка** — после действия через API/UI убедиться, что в базе правильные данные.
2. **Подготовка** — создать нужное состояние напрямую (быстрее, чем через API, и позволяет создать то, что через API не создать: «заказ, оплаченный вчера»).
3. **Уборка** — удалить созданное тестом.

## Проверка побочных эффектов

API ответил «201, заказ создан, total 289.90». Можно ли верить? Ответ формирует один код, а сохраняет в базу — другой. Баги бывают ровно между ними: ответ правильный, а в базе — нет (или наоборот). BUG-404 в QA Burger — как раз такой: в базу (и в ответ) попадает сумма, округлённая до рублей.

Тест задания:

```java
@Test
void orderIsSavedCorrectly() throws Exception {
    int stockBefore = db.stockOf("Классический бургер");

    // ... через API: вход, очистка корзины, 1 бургер в корзину, оформление заказа → orderId

    try (Connection c = DriverManager.getConnection(ShopConfig.dbUrl(), ShopConfig.dbUser(), ShopConfig.dbPassword())) {
        try (PreparedStatement st = c.prepareStatement("SELECT total, status FROM orders WHERE id = ?")) {
            st.setLong(1, orderId);
            try (ResultSet rs = st.executeQuery()) {
                assertThat(rs.next()).as("заказ есть в таблице orders").isTrue();
                assertThat(rs.getBigDecimal("total")).isEqualByComparingTo(new BigDecimal("289.90"));
                assertThat(rs.getString("status")).isEqualTo("NEW");
            }
        }
        // ... order_items: ровно одна строка, quantity = 1
    }
    assertThat(db.stockOf("Классический бургер")).isEqualTo(stockBefore - 1);
}
```

Обратите внимание:
- **остаток запоминается до действия**, а проверяется **разница** — тест не зависит от того, сколько заказов сделали до него;
- `assertThat(rs.next()).isTrue()` перед чтением — иначе при отсутствии строки получим непонятную ошибку SQL вместо понятного «заказа нет»;
- `isEqualByComparingTo` для денег (`BigDecimal`);
- адрес базы берётся из конфигурации `ShopConfig.dbUrl()` — тот же тест работает и с H2 (по умолчанию), и с PostgreSQL (`-Dshop.db.url=jdbc:postgresql://localhost:5432/qaburger`).

## Асинхронные изменения

Если запись в базу происходит **не сразу** (через очередь, фоновую задачу — как статус COOKING у QA Burger), прямая проверка после запроса упадёт. Нужно **ждать** условие с таймаутом (Awaitility, модуль 13):

```java
await().atMost(Duration.ofSeconds(15)).until(() -> db.orderStatus(id).orElse("").equals("READY"));
```

## Подготовка данных

```java
@BeforeEach
void preparePromo() throws SQLException {
    execute("INSERT INTO promo_codes (code, percent, valid_until) VALUES (?, ?, ?)",
            "AUTOTEST_" + UUID.randomUUID().toString().substring(0, 6).toUpperCase(), 15, Date.valueOf("2099-01-01"));
}
```

Правила:
- уникальные значения (урок 9.10);
- **осторожно с бизнес-логикой**: вставка «в обход» приложения может создать состояние, невозможное в жизни (заказ без позиций), и тест будет проверять несуществующую ситуацию;
- пароли хранятся хешами — пользователя проще создать через API, чем считать хеш в тесте.

## Уборка

- удалять **то, что создал тест**, по id/уникальному признаку — в `@AfterEach`;
- для изолированных стендов — откат транзакции (урок 11.4) или полный сброс (`/api/admin/reset`, пересоздание контейнера);
- никогда не чистить «всё подряд» на общем стенде.

## Тестирование миграций — для общего развития

Изменения схемы базы делают **миграциями** (Flyway, Liquibase): файлы `V1__...sql`, `V2__...sql` применяются по порядку. Что тестируют: миграция применяется к копии продакшен-данных без ошибок, старые данные не теряются, приложение запускается после миграции, откат (если предусмотрен) работает.

## Testcontainers

В модуле 15 вы узнаете **Testcontainers** — библиотеку, которая прямо из теста поднимает настоящий PostgreSQL в Docker, а после — удаляет. Каждый прогон начинается с чистой базы, и не нужно ничего запускать вручную.

```task
id: m11-t07
title: Проверка заказа в базе
module: exercises/m11-databases
test: OrderDbTestCheck
file: exercises/m11-databases/src/test/java/ru/qacourse/m11/tasks/OrderDbTest.java
solution: solutions/exercises/m11-databases/src/test/java/ru/qacourse/m11/tasks/OrderDbTest.java
text: |
  Создайте заказ через API (1 «Классический бургер»), затем через JDBC проверьте строку в `orders` (total 289.90, status NEW),
  позицию в `order_items` (одна, quantity 1) и уменьшение остатка на 1.

  Проверка ловит BUG-404. Запуск из IDEA — против работающего QA Burger: база доступна по `jdbc:h2:tcp://localhost:9093/mem:shop`.
hint: |
  Используйте `ShopDb` из задания 11.6 для остатка и `DriverManager.getConnection(ShopConfig.dbUrl(), ShopConfig.dbUser(), ShopConfig.dbPassword())` для своих запросов.
```

```quiz
- q: Зачем проверять базу, если API вернул правильный ответ?
  options:
    - "Ответ и сохранение — разный код; баг может быть в сохранении"
    - "Не нужно"
    - "Чтобы тест был длиннее"
  answer: 0
- q: Почему остаток проверяют как разницу «до» и «после»?
  options: ["Тест не зависит от действий других тестов", "Так быстрее", "Иначе SQL не работает"]
  answer: 0
- q: Чем опасна подготовка данных прямо в базе?
  options:
    - "Можно создать состояние, невозможное в реальной работе приложения"
    - "Это всегда запрещено"
    - "Ничем"
  answer: 0
- q: Как проверить запись, которая появляется в базе асинхронно?
  options: ["Ждать условие с таймаутом (Awaitility)", "Thread.sleep(10000)", "Проверить сразу"]
  answer: 0
```
