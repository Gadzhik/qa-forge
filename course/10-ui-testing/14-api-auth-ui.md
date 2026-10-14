---
title: "10.14 Авторизация в UI через API"
minutes: 35
---

## Проблема

Почти каждый UI-тест начинается со входа: открыть страницу логина, ввести имя и пароль, нажать кнопку, дождаться. Это 3–5 секунд и 5 мест, где тест может упасть **не по своей теме**. Тест корзины не должен падать из-за того, что поменялась форма входа.

А часто нужны и **данные**: товары в корзине, созданный заказ. Накликивать их через интерфейс — ещё медленнее.

## Решение: готовим через API, проверяем через UI

1. Получить токен через API (~50 мс).
2. Подготовить данные через API (положить товары в корзину).
3. **Подложить токен в cookie браузера** — сайт будет считать, что пользователь вошёл.
4. Открыть страницу и проверить то, что видит пользователь.

QA Burger узнаёт пользователя по cookie `token` (урок 8.5) — этим и воспользуемся.

```java
@Test
void loggedInViaCookie() {
    String token = given().baseUri(ShopConfig.baseUrl()).contentType(ContentType.JSON)
            .body(Map.of("username", "user", "password", "user123"))
            .post("/api/auth/login").then().statusCode(200).extract().path("token");

    given().baseUri(ShopConfig.baseUrl()).contentType(ContentType.JSON).auth().oauth2(token)
            .body(Map.of("productId", 12, "quantity", 2))
            .post("/api/cart/items").then().statusCode(200);

    open("/playground");
    getWebDriver().manage().addCookie(new Cookie("token", token));
    open("/");

    $("[data-testid=user-name]").shouldHave(exactText("user"));
    $("[data-testid=cart-count]").shouldHave(exactText("2"));
}
```

Разбор:
- первые два запроса — REST Assured (модуль 9): токен и корзина;
- **`open("/playground")`** — сначала нужно открыть **любую страницу этого сайта**: браузер разрешает ставить cookie только для текущего домена. Берите самую лёгкую страницу (в реальных проектах — часто страницу 404 или `favicon.ico`);
- `getWebDriver()` — получить объект Selenium из Selenide (`import static com.codeborne.selenide.WebDriverRunner.getWebDriver`);
- **`manage().addCookie(new Cookie("token", token))`** — положить cookie в браузер;
- **`open("/")`** — открыть главную уже «залогиненным»: браузер отправит cookie, сервер узнает пользователя.

## В виде расширения

Как в уроке 9.11, это удобно спрятать в аннотацию:

```java
@Test
@LoggedInAs(user = "user", password = "user123")
void cartCounter() {
    open("/");
    ...
}
```

`BeforeEachCallback`, читающий аннотацию с тестового метода: `context.getRequiredTestMethod().getAnnotation(LoggedInAs.class)`, получает токен и кладёт cookie.

## Если сайт хранит авторизацию иначе

- **Сессия на сервере** (cookie `SESSION`/`JSESSIONID`): выполните вход HTTP-клиентом, возьмите cookie сессии из ответа (`response.getCookie("JSESSIONID")`) и положите в браузер.
- **Токен в localStorage** (частый случай у SPA на React/Vue/Angular): откройте страницу и положите токен JavaScript'ом: `executeJavaScript("localStorage.setItem('token', arguments[0])", token); refresh();`.
- **OAuth-вход через внешний сервис**: просите разработчиков тестовую ручку выдачи токена или используйте тестовые клиенты (урок 8.5).

## Когда всё же входить через форму

Сама форма входа должна быть покрыта UI-тестами: позитивный вход, неверный пароль, пустые поля, «запомнить меня». Но это **отдельные** тесты формы входа. Остальные тесты — через API.

```task
id: m10-t14
title: Вход в UI через API
module: exercises/m10-ui
test: ApiLoginUiTestCheck
file: exercises/m10-ui/src/test/java/ru/qacourse/m10/tasks/ApiLoginUiTest.java
solution: solutions/exercises/m10-ui/src/test/java/ru/qacourse/m10/tasks/ApiLoginUiTest.java
text: |
  Получите токен через API, положите 2 шт. товара 12 в корзину через API, подложите cookie `token` в браузер
  и проверьте шапку: имя «user» и счётчик «2».

  Проверка ловит BUG-702.
hint: |
  Перед `addCookie` откройте любую страницу сайта (`open("/playground")`), после — `open("/")`.
```

```quiz
- q: Зачем авторизоваться в UI-тесте через API?
  options:
    - "Быстрее и стабильнее: тест не зависит от формы входа"
    - "Так безопаснее"
    - "Иначе Selenium не работает"
  answer: 0
- q: Почему перед addCookie нужно открыть страницу сайта?
  options: ["Браузер ставит cookie только для текущего домена", "Иначе cookie удалится", "Так требует REST Assured"]
  answer: 0
- q: Сайт хранит токен в localStorage. Как подложить его?
  options: ["executeJavaScript(\"localStorage.setItem('token', ...)\") и refresh()", "addCookie", "Никак"]
  answer: 0
- q: Нужно ли тогда вообще тестировать форму входа?
  options: ["Да, отдельными тестами самой формы", "Нет", "Только вручную"]
  answer: 0
```
