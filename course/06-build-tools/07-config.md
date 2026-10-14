---
title: "6.7 Конфигурация: properties, системные свойства, Owner"
minutes: 35
---

## Зачем выносить настройки

Один и тот же набор автотестов запускают:
- локально — на `http://localhost:8081`, в Chrome с окном;
- в CI — на тестовом стенде, в Chrome без окна (headless);
- перед релизом — на stage-стенде, ещё и в Firefox.

Если адрес и браузер зашиты в код, придётся менять код под каждый запуск. Правильно — **вынести настройки** наружу и читать их при старте тестов.

## Файл .properties

Самый простой формат настроек в Java — **properties**: строки `ключ=значение`.

```properties
# src/main/resources/config.properties
base.url=http://localhost:8081
timeout.seconds=10
browser=chrome
```

- `#` — комментарий;
- ключи принято писать через точку;
- все значения — строки (число `10` придётся превратить в `int` самим).

Чтение:

```java
Properties props = new Properties();
try (InputStream in = AppConfig.class.getResourceAsStream("/config.properties")) {
    props.load(in);
}
String url = props.getProperty("base.url");                 // "http://localhost:8081"
String browser = props.getProperty("browser", "chrome");    // со значением по умолчанию
```

- `getResourceAsStream("/config.properties")` — открыть файл из ресурсов (он попадёт в `target/classes`) как поток байтов;
- `props.load(in)` — разобрать строки `ключ=значение`;
- try-with-resources закрывает поток.

> ⚠️ Файлы `.properties` исторически читаются в кодировке ISO-8859-1. Русский текст в значениях превратится в «кракозябры». Если нужен — читайте через `new InputStreamReader(in, StandardCharsets.UTF_8)`. Для ключей и URL это неважно.

## Приоритеты источников

Хороший конфиг берёт значение по цепочке:

```text
системное свойство (-Dbase.url=...)  →  переменная окружения  →  файл  →  значение по умолчанию
            самый приоритетный                                              самый слабый
```

Так в файле лежат «обычные» значения для локальной работы, а в CI их переопределяют параметрами командной строки, ничего не меняя в коде:

```bash
./mvnw test -Dbase.url=http://stage.qaburger.local
```

Реализация — задание к уроку:

```java
public static String get(String key, String defaultValue) {
    String fromSystem = System.getProperty(key);
    if (fromSystem != null) {
        return fromSystem;
    }
    return FILE.getProperty(key, defaultValue);
}
```

## Секреты — не в файлах!

Пароли, токены, ключи API **никогда** не кладут в файлы репозитория (урок 2.1). Их передают через **переменные окружения**, а в CI — через специальные «секретные переменные» (GitLab CI Variables, GitHub Secrets, Jenkins Credentials):

```java
String password = System.getenv("ADMIN_PASSWORD");
```

## Owner — конфигурация через интерфейс

Писать свой читатель настроек — хорошее упражнение, но в проектах часто используют библиотеку **Owner**. Вы описываете **интерфейс** — Owner сам реализует его, читая настройки:

```java
@Config.LoadPolicy(Config.LoadType.MERGE)
@Config.Sources({"system:properties", "classpath:config.properties"})
public interface OwnerConfig extends Config {

    @Key("base.url")
    @DefaultValue("http://localhost:8081")
    String baseUrl();

    @Key("timeout.seconds")
    @DefaultValue("5")
    int timeoutSeconds();
}
```

Разбор:
- `extends Config` — интерфейс-маркер Owner;
- `@Config.Sources({...})` — откуда читать, **в порядке приоритета**: `system:properties` — системные свойства (`-D`), `system:env` — переменные окружения, `classpath:config.properties` — файл из ресурсов, `file:/путь` — файл на диске;
- `@Config.LoadPolicy(MERGE)` — объединять все источники (по умолчанию `FIRST` — взять только первый найденный файл целиком);
- `@Key("base.url")` — какой ключ читать (без неё ключом будет имя метода `baseUrl`);
- `@DefaultValue("5")` — значение по умолчанию;
- тип результата (`int`) — Owner сам преобразует строку в число, `boolean`, `List`, `Duration`...

Использование:

```java
OwnerConfig config = ConfigFactory.create(OwnerConfig.class);
String url = config.baseUrl();
```

`ConfigFactory.create(...)` создаёт объект, реализующий ваш интерфейс. Обычно его кладут в `static final` поле и используют во всём фреймворке.

Можно подставлять одни ключи в другие: `@DefaultValue("${base.url}/api")`.

## Где хранить конфиг в проекте автотестов

- общие настройки — `src/test/resources/config.properties` (в нашем задании — `src/main/resources`, т.к. классы в `main`);
- разные стенды — разные файлы: `local.properties`, `stage.properties`, а выбор — свойством `-Denv=stage` и источником `classpath:${env}.properties`;
- секреты — только переменные окружения.

```task
id: m06-t07
title: Конфигурация
module: exercises/m06-build-tools
test: ConfigCheck
file: exercises/m06-build-tools/src/main/java/ru/qacourse/m06/AppConfig.java
solution: solutions/exercises/m06-build-tools/src/main/java/ru/qacourse/m06/AppConfig.java
text: |
  1. В `AppConfig` реализуйте `get(key, default)` с приоритетом «системное свойство → файл → по умолчанию» и `timeoutSeconds()`.
  2. В `OwnerConfig.java` добавьте аннотации Owner: источники (системные свойства, затем `classpath:config.properties`), политику MERGE, ключи и значения по умолчанию.

  Файл настроек: `src/main/resources/config.properties`.
hint: |
  - `Integer.parseInt(get("timeout.seconds", "5"))`
  - Для Owner все нужные аннотации разобраны в уроке. Ключи: `base.url`, `timeout.seconds`, `browser`.
```

```quiz
- q: Зачем выносить адрес стенда в настройки?
  options:
    - Чтобы запускать те же тесты на разных стендах без изменения кода
    - Чтобы тесты работали быстрее
    - Это требование Java
  answer: 0
- q: Какой источник настроек обычно самый приоритетный?
  options: [Системное свойство -D, Файл properties, Значение по умолчанию]
  answer: 0
- q: Где хранить пароль от тестового стенда?
  options: [В переменной окружения / секретах CI, В config.properties в репозитории, В коде теста]
  answer: 0
- q: Что делает ConfigFactory.create(OwnerConfig.class)?
  options:
    - Создаёт объект, реализующий интерфейс и читающий настройки из указанных источников
    - Создаёт файл config.properties
    - Ничего, это заглушка
  answer: 0
```
