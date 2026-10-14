---
title: "10.13 Скриншотное тестирование и мобильная вёрстка"
minutes: 40
---

## Мобильная вёрстка

Сайты **адаптивные**: на узком экране меню прячется под кнопку ☰, карточки выстраиваются в столбик, часть текста скрывается. QA Burger тоже: при ширине до 640px навигация скрыта, появляется кнопка `menu-toggle`.

### Способ 1: размер окна

```java
Configuration.browserSize = "375x812";    // размер iPhone X/11/12 mini
open("/");
$("[data-testid=menu-toggle]").shouldBe(visible);
$("[data-testid=nav]").shouldNotBe(visible);
$("[data-testid=menu-toggle]").click();
$("[data-testid=nav]").shouldBe(visible);
```

- `browserSize` задаётся **до** открытия браузера (он применяется при старте). Если браузер уже открыт — `WebDriverRunner.getWebDriver().manage().window().setSize(new Dimension(375, 812))`;
- после теста верните обычный размер, иначе следующие тесты в том же потоке получат «телефон».

### Способ 2: эмуляция устройства Chrome

Размер окна — только ширина. Настоящая эмуляция добавляет плотность пикселей, тач-события и мобильный `User-Agent` (по нему некоторые сайты отдают отдельную мобильную версию):

```java
ChromeOptions options = new ChromeOptions();
options.setExperimentalOption("mobileEmulation", Map.of("deviceName", "iPhone 12 Pro"));
Configuration.browserCapabilities = options;
```

Это то же самое, что Device Toolbar в DevTools (урок 2.5).

### Способ 3: реальные устройства

Для мобильных **приложений** и настоящих мобильных браузеров — **Appium** (тот же протокол WebDriver, но для Android/iOS) и облачные фермы устройств (BrowserStack, Sauce Labs, Selenoid с эмуляторами Android). Это отдельная большая тема за рамками курса.

## Скриншотное (визуальное) тестирование

Функциональный тест не заметит, что кнопка «уехала» за край экрана, текст наложился на картинку или сломались стили. Для этого — **визуальное тестирование**: сравнение скриншота с эталоном.

Как это работает:
1. Первый запуск — сохранить **эталонный** скриншот (baseline), человек проверяет его глазами.
2. Следующие запуски — снять скриншот и сравнить с эталоном попиксельно (с допуском).
3. Различия → тест падает, сохраняется картинка различий (diff), человек решает: баг или ожидаемое изменение (тогда обновляют эталон).

Снять скриншот:

```java
File page = Selenide.screenshot(OutputType.FILE);                 // вся видимая область
File element = $("[data-testid=product-card]").screenshot();      // только элемент
byte[] bytes = ((TakesScreenshot) getWebDriver()).getScreenshotAs(OutputType.BYTES);
```

Сравнение — библиотеки **AShot** (умеет и скриншоты всей страницы с прокруткой), **image-comparison** (romankh3), или сервисы: Applitools, Percy, Happo; в Playwright — встроено (`toHaveScreenshot`).

Пример с `image-comparison`:

```java
BufferedImage expected = ImageComparisonUtil.readImageFromResources("baseline/card.png");
BufferedImage actual = ImageIO.read($("[data-testid=product-card]").screenshot());
ImageComparisonResult result = new ImageComparison(expected, actual)
        .setAllowingPercentOfDifferentPixels(1.0)          // допуск 1%
        .compareImages();
assertThat(result.getImageComparisonState()).isEqualTo(ImageComparisonState.MATCH);
```

### Почему визуальные тесты капризны — особенно у нас

- **Шрифты и сглаживание отличаются в Windows, macOS и Linux** — один и тот же сайт даёт разные пиксели. Эталон, снятый на macOS, не совпадёт с Linux в CI. Поэтому эталоны снимают и сравнивают **в одном окружении** — обычно в Docker-контейнере с фиксированной версией браузера (модуль 15). По этой причине в нашем курсе (он работает на разных ОС) скриншотные тесты не проверяются автоматически — попробуйте их как упражнение.
- Динамические данные: даты, случайные товары, анимации, мигающий курсор → их **маскируют** (закрашивают области) или подменяют данные.
- Разный размер окна и масштаб экрана.

Правило: визуальные проверки — на **ключевые** компоненты (шапка, карточка товара, форма оплаты), в контролируемом окружении, с допуском.

> 🧪 **Попробуй сам.** Напишите тест, который снимает скриншот карточки товара в дизайне v1 и v2 и сохраняет в `build/screens/`. Откройте файлы и сравните. Затем подключите `image-comparison` и убедитесь, что тест падает при сравнении v1 с v2.

```task
id: m10-t13
title: Мобильная вёрстка
module: exercises/m10-ui
test: MobileLayoutTestCheck
file: exercises/m10-ui/src/test/java/ru/qacourse/m10/tasks/MobileLayoutTest.java
solution: solutions/exercises/m10-ui/src/test/java/ru/qacourse/m10/tasks/MobileLayoutTest.java
text: |
  Два теста: на «телефоне» 375×812 меню скрыто под кнопкой ☰ и открывается по нажатию; на «десктопе» 1280×900 — наоборот, кнопки нет, меню видно.
hint: |
  `Configuration.browserSize` до `open(...)`, а в `@AfterEach` — `closeWebDriver()` и возврат размера по умолчанию `"1366x768"`.
```

```quiz
- q: Когда нужно задать Configuration.browserSize?
  options: ["До открытия браузера", "После каждого клика", "Неважно"]
  answer: 0
- q: Чем эмуляция устройства отличается от изменения размера окна?
  options:
    - "Добавляет мобильный User-Agent, плотность пикселей и тач-события"
    - "Ничем"
    - "Работает только в Firefox"
  answer: 0
- q: Почему скриншотные тесты нестабильны на разных ОС?
  options: ["Шрифты и сглаживание отрисовываются по-разному", "Selenium работает только в Linux", "Скриншоты нельзя сравнивать"]
  answer: 0
- q: Как поступают с датами и случайными данными на скриншотах?
  options: ["Маскируют области или подменяют данные", "Увеличивают допуск до 100%", "Никак"]
  answer: 0
```
