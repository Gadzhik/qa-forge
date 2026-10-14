---
title: "10.10 Selenide: переписываем тест"
minutes: 45
---

## Зачем Selenide

**Selenide** — библиотека поверх Selenium (автор — Андрей Солнцев, Codeborne). Её главная идея — **умные ожидания по умолчанию**: каждая проверка и каждое действие сами ждут нужного состояния (до 4 секунд по умолчанию). Плюс:
- браузер открывается и закрывается сам;
- лаконичный синтаксис `$()` и `$$()`;
- **скриншот и HTML страницы при падении** — автоматически;
- удобные коллекции, скачивание файлов, работа с фреймами и алертами;
- нет `StaleElementReferenceException` — элемент ищется заново при каждом обращении.

## Сравнение

**Selenium:**

```java
WebDriver driver = Browser.chrome();
driver.get(baseUrl + "/login");
driver.findElement(By.cssSelector("[data-testid=login-username]")).sendKeys("user");
driver.findElement(By.cssSelector("[data-testid=login-password]")).sendKeys("user123");
driver.findElement(By.cssSelector("[data-testid=login-submit]")).click();
new WebDriverWait(driver, Duration.ofSeconds(5))
        .until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector("[data-testid=user-name]"), "user"));
driver.quit();
```

**Selenide:**

```java
open("/login");
$("[data-testid=login-username]").setValue("user");
$("[data-testid=login-password]").setValue("user123");
$("[data-testid=login-submit]").click();
$("[data-testid=user-name]").shouldHave(text("user"));
```

## Импорты

```java
import static com.codeborne.selenide.Selenide.*;            // open, $, $$, closeWebDriver, refresh...
import static com.codeborne.selenide.Condition.*;           // visible, text, exactText, value, attribute...
import static com.codeborne.selenide.CollectionCondition.*; // size, texts, itemWithText...
```

## Основные операции

| Selenide | Что делает |
|---|---|
| `Configuration.baseUrl = "..."` | базовый адрес для `open("/путь")` |
| `open("/login")` | открыть страницу (браузер запустится сам при первом вызове) |
| `$("css")` / `$x("xpath")` / `$(By.name("q"))` | **один** элемент (`SelenideElement`) |
| `$$("css")` | **коллекция** элементов (`ElementsCollection`) |
| `.setValue("текст")` | очистить поле и ввести |
| `.append("текст")` | дописать |
| `.click()`, `.doubleClick()`, `.contextClick()`, `.hover()` | действия мышью |
| `.pressEnter()`, `.pressTab()`, `.press(Keys...)` | клавиши |
| `.selectOption("Напитки")` / `.selectOptionByValue("DRINK")` | выбрать в `<select>` |
| `.getText()`, `.getValue()`, `.getAttribute("x")` | прочитать |
| `.$("css")` | искать **внутри** элемента |
| `refresh()`, `back()`, `title()`, `webdriver().driver().url()` | навигация |
| `closeWebDriver()` | закрыть браузер |

> 💡 `$` не ищет элемент в момент вызова — он создаёт «прокси», который ищет элемент **при каждом действии**. Поэтому элемент можно сохранить в поле класса страницы (`private final SelenideElement total = $(...)`) — stale не будет.

## Проверки: should

```java
$("[data-testid=toast]").shouldBe(visible).shouldHave(text("Добавлено"));
$("[data-testid=cart-count]").shouldHave(exactText("3"));
$("[data-testid=pay-button]").shouldBe(disabled);
$("[data-testid=login-username]").shouldHave(value("user"));
$("[data-testid=flash-error]").shouldNotBe(visible);
```

- **`shouldBe` / `shouldHave` / `shouldNot...`** — проверка с **ожиданием**: Selenide повторяет её до 4 секунд (`Configuration.timeout`), пока условие не выполнится;
- `shouldBe` и `shouldHave` — синонимы, выбирайте тот, что читается по-русски/английски лучше («should be visible», «should have text»).

Частые условия (`Condition`):

| Условие | Смысл |
|---|---|
| `visible` / `hidden` / `exist` | видим / скрыт или нет / есть в DOM |
| `text("...")` | содержит текст (без учёта регистра!) |
| `exactText("...")` | текст точно равен |
| `value("...")` | значение поля |
| `attribute("name", "value")` | атрибут |
| `cssClass("active")` | есть класс |
| `enabled` / `disabled` | активен / нет |
| `checked`, `selected`, `focused` | чекбокс, опция, фокус |
| `empty` | пустой |

> ⚠️ `text("кола")` найдёт и «Кола 0.5» — проверка **регистронезависимая и на вхождение**. Для точного сравнения — `exactText`.

## Тест из задания

