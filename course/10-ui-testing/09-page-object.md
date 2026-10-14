---
title: "10.9 Паттерн Page Object"
minutes: 50
---

## Проблема

Напишем 50 UI-тестов, в каждом — `driver.findElement(By.cssSelector("[data-testid=login-username]"))`. Разработчики переименовали атрибут. Сколько мест править? 50. А ещё тесты нечитаемы: за локаторами не видно сценария.

## Идея Page Object

**Page Object** — самый известный паттерн UI-автоматизации:
- для каждой **страницы** (или крупного компонента) — свой **класс**;
- **локаторы** страницы живут только в этом классе;
- публичные **методы** класса — это **действия пользователя** на странице: `loginAs(...)`, `addToCart(...)`, `total()`;
- тесты общаются со страницами только через эти методы.

```java
@Test
void buyTwoBurgers() {
    CatalogPage catalog = new LoginPage(driver).open().loginAs("user", "user123");
    catalog.addToCart(1).addToCart(1);
    assertThat(catalog.cartCount()).isEqualTo("2");

    CartPage cart = catalog.openCart();
    assertThat(cart.total()).isEqualTo("579.80");
}
```

Тест читается как сценарий. Локаторы изменились — правим одну строку в классе страницы.

## Базовый класс

```java
public abstract class BasePage {
    protected final WebDriver driver;
    protected final WebDriverWait wait;

    protected BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(5));
    }

    protected By testId(String id) {
        return By.cssSelector("[data-testid=" + id + "]");
    }

    protected WebElement visible(String id) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(testId(id)));
    }

    protected void open(String path) {
        driver.get(ShopConfig.baseUrl() + path);
    }

    public String cartCount() {
        return visible("cart-count").getText();
    }

    public String userName() {
        return visible("user-name").getText();
    }
}
```

- `abstract` — сам по себе не страница, только общая часть (урок 4.5);
- `protected` поля и методы доступны наследникам;
- `visible(...)` — поиск **с ожиданием видимости**: все страницы получают устойчивые ожидания бесплатно;
- **шапка сайта** (`cartCount`, `userName`) есть на всех страницах — поэтому её методы в базовом классе. В больших проектах общие куски выносят в **компоненты** (`HeaderComponent`) и включают в страницы полем (композиция).

## Страница входа

```java
public class LoginPage extends BasePage {

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    public LoginPage open() {
        open("/login");
        return this;
    }

    public CatalogPage loginAs(String username, String password) {
        visible("login-username").sendKeys(username);
        visible("login-password").sendKeys(password);
        visible("login-submit").click();
        return new CatalogPage(driver);
    }
}
```

Важная идея — **методы возвращают следующую страницу**:
- `open()` возвращает `this` — остаёмся на странице входа;
- `loginAs(...)` после входа попадаем в каталог — возвращает `new CatalogPage(driver)`.

Так тест **«течёт»** по сайту: `new LoginPage(driver).open().loginAs(...).addToCart(1).openCart().total()`, а IDE подсказывает, что можно сделать на каждом шаге.

## Каталог: работа со списком

```java
public List<String> productNames() {
    visible("product-list");
    return driver.findElements(testId("product-name")).stream().map(WebElement::getText).toList();
}

public CatalogPage addToCart(long productId) {
    WebElement card = driver.findElement(By.cssSelector(
            "[data-testid=product-card][data-product-id='" + productId + "']"));
    card.findElement(testId("add-to-cart")).click();
    wait.until(ExpectedConditions.stalenessOf(card));
    visible("flash-message");
    return this;
}
```

- `stream().map(WebElement::getText).toList()` — тексты всех найденных элементов (урок 5.5);
- поиск **внутри** карточки нужного товара: `card.findElement(...)` — так мы нажимаем кнопку именно у этого товара;
- `wait.until(ExpectedConditions.stalenessOf(card))` — ждём, пока старая страница **исчезнет**: элемент `card` «протухает» (*stale*), когда браузер загружает новую страницу;
- `visible("flash-message")` — ожидание результата действия (новая страница с сообщением «Товар добавлен»).

