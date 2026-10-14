---
title: "7.2 Аннотации и жизненный цикл тестов"
minutes: 40
---

## Подготовка и уборка

В каждом тесте корзины первая строка — `Cart cart = new Cart();`. Повторение. А в API-тестах подготовка тяжелее: зарегистрировать пользователя, получить токен, очистить корзину. Для этого есть аннотации жизненного цикла.

```java
class CartLifecycleTest {

    private Cart cart;

    @BeforeAll
    static void beforeAll() {
        System.out.println("1 раз перед всеми тестами класса");
    }

    @BeforeEach
    void setUp() {
        cart = new Cart();                 // перед КАЖДЫМ тестом — свежая корзина
        cart.add("Бургер", "200.00", 1);
    }

    @Test
    void promo() { ... }

    @Test
    void removeLast() { ... }

    @AfterEach
    void tearDown() {
        System.out.println("после каждого теста");
    }

    @AfterAll
    static void afterAll() {
        System.out.println("1 раз после всех тестов");
    }
}
```

| Аннотация | Когда выполняется | Типичное применение |
|---|---|---|
| `@BeforeAll` | один раз до всех тестов класса (метод **static**) | настроить адрес API, поднять сервер, создать общего пользователя |
| `@BeforeEach` | перед каждым тестом | создать объект, открыть браузер, войти |
| `@AfterEach` | после каждого теста (даже упавшего) | закрыть браузер, удалить созданные данные |
| `@AfterAll` | один раз после всех (static) | остановить сервер |

Порядок для класса с двумя тестами:

```text
@BeforeAll
  @BeforeEach → тест 1 → @AfterEach
  @BeforeEach → тест 2 → @AfterEach
@AfterAll
```

**Почему `@BeforeAll` статический?** JUnit по умолчанию создаёт **новый объект тест-класса для каждого теста** (так тесты не делят состояние полей). `@BeforeAll` выполняется до создания объектов — поэтому он `static`. (Это меняется аннотацией `@TestInstance(Lifecycle.PER_CLASS)`, но без нужды так не делают.)

> 💡 Новый объект на каждый тест — причина, по которой поле `cart` безопасно: у каждого теста своя корзина, даже если тесты идут в любом порядке.

## Другие полезные аннотации

```java
@DisplayName("Корзина")
class CartLifecycleTest {

    @Test
    @DisplayName("SALE10 даёт ровно 10% скидки")
    void promo() { ... }

    @Test
    @Disabled("BUG-401: двойная скидка, ждём исправления")
    void promoForTwoItems() { ... }

    @Test
    @Timeout(5)
    void fast() { ... }

    @RepeatedTest(10)
    void stable() { ... }
}
```

- **`@DisplayName`** — человекочитаемое название в отчётах и IDE. Можно по-русски, с пробелами и эмодзи. Отчёт для менеджера выглядит как список требований.
- **`@Disabled("причина")`** — временно отключить тест. Всегда пишите причину (номер бага), иначе через полгода никто не вспомнит, почему он выключен.
- **`@Timeout(5)`** — упасть, если тест идёт дольше 5 секунд.
- **`@RepeatedTest(10)`** — повторить 10 раз; помогает ловить нестабильность.
- **`@TestMethodOrder(MethodOrderer.OrderAnnotation.class)`** + **`@Order(1)`** — задать порядок. Используйте осторожно: зависимость тестов от порядка — плохая практика.

## @Nested — группировка

Вложенные классы помогают структурировать тесты по сценариям:

```java
@DisplayName("Корзина")
class CartLifecycleTest {

    private Cart cart;

    @BeforeEach
    void setUp() {
        cart = new Cart();
        cart.add("Бургер", "200.00", 1);
    }

    @Nested
    @DisplayName("Удаление")
    class Removing {

        @Test
        @DisplayName("После удаления единственного товара корзина пуста")
        void removeLast() {
            cart.remove("Бургер");
            assertTrue(cart.isEmpty());
        }
    }
}
```

- `@Nested` ставится на **внутренний** (не `static`) класс;
- у вложенного класса есть доступ к полям внешнего, а `@BeforeEach` внешнего класса выполняется и для его тестов;
- в отчёте получается дерево: «Корзина → Удаление → После удаления единственного товара корзина пуста».

## Как называть тесты

Популярные стили:
- `methodName_condition_expectedResult`: `add_zeroQuantity_throwsException`;
- `shouldDoSomething`: `shouldRejectZeroQuantity`;
- предложение: `zeroQuantityIsRejected` + `@DisplayName("Количество 0 отклоняется")`.

Главное — **одинаково во всём проекте**. Название должно объяснять, что сломалось, если тест упал, — без чтения кода.

```task
id: m07-t02
title: Жизненный цикл, @Nested, @DisplayName
module: exercises/m07-junit
test: CartLifecycleTestCheck
file: exercises/m07-junit/src/test/java/ru/qacourse/m07/tasks/CartLifecycleTest.java
solution: solutions/exercises/m07-junit/src/test/java/ru/qacourse/m07/tasks/CartLifecycleTest.java
text: |
  Напишите `CartLifecycleTest` с нуля:
  - поле `cart`, заполняемое в методе с `@BeforeEach`;
  - вложенный класс `@Nested` с тестами удаления (`remove`);
  - тест скидки `SALE10`;
  - понятные `@DisplayName`.

  Проверка ловит ошибки «remove ничего не удаляет» и «скидка применяется дважды (19%)».
hint: |
  Для удаления проверьте `cart.isEmpty()` после удаления единственного товара. Для скидки: товар за 200.00 → `total()` = 180.00.
```

```quiz
- q: Сколько раз выполнится @BeforeEach в классе с 5 тестами?
  options: ["1", "5", "0"]
  answer: 1
- q: Почему метод @BeforeAll должен быть static?
  options:
    - Он выполняется до создания объектов тест-класса
    - Так быстрее
    - Это не обязательно
  answer: 0
- q: Выполнится ли @AfterEach, если тест упал?
  options: ["Да", "Нет"]
  answer: 0
- q: Что обязательно указать в @Disabled?
  options: ["Причину (например, номер бага)", "Ничего", "Имя автора"]
  answer: 0
- q: Для чего нужен @Nested?
  options: [Группировать тесты по сценариям во вложенных классах, Запускать тесты параллельно, Отключать тесты]
  answer: 0
```
