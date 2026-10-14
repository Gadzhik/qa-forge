---
title: "11.3 SQL: JOIN, группировка, подзапросы"
minutes: 50
---

## Агрегатные функции

Считают одно значение по многим строкам:

| Функция | Что считает |
|---|---|
| `COUNT(*)` | количество строк |
| `COUNT(столбец)` | количество **не-NULL** значений |
| `SUM(x)`, `AVG(x)` | сумма, среднее |
| `MIN(x)`, `MAX(x)` | минимум, максимум |

```sql
SELECT COUNT(*), MIN(price), MAX(price), AVG(price) FROM products;
```

## GROUP BY — группировка

«Количество товаров **в каждой** категории»:

```sql
SELECT category, COUNT(*) AS cnt
FROM products
GROUP BY category
ORDER BY category;
```

```text
CATEGORY | CNT
---------+----
BURGER   | 6
DESSERT  | 4
DRINK    | 5
SIDE     | 5
```

`GROUP BY category` разбивает строки на группы с одинаковой категорией, а агрегат считается **в каждой группе**. Правило: в `SELECT` могут быть только столбцы из `GROUP BY` и агрегаты.

Аналог из Java — `Collectors.groupingBy(Item::category, counting())` (урок 5.6).

### HAVING — фильтр групп

```sql
SELECT category, AVG(price) AS avg_price
FROM products
GROUP BY category
HAVING AVG(price) > 200;
```

- `WHERE` фильтрует **строки до** группировки;
- `HAVING` фильтрует **группы после** группировки (там уже есть агрегаты).

## JOIN — соединение таблиц

Данные разложены по таблицам, а нужен общий результат: «название товара и его оценки». Названия — в `products`, оценки — в `reviews`.

```sql
SELECT p.name, r.rating, r.author
FROM products p
JOIN reviews r ON r.product_id = p.id;
```

- `products p` — **псевдоним** таблицы: дальше пишем `p.name` вместо `products.name`;
- `JOIN reviews r ON r.product_id = p.id` — соединить строки, у которых выполняется условие: отзыв относится к товару;
- результат — по строке на **каждую пару** «товар–отзыв».

### Виды JOIN

```text
 products            reviews
 ┌─────────┐        ┌──────────┐
 │ 1 Бургер│───────▶│ 5 ★ к 1  │
 │ 1 Бургер│───────▶│ 4 ★ к 1  │
 │ 2 Кола  │  нет   │          │
 └─────────┘ отзывов└──────────┘
```

| JOIN | Что вернёт |
|---|---|
| `INNER JOIN` (просто `JOIN`) | только пары, где есть совпадение. Кола без отзывов **не попадёт** |
| `LEFT JOIN` | **все** строки левой таблицы; где нет пары — `NULL`. Кола попадёт с `rating = NULL` |
| `RIGHT JOIN` | все строки правой |
| `FULL JOIN` | все строки обеих |
| `CROSS JOIN` | каждая с каждой (декартово произведение) |

«Товары без отзывов» — классика с `LEFT JOIN`:

```sql
SELECT p.name
FROM products p
LEFT JOIN reviews r ON r.product_id = p.id
WHERE r.id IS NULL;
```

## JOIN + GROUP BY

«Средняя оценка каждого товара, у которого есть отзывы»:

```sql
SELECT p.name, AVG(r.rating * 1.0) AS avg_rating
FROM products p
JOIN reviews r ON r.product_id = p.id
GROUP BY p.name;
```

`r.rating * 1.0` — превращаем целое в дробное, иначе в некоторых БД `AVG` целых чисел округлится до целого (вспомните целочисленное деление, урок 3.3).

«Сколько потратил каждый пользователь»:

```sql
SELECT u.username, SUM(o.total) AS total_sum
FROM users u
JOIN orders o ON o.user_id = u.id
GROUP BY u.username
ORDER BY total_sum DESC;
```

Три таблицы — «что и сколько купил пользователь»:

```sql
SELECT u.username, oi.name, SUM(oi.quantity) AS qty
FROM users u
JOIN orders o ON o.user_id = u.id
JOIN order_items oi ON oi.order_id = o.id
GROUP BY u.username, oi.name;
```

## Подзапросы

Запрос внутри запроса:

```sql
-- товары дороже среднего
SELECT name, price FROM products
WHERE price > (SELECT AVG(price) FROM products);

-- пользователи, у которых есть оплаченные заказы
SELECT username FROM users
WHERE id IN (SELECT user_id FROM orders WHERE status = 'PAID');
```

## Параметры запроса

В тестах значения в запрос **не склеивают строками**, а передают параметрами `?` (урок 11.6):

```sql
SELECT name, quantity FROM order_items WHERE order_id = ? ORDER BY id;
```

Это задание `ITEMS_OF_ORDER`: проверка подставит id созданного заказа.

## Полезно для проверок в тестах

```sql
-- нет ли «осиротевших» позиций заказа без заказа
SELECT COUNT(*) FROM order_items oi LEFT JOIN orders o ON o.id = oi.order_id WHERE o.id IS NULL;

-- совпадает ли сумма заказа с суммой позиций (с учётом скидки)
SELECT o.id, o.subtotal, SUM(oi.price * oi.quantity) AS items_sum
FROM orders o JOIN order_items oi ON oi.order_id = o.id
GROUP BY o.id, o.subtotal
HAVING o.subtotal <> SUM(oi.price * oi.quantity);

-- дубликаты email (BUG-201!)
SELECT LOWER(email), COUNT(*) FROM users GROUP BY LOWER(email) HAVING COUNT(*) > 1;
```

Такие запросы — **проверки целостности данных**: их иногда запускают после прогона всех тестов.

```task
id: m11-t03
title: JOIN и группировка
module: exercises/m11-databases
test: JoinQueriesCheck
file: exercises/m11-databases/src/main/java/ru/qacourse/m11/Queries.java
solution: solutions/exercises/m11-databases/src/main/java/ru/qacourse/m11/Queries.java
text: |
  Раздел «11.3» в `Queries.java`: `COUNT_BY_CATEGORY`, `AVG_RATING`, `REVENUE_BY_USER`, `ITEMS_OF_ORDER` (с параметром `?`).
  Проверка сама создаст заказы через API перед выполнением ваших запросов.
hint: |
  Все четыре запроса почти дословно разобраны в уроке. Столбцы — ровно в том порядке, что указан в комментарии.
```

```quiz
- q: Чем WHERE отличается от HAVING?
  options:
    - "WHERE фильтрует строки до группировки, HAVING — группы после"
    - "Ничем"
    - "HAVING работает только с JOIN"
  answer: 0
- q: Какой JOIN вернёт и товары без отзывов?
  options: ["LEFT JOIN products → reviews", "INNER JOIN", "Никакой"]
  answer: 0
- q: Как найти товары без отзывов?
  options: ["LEFT JOIN reviews ... WHERE r.id IS NULL", "INNER JOIN reviews", "WHERE rating = 0"]
  answer: 0
- q: Что может быть в SELECT при GROUP BY category?
  options: ["category и агрегатные функции", "Любые столбцы", "Только *"]
  answer: 0
- q: Зачем в AVG(rating * 1.0) умножение на 1.0?
  options: ["Чтобы среднее посчиталось дробным, а не округлилось до целого", "Для красоты", "Это ошибка"]
  answer: 0
```
