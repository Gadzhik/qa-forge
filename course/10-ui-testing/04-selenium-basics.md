---
title: "10.4 Selenium WebDriver: первый тест"
minutes: 45
---

## Подключение

```xml
<dependency>
    <groupId>org.seleniumhq.selenium</groupId>
    <artifactId>selenium-java</artifactId>
    <version>4.51.0</version>
    <scope>test</scope>
</dependency>
```

В модуле `m10-ui` подключён Selenide — он включает Selenium, поэтому отдельно добавлять не нужно.

## Создание браузера

`exercises/m10-ui/src/main/java/ru/qacourse/m10/Browser.java`:

```java
public static WebDriver chrome() {
    ChromeOptions options = new ChromeOptions();
    if (Boolean.getBoolean("headless")) {
        options.addArguments("--headless=new");
    }
    options.addArguments("--window-size=1280,900");
    options.addArguments("--lang=ru");
    return new ChromeDriver(options);
}
```

- `WebDriver` — **интерфейс** браузера (урок 4.6): тест работает с ним, не зная, Chrome это или Firefox;
- `ChromeOptions` — настройки запуска Chrome;
- `--headless=new` — **без окна**: браузер работает, но ничего не рисует на экране. Так тесты гоняют на серверах CI, где нет монитора. Включается свойством `-Dheadless=true` (`Boolean.getBoolean("headless")` читает системное свойство как boolean);
- `--window-size=1280,900` — размер окна; от него зависит вёрстка (мобильное меню, урок 10.13);
- `new ChromeDriver(options)` — **запуск браузера**. Selenium Manager сам найдёт Chrome и драйвер.

Другие браузеры: `new FirefoxDriver()`, `new EdgeDriver()`, `new SafariDriver()` (только macOS).

## Структура теста

```java
public class SeleniumFirstTest {
    private WebDriver driver;

    @BeforeEach
    void setUp() {
        driver = Browser.chrome();
    }

    @AfterEach
    void tearDown() {
        driver.quit();
    }

    @Test
    void searchIsCaseInsensitive() {
        driver.get(ShopConfig.baseUrl() + "/");

        WebElement search = driver.findElement(By.cssSelector("[data-testid=search-input]"));
        search.sendKeys("кола");
        driver.findElement(By.cssSelector("[data-testid=search-button]")).click();
        new WebDriverWait(driver, Duration.ofSeconds(5)).until(ExpectedConditions.urlContains("search="));

        List<WebElement> names = driver.findElements(By.cssSelector("[data-testid=product-name]"));
        assertThat(names).isNotEmpty();
        assertThat(names.get(0).getText()).contains("Кола");
    }
}
```

### Разбор

- **`@BeforeEach` / `@AfterEach`** — новый браузер перед каждым тестом и **обязательное** закрытие после (урок 7.2). `@AfterEach` выполняется даже при падении теста — иначе на машине копились бы десятки «висящих» Chrome.
- **`driver.quit()`** — закрыть браузер целиком и остановить драйвер. Не путайте с `driver.close()` — закрывает только текущую вкладку.
- **`driver.get(url)`** — открыть адрес и **дождаться загрузки** страницы.
- **`driver.findElement(By.cssSelector(...))`** — найти **один** элемент. `By` — класс-«способ поиска»:
  - `By.cssSelector("...")`, `By.xpath("...")`, `By.id("...")`, `By.name("...")`, `By.className("...")`, `By.tagName("...")`, `By.linkText("...")`, `By.partialLinkText("...")`.
  - Если элемента нет — **`NoSuchElementException`**; если подходит несколько — вернётся **первый**.
- **`WebElement`** — найденный элемент; методы:
  - `click()` — клик;
  - `sendKeys("текст")` — ввод с клавиатуры (дописывает к имеющемуся тексту!);
  - `clear()` — очистить поле;
  - `getText()` — видимый текст;
  - `getAttribute("value")` / `getDomAttribute(...)` — значение атрибута (для полей ввода текст лежит в `value`);
  - `isDisplayed()`, `isEnabled()`, `isSelected()` — виден ли, активен ли, отмечен ли;
  - `findElement(...)` — искать **внутри** этого элемента.
