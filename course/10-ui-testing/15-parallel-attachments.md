---
title: "10.15 Параллельный запуск и вложения в отчёт"
minutes: 35
---

## Параллельный запуск UI-тестов

100 UI-тестов по 15 секунд — 25 минут. В 4 потока — около 7 минут. Параллельность для UI-тестов почти обязательна.

### JUnit 5

Файл `src/test/resources/junit-platform.properties`:

```properties
junit.jupiter.execution.parallel.enabled=true
junit.jupiter.execution.parallel.mode.default=same_thread
junit.jupiter.execution.parallel.mode.classes.default=concurrent
junit.jupiter.execution.parallel.config.strategy=fixed
junit.jupiter.execution.parallel.config.fixed.parallelism=4
```

- `enabled=true` — включить параллельность;
- `mode.default=same_thread` — методы **одного класса** последовательно;
- `mode.classes.default=concurrent` — разные **классы** параллельно;
- `fixed.parallelism=4` — 4 потока.

Такая схема (классы параллельно, методы внутри последовательно) — самый безопасный старт.

Отдельным тестам можно запретить параллельность: `@Execution(ExecutionMode.SAME_THREAD)`, а общий ресурс защитить: `@ResourceLock("cart-of-user")`.

### TestNG

В `testng.xml`: `<suite parallel="methods" thread-count="4">` (или `classes`, `tests`).

### Почему Selenide это выдерживает

Selenide хранит браузер в **`ThreadLocal`**: у каждого потока свой браузер (урок 5.10). `$("...")` в потоке 1 и в потоке 2 обращаются к разным браузерам. С голым Selenium вы бы сами хранили `WebDriver` в `ThreadLocal<WebDriver>`.

### Что ломается при параллельном запуске

- **общие данные**: два теста с одним пользователем `user` одновременно очищают и наполняют одну корзину → случайные падения. Решение: свой пользователь на тест (`@NewUser`, урок 9.11);
- **статические поля** с состоянием (урок 4.3);
- `Configuration.*` — тоже статические настройки! Не меняйте их внутри тестов при параллельном запуске (или делайте это одинаково во всех);
- нагрузка на машину: 8 Chrome одновременно едят много памяти. Для масштаба — **Selenium Grid / Selenoid** (урок 15.4).

## Вложения: что приложить к упавшему тесту

Упал ночной UI-тест — что поможет понять причину утром?

| Вложение | Как получить |
|---|---|
| **Скриншот** | Selenide — автоматически; вручную `Selenide.screenshot(OutputType.BYTES)` |
| **HTML страницы** | Selenide — автоматически; `WebDriverRunner.source()` |
| **Логи консоли браузера** | `getWebDriver().manage().logs().get(LogType.BROWSER)` (для Chrome; включается `options.setCapability("goog:loggingPrefs", ...)`) |
| **Запросы браузера** | Selenide proxy / CDP: `DevTools` Network |
| **Видео** | Selenoid/Selenium Grid записывают видео сессии (урок 15.4) |
| **Шаги теста** | Allure `@Step` (модуль 14) |
| **Тестовые данные** | Логировать / прикладывать сгенерированные значения |

В **Allure** (модуль 14) это делается так:

```java
@Attachment(value = "Скриншот", type = "image/png")
public byte[] screenshot() {
    return Selenide.screenshot(OutputType.BYTES);
}

@Attachment(value = "HTML страницы", type = "text/html")
public String pageSource() {
    return WebDriverRunner.source();
}
```

А интеграция `allure-selenide` (`SelenideLogger.addListener("allure", new AllureSelenide())`) превращает каждое действие Selenide в шаг отчёта и сама прикладывает скриншот при падении. Соберём это в модуле 14.

## Итоги модуля

Вы освоили UI-автоматизацию:
- HTML, DOM, локаторы (CSS, XPath, `data-testid`);
- Selenium WebDriver: элементы, ожидания, алерты, фреймы, вкладки, Actions, JS, файлы;
- Page Object;
- Selenide: проверки с ожиданиями, коллекции, скачивание, скриншоты;
- мобильная вёрстка, визуальное тестирование, авторизация через API, параллельность.

Самое время вернуться в **Test Lab** и закрыть UI-сценарии (BUG-601, 701, 702, 703), а также сыграть в [**Locator Shock**](#/game/locator-shock).

```quiz
- q: Какая схема параллельности JUnit 5 — самый безопасный старт?
  options:
    - "Классы параллельно, методы внутри класса последовательно"
    - "Всё параллельно"
    - "Всё последовательно"
  answer: 0
- q: Как Selenide даёт каждому потоку свой браузер?
  options: ["Хранит браузер в ThreadLocal", "Запускает один браузер на всех", "Никак"]
  answer: 0
- q: Два параллельных теста используют одного пользователя user и его корзину. Чем это грозит?
  options: ["Случайными (flaky) падениями из-за общих данных", "Ничем", "Ускорением"]
  answer: 0
- q: Что полезно приложить к упавшему UI-тесту? (несколько)
  options: ["Скриншот", "HTML страницы", "Логи консоли браузера", "Обои рабочего стола"]
  answer: [0, 1, 2]
```
