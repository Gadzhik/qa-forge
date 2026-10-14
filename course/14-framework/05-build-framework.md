---
title: "14.5 Практика: тесты на своём фреймворке"
minutes: 60
---

Пора писать тесты так, как их пишут на работе: на фреймворке, с метками Allure, против реального приложения, с отчётом.

## Задача

В `exercises/m14-framework/src/test/java/ru/qacourse/fw/tests/PromoTest.java` напишите тесты промокодов, используя только слои фреймворка: `BaseTest` (вход и чистая корзина), `cart`, `orders`, модели, константы `Products`.

Требования (урок 1.4): SALE10 — 10%, BURGER20 — 20%, SUMMER2020 истёк → 400 `PROMO_EXPIRED`; скидка округляется до копеек.

### Подсчитаем ожидания заранее

Хороший тест знает ожидание **до** запуска, а не «смотрит, что вернул сервер»:

| Корзина | subtotal | Промокод | discount | total |
|---|---|---|---|---|
| 2 × Классический бургер (289.90) + 1 × Кола (99.00) | 678.80 | SALE10 | 67.88 | 610.92 |
| 1 × Картофель фри (129.00) | 129.00 | BURGER20 | 25.80 | 103.20 |

### Пример теста

```java
@Test
@Story("Скидка по промокоду")
@Issue("BUG-401")
@DisplayName("SALE10 даёт ровно 10%")
void sale10() {
    cart.add(CLASSIC_BURGER, 2).add(COLA, 1);
    OrderDto order = orders.create("SALE10");
    assertThat(order.subtotal()).isEqualByComparingTo("678.80");
    assertThat(order.discount()).isEqualByComparingTo("67.88");
    assertThat(order.total()).isEqualByComparingTo("610.92");
}
```

Негативный — через «сырой» метод клиента:

```java
@Test
@Story("Истёкший промокод")
@Issue("BUG-402")
@DisplayName("SUMMER2020 отклоняется: 400 PROMO_EXPIRED")
void expired() {
    cart.add(COLA, 1);
    orders.tryCreate("SUMMER2020").statusCode(400).body("code", equalTo("PROMO_EXPIRED"));
}
```

## Отчёт

```bash
./mvnw -f exercises/m14-framework/pom.xml test -Dtest='*Test'
./mvnw -f exercises/m14-framework/pom.xml allure:serve
```

Посмотрите в отчёте:
- **Behaviors** → Магазин → Промокоды → ваши истории;
- у теста — шаги «Войти как user», «Очистить корзину», «Добавить в корзину товар 1 × 2», «Оформить заказ с промокодом SALE10», и вложения запросов/ответов;
- включите в Тренажёре BUG-401, перезапустите тесты и посмотрите, как выглядит упавший тест: красный шаг, `expected / but was`, ссылка на BUG-401.

## Дальнейшее развитие (по желанию)

1. **Свой пользователь на тест** вместо общего `user` (урок 14.4) — `UserClient.registerRandom()` + `@NewUser` из урока 9.11.
2. **Собственные проверки**: `OrderAssert.assertThat(order).hasStatus("NEW").hasTotal("610.92")` (наследник `AbstractAssert` из AssertJ).
3. **UI-слой**: Page Object на Selenide (модуль 10) + `AllureSelenide` — и сквозной тест «заказ через сайт» с подготовкой через API.
4. **Слой БД**: клиент `ShopDb` (модуль 11) и проверка заказа в базе.
5. **Kafka**: проверка событий заказа (модуль 13).
6. Перенос в отдельный репозиторий на GitHub с README и CI (модуль 15) — это уже **проект для портфолио** и основа дипломного проекта (модуль 18).

```task
id: m14-t05
title: Тесты промокодов на фреймворке
module: exercises/m14-framework
test: PromoTestCheck
file: exercises/m14-framework/src/test/java/ru/qacourse/fw/tests/PromoTest.java
solution: solutions/exercises/m14-framework/src/test/java/ru/qacourse/fw/tests/PromoTest.java
text: |
  Три теста в `PromoTest`: SALE10 (10%, расчёт в таблице урока), SUMMER2020 (400 PROMO_EXPIRED через `tryCreate`), BURGER20 (20%).
  Используйте клиенты фреймворка (сначала доделайте `OrderClient` из задания 14.2) и метки Allure.

  Проверка ловит BUG-401 и BUG-402.
hint: |
  Код первых двух тестов — в уроке. Для BURGER20: картофель фри 129.00 → total 103.20.
```

```quiz
- q: Почему ожидаемую сумму считают заранее, а не берут из ответа сервера?
  options:
    - "Иначе тест проверяет сервер его же данными и не найдёт ошибку расчёта"
    - "Так короче"
    - "Сервер не возвращает сумму"
  answer: 0
- q: Как проверить негативный сценарий, если create() проверяет статус 201?
  options: ["Через orders.tryCreate(...) с проверкой 400", "Ловить исключение", "Никак"]
  answer: 0
- q: Какая вкладка Allure группирует тесты по эпикам и фичам?
  options: ["Behaviors", "Timeline", "Packages"]
  answer: 0
```
