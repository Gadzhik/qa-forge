---
title: "15.4 Браузеры в Docker: Selenium Grid и Selenoid, кроссбраузерность"
minutes: 40
---

## Зачем удалённые браузеры

- На CI-сервере нет экрана и, часто, нет установленных браузеров.
- Нужно проверять в нескольких браузерах и версиях (кроссбраузерность): Chrome, Firefox, Edge.
- Параллельный запуск 20 браузеров не помещается на одной машине.
- Хочется видео прохождения теста.

Решение — запускать браузеры **в контейнерах** и подключаться к ним по сети через протокол WebDriver.

## Selenium Grid

**Selenium Grid** — официальный сервер удалённых браузеров. Принимает WebDriver-команды на `http://host:4444` и раздаёт их браузерам (узлам, nodes).

`ci/grid/docker-compose.yml`:

```yaml
services:
  chrome:
    image: selenium/standalone-chrome:latest
    shm_size: 2gb
    ports:
      - "4444:4444"
      - "7900:7900"
    environment:
      SE_NODE_MAX_SESSIONS: 4
      SE_NODE_OVERRIDE_MAX_SESSIONS: "true"
    extra_hosts:
      - "host.docker.internal:host-gateway"
```

- `selenium/standalone-chrome` — Grid и Chrome в одном контейнере (для начала проще всего; для боевого использования зафиксируйте тег версии);
- **`shm_size: 2gb`** — Chrome использует общую память `/dev/shm`; в Docker по умолчанию 64 МБ — вкладки падают с «Aw, Snap!». Классическая проблема;
- **4444** — адрес WebDriver; **7900** — noVNC: открыть http://localhost:7900 (пароль `secret`) и **смотреть**, что делает браузер;
- `SE_NODE_MAX_SESSIONS: 4` — до 4 браузеров одновременно;
- `extra_hosts host.docker.internal` — браузер в контейнере должен открыть QA Burger, работающий **на вашем компьютере**. Для него `localhost` — это сам контейнер, а ваш компьютер — `host.docker.internal`.

```bash
docker compose -f ci/grid/docker-compose.yml up -d
```

Консоль Grid: http://localhost:4444/ui.

### Тесты на удалённом браузере

**Selenide** — одна настройка:

```bash
./mvnw -f exercises/m10-ui/pom.xml test -Dtest=CatalogSelenideTest \
    -Dselenide.remote=http://localhost:4444/wd/hub \
    -Dshop.url=http://host.docker.internal:8081
```

- `selenide.remote` — адрес Grid: Selenide создаст `RemoteWebDriver` вместо локального браузера;
- `shop.url` — адрес приложения **с точки зрения браузера в контейнере**.

**Selenium**:

```java
ChromeOptions options = new ChromeOptions();
WebDriver driver = new RemoteWebDriver(new URL("http://localhost:4444/wd/hub"), options);
```

Тест остальным кодом не отличается — вот она, сила интерфейса `WebDriver` (урок 4.6).

### Кроссбраузерность

Добавьте в compose `selenium/standalone-firefox` (порт, например, 4445) или поднимите полноценный Grid: **hub** + узлы `node-chrome`, `node-firefox`, `node-edge`. Браузер выбирается в тесте: `-Dselenide.browser=firefox`. Параметризованный запуск в CI по матрице браузеров — урок 15.6.

### Видео

Образ `selenium/video` записывает экран узла в mp4 — прикладывается к отчёту для упавших тестов.

## Selenoid

**Selenoid** (компания Aerokube) — популярная в России альтернатива Grid: каждый браузер — отдельный лёгкий контейнер, быстрый старт, видео и логи «из коробки», веб-интерфейс Selenoid UI. Важно знать: проект Selenoid **больше не развивается** (Aerokube перешла на платные продукты — Moon для Kubernetes). В существующих компаниях он до сих пор встречается, поэтому его нужно узнавать:

- конфигурация браузеров — `browsers.json`;
- Selenoid UI — порт 8080, сам Selenoid — 4444 (тот же протокол WebDriver);
- capabilities `selenoid:options`: `enableVNC`, `enableVideo`, `name` — включить экран и запись видео:

```java
Map<String, Object> selenoid = Map.of("enableVNC", true, "enableVideo", true);
ChromeOptions options = new ChromeOptions();
options.setCapability("selenoid:options", selenoid);
Configuration.browserCapabilities = options;
Configuration.remote = "http://localhost:4444/wd/hub";
```

Для новых проектов — Selenium Grid (бесплатный и активный), облака (BrowserStack, Sauce Labs, LambdaTest) или Moon.

## Частые проблемы удалённых браузеров

| Симптом | Причина |
|---|---|
| Браузер открывает страницу ошибки «не удаётся подключиться» | Приложение указано как `localhost` — для контейнера это он сам |
| Вкладки падают | Мало `shm_size` |
| Загрузка файла не работает | Файл на вашей машине, а браузер — в контейнере → `LocalFileDetector` (Selenide делает сам) |
| `SessionNotCreatedException` | Все слоты заняты (`max-sessions`) или нет нужного браузера |
| Тест висит | Grid ждёт свободный слот — смотрите консоль Grid |

```quiz
- q: Зачем shm_size 2gb для контейнера с Chrome?
  options: ["Chrome использует общую память; по умолчанию её мало, и вкладки падают", "Для скорости сети", "Для хранения видео"]
  answer: 0
- q: Как браузер в контейнере откроет QA Burger, запущенный на вашем компьютере?
  options: ["По адресу host.docker.internal:8081", "localhost:8081", "Никак"]
  answer: 0
- q: Как направить Selenide в Selenium Grid?
  options: ["-Dselenide.remote=http://localhost:4444/wd/hub", "-Dgrid=true", "Поменять код тестов"]
  answer: 0
- q: Что нужно знать о Selenoid сегодня?
  options:
    - "Он встречается в проектах, но больше не развивается — для новых проектов выбирают Grid, облака или Moon"
    - "Это новейший стандарт"
    - "Он работает только в Windows"
  answer: 0
```
