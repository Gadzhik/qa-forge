---
title: "11.6 JDBC и пул соединений HikariCP"
minutes: 45
---

**JDBC** (Java Database Connectivity) — стандартный способ Java-программы работать с реляционной БД. Один и тот же код работает с PostgreSQL, H2, MySQL, Oracle — меняется только **драйвер** (jar от производителя БД) и адрес.

## Адрес подключения (JDBC URL)

```text
jdbc:postgresql://localhost:5432/qaburger
jdbc:h2:tcp://localhost:9093/mem:shop
jdbc:mysql://db.company.ru:3306/shop
```

`jdbc:` + тип БД + адрес. Плюс логин и пароль.

## Простейший запрос

```java
try (Connection c = DriverManager.getConnection(url, user, password);
     PreparedStatement st = c.prepareStatement("SELECT stock FROM products WHERE name = ?")) {
    st.setString(1, "Кола 0.5");
    try (ResultSet rs = st.executeQuery()) {
        if (rs.next()) {
            int stock = rs.getInt("stock");
        }
    }
}
```

Разбор:
- **`Connection`** — соединение с базой. Открыть дорого (сеть, авторизация), обязательно закрыть;
- **`DriverManager.getConnection(url, user, password)`** — открыть соединение; нужный драйвер Java находит сама по префиксу URL;
- **`PreparedStatement`** — подготовленный запрос с **параметрами** `?`;
- **`st.setString(1, "Кола 0.5")`** — подставить значение в **первый** `?` (нумерация с **1**!). Есть `setInt`, `setLong`, `setBigDecimal`, `setDate`, `setObject`...;
- **`executeQuery()`** — выполнить `SELECT`, получить **`ResultSet`** — курсор по строкам результата;
- **`rs.next()`** — перейти к следующей строке; `false` — строки кончились. Изначально курсор **перед** первой строкой, поэтому `next()` нужен даже для одной строки;
- **`rs.getInt("stock")`** — значение столбца текущей строки (по имени или номеру с 1); `getString`, `getBigDecimal`, `getTimestamp`...;
- **try-with-resources** (урок 5.1) закрывает `ResultSet`, `Statement`, `Connection` в обратном порядке — даже при исключении.

Для `INSERT`/`UPDATE`/`DELETE` — **`executeUpdate()`**: возвращает число изменённых строк.

## SQL-инъекции: почему только параметры

```java
// ❌ НИКОГДА ТАК
String sql = "SELECT stock FROM products WHERE name = '" + productName + "'";
```

Если `productName` = `x' OR '1'='1`, запрос превратится в `... WHERE name = 'x' OR '1'='1'` — вернёт все строки. В реальных системах так воруют данные и удаляют таблицы. Это **SQL-инъекция** — одна из самых известных уязвимостей.

С `?` значение передаётся **отдельно** от текста запроса, база никогда не воспримет его как часть SQL. Проверка задания специально подаёт `x' OR '1'='1` в `stockOf`.

> 💡 Тестировщику это знание пригодится дважды: не писать уязвимый код в тестах и проверять приложение — пробовать `' OR '1'='1` и `'; DROP TABLE...` в полях ввода и параметрах API (урок 1.5).

## Пул соединений: HikariCP

Открывать соединение на каждый запрос медленно. **Пул** держит несколько открытых соединений и раздаёт их: взял — поработал — вернул. **HikariCP** — самый популярный пул в Java (используется в Spring Boot по умолчанию).

```java
HikariConfig config = new HikariConfig();
config.setJdbcUrl(url);
config.setUsername(user);
config.setPassword(password);
config.setMaximumPoolSize(3);
HikariDataSource dataSource = new HikariDataSource(config);

try (Connection c = dataSource.getConnection()) {   // взять из пула
    ...
}                                                   // close() — ВЕРНУТЬ в пул, а не закрыть
```

- `HikariConfig` — настройки; `setMaximumPoolSize(3)` — не больше 3 соединений (тестам много не надо);
- **`DataSource`** — стандартный интерфейс «источника соединений»; пул его реализует;
- `getConnection()` берёт соединение из пула, `close()` возвращает его обратно;
- `dataSource.close()` — закрыть пул целиком в конце (у нас `ShopDb implements AutoCloseable`).

## Транзакция в JDBC

```java
try (Connection c = dataSource.getConnection()) {
    c.setAutoCommit(false);                // начать транзакцию
    try (...) {
        ... dec.executeUpdate() ...
        ... inc.executeUpdate() ...
        c.commit();                        // зафиксировать
    } catch (SQLException e) {
        c.rollback();                      // откатить при ошибке
        throw e;
    }
}
```

В задании `moveStock` уменьшает остаток условием `WHERE name = ? AND stock >= ?` — если остатка не хватает, `executeUpdate()` вернёт 0 строк, и мы откатываемся.

## ORM и Spring — для общего развития

В приложениях (в том числе QA Burger) с базой обычно работают не голым JDBC, а через **ORM** — Hibernate/JPA: объекты Java автоматически сохраняются в таблицы (`@Entity` в `shop-app`). В тестах чаще хватает JDBC или **Spring JdbcTemplate** — тонкой обёртки, убирающей шаблонный код:

```java
int stock = jdbc.queryForObject("SELECT stock FROM products WHERE name = ?", Integer.class, "Кола 0.5");
```

```task
id: m11-t06
title: JDBC и HikariCP
module: exercises/m11-databases
test: ShopDbCheck
file: exercises/m11-databases/src/main/java/ru/qacourse/m11/ShopDb.java
solution: solutions/exercises/m11-databases/src/main/java/ru/qacourse/m11/ShopDb.java
text: |
  В `ShopDb` (пул HikariCP уже создаётся в конструкторе, пример `productCount` готов) реализуйте:
  `stockOf`, `orderStatus`, `productNames`, `moveStock` (транзакция).
  Только `PreparedStatement` с `?` — проверка попробует SQL-инъекцию.
hint: |
  Шаблон: `try (Connection c = dataSource.getConnection(); PreparedStatement st = c.prepareStatement("...")) { st.setString(1, ...); try (ResultSet rs = st.executeQuery()) { ... } }`.
```

```quiz
- q: С какого номера нумеруются параметры PreparedStatement?
  options: ["С 1", "С 0", "С -1"]
  answer: 0
- q: Зачем вызывать rs.next() перед чтением первой строки?
  options: ["Курсор изначально стоит перед первой строкой", "Чтобы пропустить заголовок", "Это не нужно"]
  answer: 0
- q: Почему нельзя склеивать значения в SQL строками?
  options: ["SQL-инъекция: значение может изменить смысл запроса", "Так медленнее", "Строки нельзя склеивать в Java"]
  answer: 0
- q: Что делает close() у соединения, взятого из пула HikariCP?
  options: ["Возвращает соединение в пул", "Закрывает базу", "Удаляет данные"]
  answer: 0
- q: Какой метод выполняет UPDATE и возвращает число изменённых строк?
  options: ["executeUpdate()", "executeQuery()", "next()"]
  answer: 0
```