```java
@BeforeEach
void setUp() {
    Configuration.baseUrl = ShopConfig.baseUrl();
    open("/login");
    $("[data-testid=login-username]").setValue("user");
    $("[data-testid=login-password]").setValue("user123");
    $("[data-testid=login-submit]").click();
    $("[data-testid=user-name]").shouldBe(visible);
}

@AfterEach
void tearDown() {
    closeWebDriver();
}

@Test
void addFromProductPage() {
    open("/product/1");
    $("[data-testid=quantity-input]").setValue("3");
    $("[data-testid=add-to-cart]").click();
    $("[data-testid=toast]").shouldBe(visible).shouldHave(text("Добавлено"));

    refresh();
    $("[data-testid=cart-count]").shouldHave(exactText("3"));

    open("/cart");
    $$("[data-testid=cart-row]").shouldHave(size(1));
    $("[data-testid=cart-item-quantity]").shouldHave(exactText("3"));
}
```

- `Configuration.baseUrl` ставим **в `@BeforeEach`**, а не один раз в статике: адрес приложения при проверке меняется (урок 9.5);
- вход через форму в подготовке (в уроке 10.14 заменим на быстрый вход через API);
- `$("[data-testid=user-name]").shouldBe(visible)` — **дождаться**, что вход завершился, прежде чем идти дальше;
- после клика «В корзину» на странице товара JS отправляет запрос и показывает уведомление — `shouldBe(visible)` ждёт его;
- `refresh()` — перезагрузить страницу, чтобы проверить, что сервер действительно сохранил корзину;
- проверка корзины — и через счётчик в шапке, и в самой корзине: BUG-703 (кнопка «врёт») и BUG-701 (счётчик считает не то) ловятся разными проверками.

## Настройки Configuration

```java
Configuration.browser = "chrome";        // firefox, edge, safari
Configuration.browserSize = "1280x900";
Configuration.headless = true;           // или -Dselenide.headless=true
Configuration.timeout = 6000;            // ожидание в мс (по умолчанию 4000)
Configuration.pageLoadTimeout = 30000;
Configuration.reportsFolder = "build/reports/tests";   // куда класть скриншоты при падении
Configuration.remote = "http://localhost:4444/wd/hub"; // Selenium Grid (урок 15.4)
```

Любую настройку можно передать системным свойством `-Dselenide.имя=значение` — так делают в CI.

## Скриншоты при падении

Если проверка упала, Selenide сам сохраняет **скриншот** и **HTML страницы** в `build/reports/tests` и печатает пути в сообщении об ошибке:

```text
Element should have exact text "3" {[data-testid=cart-count]}
Element: '<span class="cart-count" data-testid="cart-count">1</span>'
Actual value: exact text="1"
Screenshot: file:/.../build/reports/tests/1715515275.png
Page source: file:/.../build/reports/tests/1715515275.html
Timeout: 4 s.
```

Сообщение уже содержит: какое условие, какой элемент, какое реальное значение, сколько ждали, и где скриншот. Сравните с голым Selenium!

```task
id: m10-t10
title: Первый тест на Selenide
module: exercises/m10-ui
test: SelenideCartTestCheck
file: exercises/m10-ui/src/test/java/ru/qacourse/m10/tasks/SelenideCartTest.java
solution: solutions/exercises/m10-ui/src/test/java/ru/qacourse/m10/tasks/SelenideCartTest.java
text: |
  Вход через форму в `@BeforeEach`, затем: на странице товара 1 добавить 3 шт., дождаться уведомления, обновить страницу,
  проверить счётчик «3» и корзину (одна строка, количество 3).

  Проверка ловит BUG-703 и BUG-701.
hint: |
  Код разобран в уроке. Не забудьте `closeWebDriver()` в `@AfterEach`.
```

```quiz
- q: Главное преимущество Selenide перед Selenium?
  options: ["Встроенные умные ожидания в каждом действии и проверке", "Он работает без браузера", "Он бесплатный"]
  answer: 0
- q: Чем text("кола") отличается от exactText("кола")?
  options:
    - "text — содержит, без учёта регистра; exactText — точное равенство"
    - "Ничем"
    - "exactText ищет по value"
  answer: 0
- q: Сколько по умолчанию ждёт should-проверка Selenide?
  options: ["4 секунды", "0 секунд", "1 минуту"]
  answer: 0
- q: Почему у Selenide не бывает StaleElementReferenceException?
  options: ["$() ищет элемент заново при каждом обращении", "Selenide не использует DOM", "Это неправда"]
  answer: 0
- q: Что Selenide сохраняет при падении проверки?
  options: ["Скриншот и HTML страницы", "Видео", "Ничего"]
  answer: 0
```
