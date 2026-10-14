---
title: "11.2 SQL: SELECT — выборка данных"
minutes: 45
---

Все примеры можно выполнять в H2-консоли QA Burger (http://localhost:8081/h2-console).

## Базовый SELECT

```sql
SELECT name, price FROM products;
```

- `SELECT` — «выбрать»;
- `name, price` — какие столбцы; `*` — все столбцы;
- `FROM products` — из какой таблицы;
- `;` — конец запроса (в консоли можно не писать).

SQL **не чувствителен к регистру ключевых слов**: `select` = `SELECT`. Принято писать ключевые слова заглавными — так запрос легче читать.

## WHERE — фильтр

```sql
SELECT name, price FROM products WHERE category = 'DRINK';
```

- строки — в **одинарных** кавычках `'DRINK'` (двойные в SQL — для имён столбцов);
- `=` — сравнение (в SQL нет `==`).

| Оператор | Пример |
|---|---|
| `=`, `<>` (или `!=`), `<`, `>`, `<=`, `>=` | `price < 100` |
| `AND`, `OR`, `NOT` | `category = 'DRINK' AND price < 100` |
| `BETWEEN a AND b` | `price BETWEEN 100 AND 200` (включительно) |
| `IN (...)` | `category IN ('DRINK', 'DESSERT')` |
| `LIKE` | `name LIKE '%бургер%'` |
| `IS NULL` / `IS NOT NULL` | `promo_code IS NULL` |

### LIKE — поиск по шаблону

- `%` — любые символы (сколько угодно, в т.ч. ноль);
- `_` — ровно один символ.

```sql
SELECT name FROM products WHERE name LIKE 'Ч%';            -- начинается на «Ч»
SELECT name FROM products WHERE LOWER(name) LIKE '%бургер%'; -- содержит «бургер» в любом регистре
```

`LIKE` чувствителен к регистру (в PostgreSQL есть `ILIKE` — без учёта регистра). Универсально — привести к нижнему регистру функцией `LOWER(...)`.

### NULL — особое значение

`NULL` — «значения нет». С ним **нельзя** сравнивать через `=`:

```sql
SELECT * FROM orders WHERE promo_code = NULL;    -- ❌ всегда пусто!
SELECT * FROM orders WHERE promo_code IS NULL;   -- ✅
```

Любое сравнение с `NULL` даёт «неизвестно», а не `true`. Классический вопрос собеседования.

## ORDER BY — сортировка

```sql
SELECT name, price FROM products ORDER BY price;          -- по возрастанию (ASC — по умолчанию)
SELECT name, price FROM products ORDER BY price DESC;     -- по убыванию
SELECT name, category, price FROM products ORDER BY category, price DESC;  -- по категории, внутри — по цене
```

> ⚠️ **Без ORDER BY порядок строк не гарантирован.** Сегодня база вернула строки по id, завтра — иначе. Тест, сравнивающий результат без сортировки с ожидаемым списком «по порядку», нестабилен.

## LIMIT — ограничение

```sql
SELECT name, price FROM products ORDER BY price DESC LIMIT 3;          -- три самых дорогих
SELECT name FROM products ORDER BY id LIMIT 5 OFFSET 5;               -- вторая «страница» по 5
```

`LIMIT n OFFSET m` — пропустить m, взять n. Именно так API реализует пагинацию (урок 8.3). В Oracle и MS SQL синтаксис другой (`FETCH FIRST 3 ROWS ONLY`, `TOP 3`).

## Функции и выражения

```sql
SELECT name, price, price * 0.9 AS sale_price FROM products;
SELECT UPPER(name), LENGTH(name) FROM products;
SELECT COUNT(*) FROM products WHERE price < 100;
SELECT DISTINCT category FROM products;          -- уникальные значения
```

- `AS sale_price` — **псевдоним** (alias) столбца в результате;
- `COUNT(*)` — количество строк (подробнее — урок 11.3);
- `DISTINCT` — убрать повторы.

## Порядок выполнения

Запрос **пишется** в порядке `SELECT → FROM → WHERE → ORDER BY → LIMIT`, а **выполняется** так:

```text
FROM (взять таблицу) → WHERE (отфильтровать) → SELECT (выбрать столбцы) → ORDER BY → LIMIT
```

Поэтому псевдоним из `SELECT` нельзя использовать в `WHERE` (на этом этапе его ещё нет), а в `ORDER BY` — можно.

## Задание: SQL-запросы как константы

В `exercises/m11-databases/src/main/java/ru/qacourse/m11/Queries.java` — строковые константы с SQL. Сначала отладьте запрос в H2-консоли, потом вставьте в Java-строку. Проверка выполнит ваш запрос в базе QA Burger и сравнит результат.

> 💡 Длинный запрос в Java удобно писать текстовым блоком (урок 3.5): `"""SELECT ... FROM ... WHERE ..."""`.

```task
id: m11-t02
title: SELECT
module: exercises/m11-databases
test: SelectQueriesCheck
file: exercises/m11-databases/src/main/java/ru/qacourse/m11/Queries.java
solution: solutions/exercises/m11-databases/src/main/java/ru/qacourse/m11/Queries.java
text: |
  Напишите 4 запроса из раздела «11.2 SELECT» в `Queries.java`:
  `DRINKS_BY_PRICE`, `COUNT_CHEAPER_THAN_100`, `BURGERS_BY_NAME` (без учёта регистра), `TOP3_EXPENSIVE`.
hint: |
  `WHERE category = 'DRINK' ORDER BY price`, `COUNT(*) ... WHERE price < 100`, `LOWER(name) LIKE '%бургер%'`, `ORDER BY price DESC LIMIT 3`.
```

```quiz
- q: Как правильно найти заказы без промокода?
  options: ["WHERE promo_code IS NULL", "WHERE promo_code = NULL", "WHERE promo_code = ''"]
  answer: 0
- q: "Что найдёт LIKE '_ола%'?"
  options: ["Строки, где один любой символ, затем «ола», затем что угодно", "Только «ола»", "Строки, начинающиеся с «_ола»"]
  answer: 0
- q: Гарантирован ли порядок строк без ORDER BY?
  options: ["Нет", "Да, по id", "Да, по времени добавления"]
  answer: 0
- q: Как выбрать вторую страницу по 10 записей?
  options: ["LIMIT 10 OFFSET 10", "LIMIT 20", "OFFSET 2"]
  answer: 0
- q: В каких кавычках пишутся строки в SQL?
  options: ["В одинарных 'текст'", "В двойных", "Без кавычек"]
  answer: 0
```
