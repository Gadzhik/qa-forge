---
title: "10.7 Actions и JavascriptExecutor"
minutes: 40
---

`click()` и `sendKeys()` покрывают большинство случаев. Для сложных жестов есть **Actions**, а когда ничего не помогает — **JavaScript**.

## Actions — «низкоуровневые» действия

```java
Actions actions = new Actions(driver);
```

| Метод | Что делает |
|---|---|
| `moveToElement(el)` | навести мышь (hover) |
| `click(el)` / `doubleClick(el)` / `contextClick(el)` | клик / двойной / правой кнопкой |
| `clickAndHold(el)` / `release()` | зажать / отпустить кнопку мыши |
| `dragAndDrop(source, target)` | перетащить |
| `dragAndDropBy(el, dx, dy)` | перетащить на смещение в пикселях |
| `keyDown(Keys.SHIFT)` / `keyUp(...)` | зажать / отпустить клавишу |
| `sendKeys(...)` | ввод |
| `scrollToElement(el)` / `scrollByAmount(dx, dy)` | прокрутка |
| `pause(Duration)` | пауза внутри цепочки |
| **`perform()`** | **выполнить** собранную цепочку |

Действия собираются в цепочку и выполняются **только** по `perform()`:

```java
actions.moveToElement(menu).pause(Duration.ofMillis(300)).click(item).perform();
```

> ⚠️ Забыли `perform()` — ничего не произойдёт, и ошибки тоже не будет.

### Наведение (hover)

Меню, которое появляется при наведении мыши (CSS `:hover`), обычным кликом не открыть:

```java
actions.moveToElement(el("hover-target")).perform();
assertThat(driver.findElements(By.cssSelector("[data-testid=hover-item]"))).allMatch(WebElement::isDisplayed);
```

### Двойной и правый клик

```java
actions.doubleClick(el("double-click-btn")).perform();
actions.contextClick(el("right-click-btn")).perform();
```

> ⚠️ После `moveToElement` мышь **остаётся** на месте, а меню — открытым. Если ниже есть элемент, который меню перекрывает, следующий клик Actions попадёт в меню. Мы наткнулись на это при подготовке курса и сдвинули кнопки в Песочнице. Правило: перед следующим действием уберите мышь (`moveToElement` на другой элемент) или дождитесь закрытия всплывающего.

### Drag & Drop

```java
new Actions(driver).dragAndDrop(el("draggable"), el("droppable")).perform();
assertThat(el("dnd-status").getText()).isEqualTo("на тарелке");
```

Внутри: нажать мышь на источнике → переместить на цель → отпустить.

> ⚠️ **HTML5 drag&drop** (атрибут `draggable="true"` и события `dragstart`/`drop`) часто **не работает** с Selenium Actions: браузер не генерирует «настоящие» drag-события от эмуляции мыши. В Песочнице: бургер на тарелку сделан на событиях мыши — Actions работает; сортируемый список — на HTML5 — Actions может не сработать. Обходные пути: JavaScript, который вызывает события вручную, или Selenide `dragAndDrop(to(...).usingJavaScript())`.

## JavascriptExecutor

Selenium умеет выполнить в странице произвольный JavaScript:

```java
JavascriptExecutor js = (JavascriptExecutor) driver;
js.executeScript("window.scrollTo(0, document.body.scrollHeight)");          // прокрутить в самый низ
js.executeScript("arguments[0].scrollIntoView(true)", element);               // прокрутить к элементу
js.executeScript("arguments[0].click()", element);                            // клик JavaScript'ом
Object title = js.executeScript("return document.title");                     // вернуть значение
Long count = (Long) js.executeScript("return document.querySelectorAll('li').length");
js.executeScript("arguments[0].value = arguments[1]", input, "текст");        // задать значение поля
js.executeScript("localStorage.clear()");
```

- `(JavascriptExecutor) driver` — приведение типа: `ChromeDriver` реализует и `WebDriver`, и `JavascriptExecutor`;
- `arguments[0]`, `arguments[1]` — параметры, переданные после скрипта (элементы, строки, числа);
- `return ...` — вернуть значение в Java (числа приходят как `Long`/`Double`, элементы — как `WebElement`).

### Когда JS оправдан

- прокрутка, бесконечная лента;
- чтение того, чего нет в DOM-атрибутах (localStorage, вычисленные стили, состояние приложения);
- подготовка состояния (почистить хранилище).

### Когда JS — обман

`arguments[0].click()` нажмёт кнопку, **даже если она перекрыта или невидима** — пользователь бы так не смог. Тест пройдёт, а реальный баг «кнопку закрывает баннер» вы пропустите. Используйте JS-клик только осознанно, а не «чтобы тест позеленел».

## Бесконечная прокрутка

```java
for (int i = 0; i < 10 && !el("feed-end").isDisplayed(); i++) {
    int before = driver.findElements(By.cssSelector("[data-testid=feed-item]")).size();
    js.executeScript("window.scrollTo(0, document.body.scrollHeight)");
    wait.until(d -> d.findElements(By.cssSelector("[data-testid=feed-item]")).size() > before
            || el("feed-end").isDisplayed());
}
assertThat(driver.findElements(By.cssSelector("[data-testid=feed-item]"))).hasSize(100);
```

- цикл с ограничением (не больше 10 прокруток — защита от бесконечного цикла);
- запоминаем, сколько элементов было, крутим вниз, **ждём**, пока станет больше (или появится «конец»);
- своё условие ожидания — лямбда.

```task
id: m10-t07
title: Actions и JavaScript
module: exercises/m10-ui
test: ActionsJsTestCheck
file: exercises/m10-ui/src/test/java/ru/qacourse/m10/tasks/ActionsJsTest.java
solution: solutions/exercises/m10-ui/src/test/java/ru/qacourse/m10/tasks/ActionsJsTest.java
text: |
  Три теста: перетаскивание бургера на тарелку, наведение + двойной + правый клик, бесконечная прокрутка до 100 элементов.
hint: |
  Не забывайте `perform()`. Для прокрутки — цикл с `executeScript("window.scrollTo(0, document.body.scrollHeight)")` и ожиданием роста количества элементов.
```

```quiz
- q: Что произойдёт, если не вызвать perform() у Actions?
  options: ["Ничего — действия не выполнятся", "Ошибка компиляции", "Действия выполнятся дважды"]
  answer: 0
- q: Как навести мышь на элемент?
  options: ["actions.moveToElement(el).perform()", "el.hover()", "el.click()"]
  answer: 0
- q: Почему JS-клик может скрыть баг?
  options:
    - "Он нажмёт кнопку, даже если она перекрыта или невидима для пользователя"
    - "Он медленнее"
    - "Он не работает"
  answer: 0
- q: Что такое arguments[0] в executeScript?
  options: ["Первый параметр, переданный после текста скрипта", "Первый элемент страницы", "Номер вкладки"]
  answer: 0
```
