---
title: "7.6 Расширения JUnit 5 и своя аннотация-параметр"
minutes: 45
---

В каждом API-тесте нужен новый пользователь: сгенерировать уникальное имя, зарегистрировать, получить токен. Можно копировать это в `@BeforeEach` каждого класса... а можно один раз написать **расширение**, и тест будет просто **получать пользователя параметром**:

```java
@ExtendWith(RandomCustomerResolver.class)
class CartApiTest {

    @Test
    void addToCart(TestCustomer customer) {     // ← JUnit сам подставит покупателя
        ...
    }
}
```

## Что такое расширение

**Расширение** (extension) — класс, который «вклинивается» в жизненный цикл JUnit: до и после тестов, при подстановке параметров, при падении. Почти все библиотеки интегрируются с JUnit через расширения: Selenide (скриншот при падении), Allure (отчёт), Spring, Mockito, Testcontainers.

Расширение реализует один или несколько интерфейсов:

| Интерфейс | Когда вызывается |
|---|---|
| `BeforeAllCallback`, `BeforeEachCallback` | до всех / каждого теста |
| `AfterEachCallback`, `AfterAllCallback` | после |
| `ParameterResolver` | когда тестовому методу нужен параметр |
| `TestWatcher` | после теста: прошёл, упал, пропущен (урок 7.7) |
| `TestExecutionExceptionHandler` | когда тест бросил исключение |
| `ExecutionCondition` | решить, запускать ли тест вообще |

Подключение:
- `@ExtendWith(МоёРасширение.class)` на классе или методе;
- или через свою аннотацию (ниже);
- или глобально — файлом `META-INF/services/org.junit.jupiter.api.extension.Extension` и свойством `junit.jupiter.extensions.autodetection.enabled=true`.

## ParameterResolver — подставляем параметры

```java
public class RandomCustomerResolver implements ParameterResolver {

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return parameterContext.getParameter().getType() == TestCustomer.class;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        String username = "user_" + UUID.randomUUID().toString().substring(0, 8);
        return new TestCustomer(username, username + "@test.local", "Secret1");
    }
}
```

1. **`supportsParameter`** — JUnit спрашивает: «умеешь подставить этот параметр?». `parameterContext.getParameter()` — описание параметра метода (`java.lang.reflect.Parameter`): тип, имя, аннотации. Отвечаем `true` для `TestCustomer`.
2. **`resolveParameter`** — «подставь»: создаём и возвращаем значение. Здесь можно сделать что угодно: зарегистрировать пользователя через API, получить токен, открыть страницу.

`UUID.randomUUID()` даёт уникальную строку, `substring(0, 8)` — её первые 8 символов: `user_3f2b6c1e`.

### Своя аннотация — красивее

```java
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface NewUser {
    String role() default "USER";
}
```

```java
@Override
public boolean supportsParameter(ParameterContext pc, ExtensionContext ec) {
    return pc.isAnnotated(NewUser.class);
}

@Override
public Object resolveParameter(ParameterContext pc, ExtensionContext ec) {
    NewUser settings = pc.findAnnotation(NewUser.class).orElseThrow();
    return registerViaApi(settings.role());     // создаём пользователя нужной роли
}
```

Тест:

```java
@Test
void adminCanDeleteProduct(@NewUser(role = "ADMIN") TestCustomer admin) { ... }
```

- у аннотации могут быть **параметры** с значением по умолчанию (`role() default "USER"`);
- `pc.findAnnotation(...)` достаёт аннотацию с параметра и её настройки.

А чтобы не писать `@ExtendWith` в каждом классе, расширение вешают на саму аннотацию: `@ExtendWith(UserResolver.class)` над `@interface NewUser` — тогда достаточно поставить `@NewUser` на параметр.

## BeforeEachCallback — общая подготовка

```java
public class ResetShopExtension implements BeforeEachCallback {
    @Override
    public void beforeEach(ExtensionContext context) {
        given().post("/api/admin/reset");     // сбросить данные QA Burger перед каждым тестом
    }
}
```

`ExtensionContext` даёт доступ к информации о тесте: `getDisplayName()`, `getRequiredTestMethod()`, `getTags()`, а через `getStore(...)` — хранилище, чтобы передавать данные между этапами (например, запомнить время старта в `before` и посчитать длительность в `after`).

## Зачем это автоматизатору

Расширения — то, что отличает «набор тестов» от **фреймворка**. С ними тест содержит только суть проверки, а вся рутина спрятана:

```java
@Test
@DisplayName("Пользователь может оформить заказ с промокодом")
void orderWithPromo(@NewUser TestCustomer user) {
    cart(user).add(BURGER, 2);
    Order order = orders(user).create("SALE10");
    assertThat(order.total()).isEqualByComparingTo("521.82");
}
```

Мы построим такой фреймворк в модуле 14.

```task
id: m07-t06
title: ParameterResolver
module: exercises/m07-junit
test: RandomCustomerResolverCheck
file: exercises/m07-junit/src/main/java/ru/qacourse/m07/ext/RandomCustomerResolver.java
solution: solutions/exercises/m07-junit/src/main/java/ru/qacourse/m07/ext/RandomCustomerResolver.java
text: |
  Реализуйте `RandomCustomerResolver`: он должен подставлять параметр типа `TestCustomer` —
  каждый раз нового, с username `user_` + 8 случайных символов, email `username@test.local`, паролем `Secret1`.

  Проверка запустит тест-образец `checks/ResolverSample.java`, который использует ваше расширение.
hint: |
  `supportsParameter`: `return parameterContext.getParameter().getType() == TestCustomer.class;`
  `resolveParameter`: `UUID.randomUUID().toString().substring(0, 8)` даёт 8 символов из `0-9a-f`.
```

```quiz
- q: Какой интерфейс нужен, чтобы подставлять параметры в тестовые методы?
  options: [ParameterResolver, TestWatcher, BeforeEachCallback]
  answer: 0
- q: Как подключить расширение к тест-классу?
  options: ["@ExtendWith(Расширение.class)", "@Extension", "@Use(Расширение.class)"]
  answer: 0
- q: Что возвращает supportsParameter?
  options: ["true, если расширение умеет подставить этот параметр", "Значение параметра", "Имя параметра"]
  answer: 0
- q: Зачем нужны расширения во фреймворке автотестов?
  options:
    - Спрятать повторяющуюся подготовку и инфраструктуру, оставив в тесте только суть проверки
    - Чтобы тесты запускались без JUnit
    - Чтобы отключать тесты
  answer: 0
```