- **`driver.findElements(...)`** (с **s**) — найти **все** подходящие; если нет ни одного — пустой список (без исключения).
- **`WebDriverWait ... urlContains("search=")`** — дождаться перезагрузки страницы после отправки формы (почему это необходимо — следующий урок).

### Полезные методы driver

```java
driver.getTitle();                 // заголовок вкладки
driver.getCurrentUrl();            // текущий адрес
driver.getPageSource();            // HTML страницы
driver.navigate().back();          // назад
driver.navigate().refresh();       // обновить
driver.manage().window().maximize();
driver.manage().window().setSize(new Dimension(375, 812));
```

## Клавиши

```java
search.sendKeys("кола", Keys.ENTER);         // ввести и нажать Enter
field.sendKeys(Keys.CONTROL, "a");            // Ctrl+A (на macOS — Keys.COMMAND)
field.sendKeys(Keys.BACK_SPACE);
```

## Выпадающие списки

```java
Select category = new Select(driver.findElement(By.cssSelector("[data-testid=category-select]")));
category.selectByVisibleText("Напитки");    // по тексту
category.selectByValue("DRINK");             // по атрибуту value
String chosen = category.getFirstSelectedOption().getText();
```

`Select` (из `org.openqa.selenium.support.ui`) работает только с настоящими `<select>`. Красивые «самодельные» списки на `div` — это обычные клики.

## Если не работает

| Ошибка | Причина |
|---|---|
| `NoSuchElementException` | Неверный локатор, элемент ещё не появился, или он внутри iframe |
| `ElementNotInteractableException` | Элемент есть, но невидим/неактивен |
| `ElementClickInterceptedException` | Элемент **перекрыт** другим (всплывающее окно, меню, баннер) |
| `StaleElementReferenceException` | Элемент найден раньше, но страница/часть DOM перерисовалась |
| `SessionNotCreatedException` | Проблема запуска браузера (версия, нет Chrome) |

```task
id: m10-t04
title: Первый тест на Selenium
module: exercises/m10-ui
test: SeleniumFirstTestCheck
file: exercises/m10-ui/src/test/java/ru/qacourse/m10/tasks/SeleniumFirstTest.java
solution: solutions/exercises/m10-ui/src/test/java/ru/qacourse/m10/tasks/SeleniumFirstTest.java
text: |
  Напишите тест поиска: открыть каталог, ввести «кола» (с маленькой буквы), нажать «Найти», дождаться перезагрузки,
  проверить, что найден хотя бы один товар и первое название содержит «Кола».

  Запустите из IDEA — увидите, как браузер всё делает сам. Проверка ловит BUG-103 (поиск с учётом регистра).
hint: |
  Код разобран в уроке. Без ожидания `urlContains("search=")` тест может «поймать» элементы старой страницы.
```

```quiz
- q: Чем driver.quit() отличается от driver.close()?
  options:
    - "quit закрывает браузер целиком и драйвер, close — только текущую вкладку"
    - "Ничем"
    - "close быстрее"
  answer: 0
- q: Что вернёт findElements, если элементов нет?
  options: ["Пустой список", "NoSuchElementException", "null"]
  answer: 0
- q: Что делает опция --headless=new?
  options: ["Запускает браузер без окна", "Открывает новую вкладку", "Отключает JavaScript"]
  answer: 0
- q: "Ошибка ElementClickInterceptedException означает..."
  options: ["Элемент перекрыт другим элементом", "Элемента нет", "Браузер не запустился"]
  answer: 0
- q: Как выбрать пункт «Напитки» в настоящем select?
  options: ['new Select(element).selectByVisibleText("Напитки")', 'element.sendKeys("Напитки")', 'element.click("Напитки")']
  answer: 0
```
