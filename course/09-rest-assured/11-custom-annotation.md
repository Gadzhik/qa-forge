---
title: "9.11 Своя аннотация: пользователь как параметр теста"
minutes: 40
---

В уроке 7.6 мы сделали расширение, подставляющее объект в параметр теста. Теперь применим это к API: тест просто объявляет «мне нужен новый пользователь», а регистрация и получение токена происходят за кадром.

```java
@Test
void userIsRegisteredAndLoggedIn(@NewUser ApiUser user) {
    given(Specs.base()).header("Authorization", "Bearer " + user.token())
            .get("/api/auth/me")
            .then().statusCode(200).body("username", equalTo(user.username()));
}
```

Каждый тест получает **своего свежего** пользователя — тесты не мешают друг другу, их можно запускать параллельно, на любом стенде, сколько угодно раз.

## Аннотация

```java
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(NewUserResolver.class)
public @interface NewUser {
}
```

- `@Target(PARAMETER)` — ставится на параметры методов;
- `@Retention(RUNTIME)` — видна JUnit во время выполнения;
- **`@ExtendWith(NewUserResolver.class)` на самой аннотации** — расширение подключится автоматически везде, где используется `@NewUser`. Не нужно писать `@ExtendWith` над каждым тест-классом.

## Модель результата

```java
public record ApiUser(long id, String username, String password, String token) {
}
```

## Расширение

```java
public class NewUserResolver implements ParameterResolver {

    @Override
    public boolean supportsParameter(ParameterContext pc, ExtensionContext ec) {
        return pc.isAnnotated(NewUser.class) && pc.getParameter().getType() == ApiUser.class;
    }

    @Override
    public Object resolveParameter(ParameterContext pc, ExtensionContext ec) {
        String username = "u" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        String password = "Secret1";
        RegisterRequest request = new RegisterRequest(username, username + "@test.local", password);
        int id = given(Specs.base()).body(request).post("/api/auth/register")
                .then().statusCode(201).extract().path("id");
        return new ApiUser(id, username, password, Specs.token(username, password));
    }
}
```

- `pc.isAnnotated(NewUser.class)` — у параметра есть наша аннотация;
- и тип параметра — `ApiUser` (защита от ошибки: `@NewUser String x` не сработает с непонятной ошибкой);
- `UUID...replace("-", "").substring(0, 10)` — 10 случайных шестнадцатеричных символов;
- регистрация через API → `id`; вход → токен;
- возвращаемый объект JUnit подставит в параметр.

Если регистрация не удалась (сервер упал), `statusCode(201)` бросит `AssertionError` — тест упадёт с понятной причиной ещё до своих проверок.

## Развитие идеи

Параметры аннотации делают её гибче:

```java
public @interface NewUser {
    String role() default "USER";       // @NewUser(role = "ADMIN")
    boolean withCart() default false;   // сразу положить товары в корзину
}
```

В `resolveParameter` настройки читаются так: `NewUser settings = pc.findAnnotation(NewUser.class).orElseThrow();`.

Похожим образом в настоящих фреймворках делают `@WithOrder`, `@WithProduct`, `@LoggedIn` (для UI-тестов: открыть браузер уже авторизованным — модуль 10).

## Итоги модуля

Вы научились:
- писать API-тесты на REST Assured: given/when/then, Hamcrest, GPath;
- работать с параметрами, телом, заголовками, авторизацией;
- использовать модели (Lombok, record) и JSON Schema;
- выносить общее в спецификации, обёртки, клиенты и расширения;
- работать с файлами, PDF, генерировать данные.

Теперь самое время вернуться в **Test Lab** — почти все API-сценарии там вам уже по силам. А следующий модуль — UI-тесты в браузере.

```task
id: m09-t11
title: Аннотация @NewUser
module: exercises/m09-rest-assured
test: NewUserResolverCheck
file: exercises/m09-rest-assured/src/main/java/ru/qacourse/m09/ext/NewUserResolver.java
solution: solutions/exercises/m09-rest-assured/src/main/java/ru/qacourse/m09/ext/NewUserResolver.java
text: |
  Реализуйте `NewUserResolver`: для параметра `@NewUser ApiUser` зарегистрировать нового пользователя через API
  (username `u` + 10 случайных символов, email `username@test.local`, пароль `Secret1`) и вернуть `ApiUser` с id и токеном.

  Проверка запустит образец `checks/NewUserSample.java`.
hint: |
  Код разобран в уроке. Для токена используйте `Specs.token(username, password)`.
```

```quiz
- q: Зачем ставить @ExtendWith на саму аннотацию @NewUser?
  options:
    - "Расширение подключится автоматически везде, где используется @NewUser"
    - "Иначе аннотация не скомпилируется"
    - "Для отчёта"
  answer: 0
- q: Какая главная польза @NewUser для набора тестов?
  options:
    - "Каждый тест получает своего пользователя — тесты независимы и годятся для параллельного запуска"
    - "Тесты становятся медленнее"
    - "Не нужен токен"
  answer: 0
- q: Как прочитать параметры аннотации внутри resolveParameter?
  options: ["pc.findAnnotation(NewUser.class).orElseThrow()", "System.getProperty", "Нельзя"]
  answer: 0
```