> ⚠️ **Почему не хватает одного `visible("flash-message")`.** После первого `addToCart` сообщение уже есть на странице. При втором клике ожидание видит **старое** сообщение и сразу возвращает управление — ещё до перезагрузки. Тест читает счётчик корзины со старой страницы: «1» вместо «2». На быстром компьютере новая страница обычно успевает загрузиться, и тест зелёный; на медленном CI-сервере — красный. Это классический нестабильный тест (урок 16.2). Правило: если действие перезагружает страницу, сначала дождитесь, что старая ушла (`stalenessOf`), потом ждите новое.

> 💡 При подготовке этого задания тест не находил сообщение: при первом создании сессии сервер добавлял в адрес `;jsessionid=...`, и сообщение терялось. Это был настоящий баг приложения (исправлен настройкой `tracking-modes: cookie`), и UI-тест его поймал. Именно для таких находок и нужны end-to-end тесты.

## Правила хорошего Page Object

1. **Никаких проверок (assert) внутри страниц** — страница предоставляет данные (`total()`), тест решает, что с ними сравнивать. Исключение — проверки «я на нужной странице» (`shouldBeOpened()`).
2. **Методы — на языке пользователя**: `loginAs`, а не `fillUsernameFieldAndClickSubmitButton`.
3. **Локаторы не торчат наружу** (`private`/`protected`).
4. **Возвращайте страницы** для цепочек.
5. **Не плодите гигантские страницы** — выделяйте компоненты (шапка, карточка товара, модальное окно).
6. Не используйте `@FindBy` и `PageFactory` из старых статей бездумно: они создают «ленивые» элементы, которые легко становятся stale. Методы с поиском в момент вызова надёжнее.

## Page Object на Selenide

На Selenide (следующий урок) страницы получаются ещё короче — без driver и явных ожиданий:

```java
public class LoginPage {
    private final SelenideElement username = $("[data-testid=login-username]");
    private final SelenideElement password = $("[data-testid=login-password]");

    public CatalogPage loginAs(String user, String pass) {
        username.setValue(user);
        password.setValue(pass);
        $("[data-testid=login-submit]").click();
        return new CatalogPage();
    }
}
```

```task
id: m10-t09
title: Page Object для магазина
module: exercises/m10-ui
test: PageObjectCheck
file: exercises/m10-ui/src/main/java/ru/qacourse/m10/pages/LoginPage.java
solution: solutions/exercises/m10-ui/src/main/java/ru/qacourse/m10/pages/LoginPage.java
text: |
  Допишите Page Object в `src/main/java/ru/qacourse/m10/pages`: `LoginPage` (open, loginAs), `CatalogPage`
  (productNames, addToCart, openCart), `CartPage` (rowsCount, total). `BasePage` уже готов.

  Проверка запустит тест-образец `checks/PageObjectSample.java` на ваших страницах и убедится, что он ловит BUG-702.
hint: |
  Используйте помощники `BasePage`: `open(path)`, `visible(testId)`, `testId(id)`. Итог корзины: `visible("cart-total").getText().replace("₽", "").trim()`.
```

```quiz
- q: Что такое Page Object?
  options:
    - "Класс на каждую страницу, который прячет локаторы и даёт методы-действия пользователя"
    - "Скриншот страницы"
    - "HTML-страница"
  answer: 0
- q: Где должны быть проверки (assert)?
  options: ["В тестах, а не в Page Object", "Только в Page Object", "Где угодно"]
  answer: 0
- q: Что возвращает метод loginAs, если после входа открывается каталог?
  options: ["Объект CatalogPage", "void", "LoginPage"]
  answer: 0
- q: Главная выгода Page Object?
  options:
    - "Изменение вёрстки правится в одном месте, тесты читаются как сценарии"
    - "Тесты работают без браузера"
    - "Не нужны локаторы"
  answer: 0
```
