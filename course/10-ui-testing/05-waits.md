---
title: "10.5 Ожидания и нестабильность UI-тестов"
minutes: 45
---

Ожидания — **главная** тема UI-автоматизации. Большинство нестабильных (flaky) UI-тестов падают именно из-за них.

## Проблема

Браузер работает **асинхронно** относительно теста:
- страница загружается, а JavaScript ещё подгружает данные;
- кнопка появляется через секунду после клика;
- после отправки формы загружается новая страница;
- анимация ещё не закончилась.

Тест выполняется со скоростью процессора и **не ждёт** сам. Он ищет элемент, которого ещё нет, — `NoSuchElementException`. Или находит элемент **старой** страницы — `StaleElementReferenceException`.

## Thread.sleep — не решение

```java
button.click();
Thread.sleep(3000);   // ❌
```

- если элемент появился за 0,5 с — 2,5 с потрачены впустую (на 500 тестах — 20 минут);
- если стенд нагружен и нужно 4 с — тест падает.

## Неявное ожидание (implicit wait)

```java
driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
```

Теперь каждый `findElement` будет до 5 секунд повторять поиск, пока элемент не появится. Просто, но:
- ждёт только **наличия** элемента в DOM, а не видимости/кликабельности;
- замедляет проверки «элемента **нет**» (каждая ждёт полный таймаут);
- плохо сочетается с явными ожиданиями (официально не рекомендуется смешивать).

## Явное ожидание (explicit wait) — правильный способ

```java
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

WebElement loaded = wait.until(
        ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid=loaded]")));
```

- `WebDriverWait(driver, таймаут)` — «ждатель» с максимальным временем;
- `until(условие)` — проверять условие **каждые 500 мс**, пока оно не станет истинным (вернёт результат) или не выйдет время (`TimeoutException`);
- `ExpectedConditions` — готовые условия.

| Условие | Ждёт, пока |
|---|---|
| `presenceOfElementLocated(by)` | элемент появится в DOM |
| `visibilityOfElementLocated(by)` | станет **видимым** |
| `elementToBeClickable(by)` | видим и активен |
| `invisibilityOfElementLocated(by)` | исчезнет / станет невидимым |
| `textToBePresentInElementLocated(by, "текст")` | в элементе появится текст |
| `attributeToBe(by, "attr", "v")` | атрибут примет значение |
| `urlContains("..."), urlToBe(...)` | адрес изменится |
| `titleIs(...)` | заголовок вкладки |
| `numberOfWindowsToBe(2)` | откроется вкладка |
| `alertIsPresent()` | появится alert |
| `frameToBeAvailableAndSwitchToIt(by)` | iframe загрузится (и переключиться в него) |
| `stalenessOf(element)` | старый элемент исчезнет (страница перезагрузилась) |
| `numberOfElementsToBe(by, n)` | элементов станет n |

Своё условие — лямбда (урок 5.4):

```java
wait.until(d -> d.findElements(By.cssSelector("[data-testid=random-item]")).size() >= 1);
```

## Stale Element — ловушка перезагрузки

Это мы встретили при подготовке курса, и встретите вы. Тест поиска:

```java
driver.findElement(By.cssSelector("[data-testid=search-button]")).click();
List<WebElement> names = driver.findElements(By.cssSelector("[data-testid=product-name]"));  // ❌
names.get(0).getText();   // 💥 StaleElementReferenceException
```

Что произошло:
1. клик отправил форму, браузер **начал** загружать новую страницу;
2. `findElements` выполнился мгновенно и нашёл карточки **старой** страницы (она ещё на экране);
3. новая страница загрузилась, старые элементы уничтожены;
4. `getText()` обращается к уничтоженному элементу — «stale» (устаревший).

Решение — дождаться признака **новой** страницы:

```java
button.click();
wait.until(ExpectedConditions.urlContains("search="));          // адрес изменился
// или: wait.until(ExpectedConditions.stalenessOf(button));      // старая кнопка исчезла
List<WebElement> names = driver.findElements(...);              // ✅ уже новая страница
```

Тот же эффект — «элемент не найден» сразу после отправки формы загрузки файла (урок 10.8): тест искал результат, когда новая страница ещё не пришла.

## Перекрытые элементы

Ещё одна находка при подготовке курса: в Песочнице после наведения мыши на меню выпадающий список **перекрывал** кнопку двойного клика ниже — и клик попадал в пункт меню. Selenium в таком случае либо бросает `ElementClickInterceptedException`, либо (при `Actions`) кликает в то, что сверху. Лечение: убрать мышь/закрыть всплывающее, дождаться исчезновения оверлея (`invisibilityOf...`), прокрутить элемент в видимую область.

## Практика: Песочница «Динамика»

http://localhost:8081/playground/dynamic:
- «Загрузить» — элемент `loaded` появляется через 3 с;
- «Спрятать» — `vanishing` исчезает через 2 с;
- «Запустить таймер» — `late-btn` становится активной через 4 с;
- «Получить заказ» — элемент появляется через случайное время 0,5–5 с (идеально для проверки устойчивости ожиданий).

```java
@Test
void elementAppears() {
    driver.findElement(By.cssSelector("[data-testid=start-loading]")).click();
    WebElement loaded = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid=loaded]")));
    assertThat(loaded.getText()).contains("Готово");
}
```

> 💡 **Selenide** (урок 10.10) делает такие ожидания **автоматически** в каждой проверке — это его главное преимущество. Но понимать, что происходит внутри, нужно: на собеседовании обязательно спросят про implicit/explicit wait и StaleElement.

## Таймауты

- Ожидание элемента: обычно 4–10 с. Слишком мало — нестабильность, слишком много — медленные падения.
- Загрузка страницы: `driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30))`.
- Таймауты лучше выносить в конфигурацию (урок 6.7): на медленном стенде можно увеличить без правки кода.

```task
id: m10-t05
title: Явные ожидания
module: exercises/m10-ui
test: WaitsTestCheck
file: exercises/m10-ui/src/test/java/ru/qacourse/m10/tasks/WaitsTest.java
solution: solutions/exercises/m10-ui/src/test/java/ru/qacourse/m10/tasks/WaitsTest.java
text: |
  Три теста на `/playground/dynamic` с `WebDriverWait`: элемент появляется, кнопка становится кликабельной, элемент исчезает.
  `Thread.sleep(...)` использовать нельзя — проверка это контролирует.
hint: |
  `visibilityOfElementLocated`, `elementToBeClickable`, `invisibilityOfElementLocated`. Таймаут 10 секунд.
```

```quiz
- q: Чем плох Thread.sleep в UI-тестах?
  options:
    - "Либо тратит время зря, либо его не хватает — тест нестабилен"
    - "Он не работает в Selenium"
    - "Ничем"
  answer: 0
- q: Как часто WebDriverWait проверяет условие по умолчанию?
  options: ["Каждые 500 мс", "Один раз", "Каждую минуту"]
  answer: 0
- q: Почему возникает StaleElementReferenceException?
  options:
    - "Элемент был найден раньше, но страница или часть DOM перерисовалась"
    - "Элемента никогда не было"
    - "Неверный локатор"
  answer: 0
- q: Чем visibilityOfElementLocated отличается от presenceOfElementLocated?
  options:
    - "visibility ждёт, что элемент станет видимым, presence — только появления в DOM"
    - "Ничем"
    - "presence строже"
  answer: 0
- q: Как дождаться загрузки новой страницы после отправки формы?
  options: ["Ждать urlContains(...) или stalenessOf(старый элемент)", "Thread.sleep(5000)", "Ничего не ждать"]
  answer: 0
```
