---
title: "10.11 Selenide: коллекции, условия, устойчивые локаторы"
minutes: 40
---

## Коллекции: $$

```java
ElementsCollection names = $$("[data-testid=product-name]");
```

`$$` возвращает **коллекцию** — тоже «живую»: элементы ищутся при каждом обращении.

### Проверки коллекций (CollectionCondition)

| Проверка | Смысл |
|---|---|
| `shouldHave(size(5))` | ровно 5 элементов |
| `shouldHave(sizeGreaterThan(0))`, `sizeLessThanOrEqual(10)` | количество |
| `shouldHave(texts("Кола 0.5", "Лимонад..."))` | тексты по порядку (каждый — **содержит**) |
| `shouldHave(exactTexts(...))` | тексты по порядку, точно |
| `shouldHave(textsInAnyOrder(...))` | тексты в любом порядке |
| `shouldHave(itemWithText("Кола 0.5"))` | есть элемент с таким текстом |
| `shouldBe(empty)` | пустая |
| `shouldHave(anyMatch("описание", el -> ...))`, `allMatch(...)` | своё условие |

Все с ожиданием, как и у одиночных элементов.

### Работа с элементами коллекции

```java
$$("[data-testid=product-card]").first();                         // первый
$$("[data-testid=product-card]").get(2);                          // третий (с 0)
$$("[data-testid=product-card]").last();
$$("[data-testid=product-card]").findBy(text("Кола 0.5"));        // первый, у которого есть текст
$$("[data-testid=product-card]").filterBy(text("бургер"));        // все такие — новая коллекция
$$("[data-testid=product-card]").findBy(attribute("data-product-id", "12"))
        .$("[data-testid=add-to-cart]").click();                  // кнопка внутри нужной карточки
List<String> all = $$("[data-testid=product-name]").texts();      // список текстов
```

`findBy(text("Кола 0.5")).$(...)` — лаконичная замена XPath «кнопка в карточке с таким текстом» из урока 10.3.

## Фильтр каталога через интерфейс

```java
@Test
void filterDrinks() {
    open("/");
    $("[data-testid=category-select]").selectOption("Напитки");
    $("[data-testid=search-button]").click();
    ElementsCollection names = $$("[data-testid=product-name]");
    names.shouldHave(size(5)).shouldHave(itemWithText("Кола 0.5"));
}
```

После клика «Найти» страница перезагружается, а `shouldHave(size(5))` **ждёт**, пока на странице станет 5 элементов, — в Selenium здесь была бы ловушка stale-элементов, Selenide справляется сам.

## Сортировка: переводим тексты в числа

```java
List<Double> prices = $$("[data-testid=product-price]").texts().stream()
        .map(t -> Double.parseDouble(t.replace("₽", "").trim()))
        .toList();
assertThat(prices).isSortedAccordingTo(Comparator.reverseOrder());
```

- `.texts()` — **без ожидания** возвращает текущие тексты; поэтому сначала убедитесь, что страница нужная (`$$(...).shouldHave(size(20))`);
- `replace("₽", "").trim()` — «289.90 ₽» → «289.90»;
- `Double.parseDouble` — в число; дальше AssertJ (урок 7.3).

## Устойчивость к редизайну

Дизайн v2 QA Burger меняет теги, классы и порядок элементов, но оставляет `data-testid`:

```java
@Test
void designV2KeepsTestIds() {
    open("/?design=v2");
    $$("[data-testid=product-card]").shouldHave(size(20));
    $("[data-testid=search-input]").shouldBe(visible);
    open("/?design=v1");                     // вернуть дизайн (он запоминается в cookie)
}
```

Тест на `.product-card` или `#search-input` в v2 упал бы, хотя функциональность цела. Это и есть цена хрупких локаторов.

## Работа с alert, frame, вкладками в Selenide

```java
switchTo().alert().accept();                    // или: confirm(), prompt("Анна")
switchTo().frame($("[data-testid=inner-frame]"));
switchTo().defaultContent();
switchTo().window(1);                           // вторая вкладка
$("[data-testid=draggable]").dragAndDrop(DragAndDropOptions.to("[data-testid=droppable]"));
executeJavaScript("window.scrollTo(0, document.body.scrollHeight)");
```

## Свои условия

```java
public static Condition price(double expected) {
    return Condition.match("цена " + expected,
            el -> Double.parseDouble(el.getText().replace("₽", "").trim()) == expected);
}

$("[data-testid=product-price]").shouldHave(price(289.9));
```

Аналог «условий» из урока 9.8 для UI.

```task
id: m10-t11
title: Коллекции и устойчивые локаторы
module: exercises/m10-ui
test: CatalogSelenideTestCheck
file: exercises/m10-ui/src/test/java/ru/qacourse/m10/tasks/CatalogSelenideTest.java
solution: solutions/exercises/m10-ui/src/test/java/ru/qacourse/m10/tasks/CatalogSelenideTest.java
text: |
  Три теста каталога на Selenide: фильтр «Напитки» (5 товаров, есть «Кола 0.5»), сортировка «Сначала дорогие» (цены по убыванию),
  дизайн v2 (20 карточек, поиск виден).

  Проверка ловит BUG-105 и BUG-102 через интерфейс.
hint: |
  `selectOption("Напитки")` / `selectOption("Сначала дорогие")` выбирают по видимому тексту. Тексты цен — `$$(...).texts()`.
  Ловушка сортировки: и до, и после нажатия «Найти» на странице 20 карточек, поэтому `shouldHave(size(20))` не ждёт новую страницу.
  Сначала дождитесь, что адрес изменился: `webdriver().shouldHave(urlContaining("sort=price"))`, и только потом читайте цены.
  `texts()` не ждёт — он берёт то, что на странице прямо сейчас.
```

```quiz
- q: Что делает $$(...).findBy(text("Кола")).$(".btn")?
  options: ["Кнопку внутри первого элемента коллекции, содержащего «Кола»", "Все кнопки", "Ошибку"]
  answer: 0
- q: Ждёт ли метод .texts() появления элементов?
  options: ["Нет — сначала убедитесь, что страница готова (shouldHave(size(...)))", "Да, 4 секунды", "Да, бесконечно"]
  answer: 0
- q: Чем texts(...) отличается от exactTexts(...)?
  options: ["texts — каждый содержит строку, exactTexts — точное совпадение", "Ничем", "texts игнорирует порядок"]
  answer: 0
- q: Почему тест с [data-testid=...] пережил дизайн v2?
  options: ["data-testid не меняется при изменении вёрстки", "Selenide игнорирует вёрстку", "Повезло"]
  answer: 0
```
