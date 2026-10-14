---
title: "10.6 Alert, iframe, вкладки и Basic Auth"
minutes: 40
---

Четыре вещи, на которых «спотыкаются» UI-тесты, потому что они живут **вне** обычного DOM страницы.

## Alert, Confirm, Prompt

Браузерные диалоги (`alert('...')`, `confirm(...)`, `prompt(...)`) — не HTML. Пока диалог открыт, страница заблокирована, и любое действие с ней бросит `UnhandledAlertException`.

```java
driver.findElement(By.cssSelector("[data-testid=confirm-btn]")).click();
Alert confirm = wait.until(ExpectedConditions.alertIsPresent());
assertThat(confirm.getText()).isEqualTo("Удалить заказ?");
confirm.dismiss();
```

- `wait.until(alertIsPresent())` — дождаться диалога и получить объект `Alert` (диалог может появиться не сразу — в Песочнице есть кнопка «Alert через 2 с»);
- `getText()` — текст диалога;
- **`accept()`** — нажать «OK»;
- **`dismiss()`** — нажать «Отмена»;
- **`sendKeys("Анна")`** — ввести текст в `prompt`, затем `accept()`.

Без ожидания: `driver.switchTo().alert()` — переключиться на уже открытый диалог.

## iframe

**iframe** — встроенная **отдельная страница** внутри страницы: виджет оплаты, карта, чат поддержки, капча. Элементы внутри iframe **не видны** для `findElement`, пока вы не «переключитесь» в него.

```java
driver.switchTo().frame(driver.findElement(By.cssSelector("[data-testid=inner-frame]")));
driver.findElement(By.cssSelector("[data-testid=inner-input]")).sendKeys("бургер");
driver.findElement(By.cssSelector("[data-testid=inner-btn]")).click();
assertThat(driver.findElement(By.cssSelector("[data-testid=inner-result]")).getText()).isEqualTo("Вы ввели: бургер");

driver.switchTo().defaultContent();
assertThat(driver.findElement(By.cssSelector("[data-testid=outer-text]")).getText()).isEqualTo("Я снаружи");
```

- `switchTo().frame(элемент)` — перейти внутрь фрейма (также можно по имени/id: `frame("inner")` или номеру `frame(0)`);
- после этого все поиски идут **внутри** фрейма;
- `switchTo().defaultContent()` — вернуться на основную страницу; `parentFrame()` — на уровень вверх (для вложенных фреймов).

> ⚠️ Если `NoSuchElementException`, а в DevTools элемент виден — проверьте, нет ли выше по дереву тега `<iframe>`. Это причина №1.

**Отладка в DevTools:** в Console есть выпадающий список «top» — выберите в нём фрейм, чтобы выполнять `document.querySelector` внутри него.

## Вкладки и окна

Ссылка с `target="_blank"` или `window.open(...)` открывают новую вкладку, но Selenium **остаётся в старой**.

```java
String original = driver.getWindowHandle();
driver.findElement(By.cssSelector("[data-testid=new-tab-link]")).click();
wait.until(ExpectedConditions.numberOfWindowsToBe(2));

for (String handle : driver.getWindowHandles()) {
    if (!handle.equals(original)) {
        driver.switchTo().window(handle);
    }
}
assertThat(driver.findElement(By.tagName("h1")).getText()).isEqualTo("Формы");

driver.close();                          // закрыть новую вкладку
driver.switchTo().window(original);      // вернуться
```

- **handle** — строковый идентификатор вкладки;
- `getWindowHandle()` — текущая, `getWindowHandles()` — все (Set);
- перебираем и переключаемся на «не исходную».

Открыть новую вкладку самому: `driver.switchTo().newWindow(WindowType.TAB)`.

## HTTP Basic Auth

Страница за Basic Auth (урок 8.5) показывает **браузерное** окно логина — его нельзя заполнить через `findElement` (это не HTML и не alert). Варианты:

**1. Логин и пароль в адресе:**

```java
String url = ShopConfig.baseUrl().replace("http://", "http://admin:admin@") + "/playground/basic-auth";
driver.get(url);
```

Адрес вида `http://логин:пароль@хост/...`. Работает в Chrome для открытия страницы (браузеры ограничивают такие адреса для вложенных ресурсов).

**2. Через CDP (Chrome DevTools Protocol)** — Selenium 4 для Chromium-браузеров:

```java
((HasAuthentication) driver).register(UsernameAndPassword.of("admin", "admin"));
driver.get(ShopConfig.baseUrl() + "/playground/basic-auth");
```

**3. Selenide**: `open("/playground/basic-auth", "", "admin", "admin")`.

## Cookies

Пригодятся в уроке 10.14:

```java
driver.manage().addCookie(new Cookie("token", token));
driver.manage().getCookieNamed("token");
driver.manage().deleteAllCookies();
```

Cookie можно добавить только для **открытого** домена — поэтому сначала `driver.get(...)` любую страницу сайта.

```task
id: m10-t06
title: Alert, iframe, вкладки, Basic Auth
module: exercises/m10-ui
test: AlertsFramesTestCheck
file: exercises/m10-ui/src/test/java/ru/qacourse/m10/tasks/AlertsFramesTest.java
solution: solutions/exercises/m10-ui/src/test/java/ru/qacourse/m10/tasks/AlertsFramesTest.java
text: |
  Четыре теста по Песочнице: confirm + prompt, iframe, новая вкладка, Basic Auth (admin/admin). Точные шаги — в комментарии к классу.
hint: |
  Все конструкции — в уроке. Для confirm результат «Отменено» появляется после `dismiss()`, для prompt — «Привет, Анна!» после `sendKeys` + `accept()`.
```

```quiz
- q: Как нажать «Отмена» в confirm?
  options: ["alert.dismiss()", "alert.accept()", "alert.close()"]
  answer: 0
- q: Элемент виден в DevTools, но NoSuchElementException. Что проверить первым?
  options: ["Не внутри ли он iframe", "Цвет элемента", "Версию Java"]
  answer: 0
- q: Как вернуться из iframe на основную страницу?
  options: ["driver.switchTo().defaultContent()", "driver.navigate().back()", "driver.quit()"]
  answer: 0
- q: После клика открылась новая вкладка. Где «находится» Selenium?
  options: ["В старой вкладке, нужно переключиться через switchTo().window(...)", "В новой", "Нигде"]
  answer: 0
- q: Почему окно Basic Auth нельзя заполнить через findElement?
  options: ["Это окно браузера, а не HTML страницы", "Оно в iframe", "Оно скрыто"]
  answer: 0
```
