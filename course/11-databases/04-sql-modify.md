---
title: "11.4 SQL: INSERT, UPDATE, DELETE и транзакции"
minutes: 40
---

## INSERT — добавить

```sql
INSERT INTO promo_codes (code, percent, valid_until)
VALUES ('TEST50', 50, '2099-01-01');
```

- `INSERT INTO таблица (столбцы)` — куда и в какие столбцы;
- `VALUES (...)` — значения в том же порядке;
- столбцы с автоматическими значениями (`id` с IDENTITY) не указываем — база заполнит сама.

Несколько строк сразу: `VALUES ('A', 5, '2099-01-01'), ('B', 7, '2099-01-01');`

## UPDATE — изменить

```sql
UPDATE products SET price = price * 1.1 WHERE category = 'DRINK';
UPDATE products SET stock = 0, description = 'Нет в наличии' WHERE id = 16;
```

- `SET столбец = новое значение` (можно выражение от старого);
- **`WHERE`** — каким строкам.

> ❌ **UPDATE и DELETE без WHERE изменяют ВСЮ таблицу.** `UPDATE products SET price = 0;` — все товары бесплатно. На общем тестовом стенде это катастрофа для всей команды. Перед выполнением изменяющего запроса сначала выполните `SELECT` с тем же `WHERE` и посмотрите, что попадёт под изменение.

## DELETE — удалить

```sql
DELETE FROM reviews WHERE rating < 3;
```

`TRUNCATE TABLE reviews;` — очистить таблицу целиком (быстрее, но без `WHERE` и обычно без отката).

## Ограничения защищают данные

Попробуйте:

```sql
INSERT INTO users (username, email, password_hash, role, created_at)
VALUES ('user', 'x@x', 'x', 'USER', CURRENT_TIMESTAMP);
```

Ошибка: `Unique index or primary key violation` — `username` уникален. А удалить товар, на который ссылается позиция корзины, без `ON DELETE CASCADE` не дала бы **внешний ключ**. Ограничения — последняя линия обороны от «грязных» данных; тестировщик может проверять, что они есть (попытаться вставить дубликат прямо в базу).

## Транзакции

**Транзакция** — группа изменений, которая выполняется **целиком или никак**. Пример: перенос остатка между товарами — «убавить у одного» и «прибавить другому». Если после первого шага произойдёт сбой, данные испортятся. В транзакции — либо оба шага, либо ни одного.

```sql
BEGIN;                                                         -- начать (в H2 консоли: SET AUTOCOMMIT FALSE)
UPDATE products SET stock = stock - 5 WHERE name = 'Лимонад домашний';
UPDATE products SET stock = stock + 5 WHERE name = 'Морс клюквенный';
COMMIT;                                                        -- зафиксировать
-- или ROLLBACK;                                               -- отменить всё с начала транзакции
```

Свойства транзакций — **ACID** (вопрос на собеседовании):
- **A**tomicity — атомарность: всё или ничего;
- **C**onsistency — согласованность: из корректного состояния в корректное;
- **I**solation — изоляция: параллельные транзакции не мешают друг другу (с оговорками об уровнях изоляции);
- **D**urability — долговечность: после COMMIT данные не потеряются.

### Транзакции в тестах — откат после проверки

Полезный приём для тестов, которые меняют данные напрямую: выполнить изменения в транзакции и в конце **откатить** — база останется чистой. Проверка задания так и работает:

```java
try (Connection c = connect()) {
    c.setAutoCommit(false);            // начать транзакцию вручную
    try {
        ... выполнить INSERT/UPDATE и проверить результат ...
    } finally {
        c.rollback();                  // откатить всё
    }
}
```

## Автокоммит

По умолчанию JDBC и большинство консолей работают в режиме **autocommit**: каждый запрос — отдельная транзакция, фиксируется сразу. В DBeaver режим видно на панели инструментов (Auto / Manual commit).

```task
id: m11-t04
title: INSERT, UPDATE, DELETE
module: exercises/m11-databases
test: ModifyQueriesCheck
file: exercises/m11-databases/src/main/java/ru/qacourse/m11/Queries.java
solution: solutions/exercises/m11-databases/src/main/java/ru/qacourse/m11/Queries.java
text: |
  Раздел «11.4» в `Queries.java`: `INSERT_PROMO` (3 параметра), `RAISE_PRICES` (+10% в категории), `DELETE_LOW_REVIEWS` (оценка ниже параметра).
  Проверка выполнит их в транзакции и откатит — данные не испортятся.
hint: |
  `INSERT INTO promo_codes (code, percent, valid_until) VALUES (?, ?, ?)`, `UPDATE products SET price = price * 1.1 WHERE category = ?`, `DELETE FROM reviews WHERE rating < ?`.
```

```quiz
- q: Что сделает UPDATE products SET price = 0 без WHERE?
  options: ["Обнулит цены ВСЕХ товаров", "Ничего", "Ошибку"]
  answer: 0
- q: Что такое транзакция?
  options: ["Группа изменений, выполняемая целиком или никак", "Один SELECT", "Резервная копия"]
  answer: 0
- q: Что делает ROLLBACK?
  options: ["Отменяет изменения с начала транзакции", "Фиксирует изменения", "Удаляет таблицу"]
  answer: 0
- q: Что означает A в ACID?
  options: ["Atomicity — всё или ничего", "Availability", "Authorization"]
  answer: 0
- q: Как безопасно проверить UPDATE перед выполнением?
  options: ["Сначала выполнить SELECT с тем же WHERE", "Выполнить и посмотреть", "Никак"]
  answer: 0
```
