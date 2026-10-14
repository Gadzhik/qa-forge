---
title: "14.3 Конфигурация, окружения и тестовые данные"
minutes: 35
---

## Конфигурация фреймворка

```java
@Config.LoadPolicy(Config.LoadType.MERGE)
@Config.Sources({"system:properties", "system:env", "classpath:framework.properties"})
public interface FrameworkConfig extends Config {

    @Key("shop.url")
    @DefaultValue("http://localhost:8081")
    String baseUrl();

    @Key("user.login")
    @DefaultValue("user")
    String userLogin();

    @Key("await.seconds")
    @DefaultValue("20")
    int awaitSeconds();
}
```

Это Owner из урока 6.7. Приоритеты: `-Dshop.url=...` → переменная окружения → `framework.properties` → `@DefaultValue`.

```java
public static FrameworkConfig get() {
    return ConfigFactory.create(FrameworkConfig.class, System.getProperties());
}
```

`ConfigFactory.create(..., System.getProperties())` — передать текущие системные свойства. Обычно конфиг создают **один раз** (`static final`), но в нашем курсе адрес меняется между проверками, поэтому создаётся при каждом вызове.

## Несколько окружений

Частый подход — файл на каждый стенд и выбор свойством `env`:

```text
src/test/resources/
├── local.properties     shop.url=http://localhost:8081
├── stage.properties     shop.url=https://stage.qaburger.ru
└── prod.properties      shop.url=https://qaburger.ru   (только smoke и только безопасные проверки!)
```

```java
@Config.Sources({"system:properties", "classpath:${env}.properties", "classpath:local.properties"})
```

```bash
./mvnw test -Denv=stage
```

`${env}` подставится из системного свойства; если не передано — сработает следующий источник `local.properties`.

## Секреты

Пароли тестовых пользователей и ключи API — **только** из переменных окружения или секретов CI:

```java
@Key("ADMIN_PASSWORD")      // переменная окружения
String adminPassword();
```

В CI: GitLab → Settings → CI/CD → Variables (masked), GitHub → Settings → Secrets.

## Тестовые данные: откуда брать

| Подход | Плюсы | Минусы |
|---|---|---|
| **Константы** (`Products.CLASSIC_BURGER = 1`) | Просто | Зависят от состояния стенда |
| **Генерация** (Datafaker, UUID) | Уникальность, независимость | Сложнее воспроизвести |
| **Создание через API в подготовке** | Реалистично, независимо | Медленнее |
| **Создание в БД** | Быстро, любое состояние | Риск «невозможных» данных |
| **Файлы** (JSON/CSV в resources) | Наглядно, легко менять | Нужно поддерживать |
| **Справочные данные стенда** | Ничего не создавать | Кто-то может изменить |

Хорошая практика: **неизменяемые справочники** (товары) — константами; **всё, что тест меняет** (пользователи, заказы, корзины) — создавать самому и уникальным.

### Фабрики данных

```java
public final class TestUsers {
    private static final Faker faker = new Faker();

    public static RegisterRequest random() {
        String username = "u" + faker.number().digits(9);
        return new RegisterRequest(username, username + "@test.local", "Secret1");
    }
}
```

### Данные из файлов

```java
@ParameterizedTest
@CsvFileSource(resources = "/data/promo-codes.csv", numLinesToSkip = 1)
void promo(String code, int percent) { ... }
```

```text
code,percent
SALE10,10
BURGER20,20
```

Для сложных структур — JSON в ресурсах, читаемый Jackson в модель.

## Логирование фреймворка

Для разбора падений полезен лог (SLF4J + Logback/Log4j2): в начале теста — какие данные сгенерированы, в клиентах — ключевые действия. В Allure это частично делают шаги и вложения, но лог в CI спасает, когда отчёта нет.

```quiz
- q: Как запустить тесты на stage, если файлы конфигурации разделены по окружениям?
  options: ["-Denv=stage", "Переписать код", "Переименовать файл"]
  answer: 0
- q: Где хранить пароль администратора тестового стенда?
  options: ["В переменной окружения / секретах CI", "В stage.properties в git", "В коде теста"]
  answer: 0
- q: Какие данные лучше создавать в самом тесте?
  options: ["Всё, что тест меняет: пользователей, заказы, корзины", "Справочник товаров", "Ничего"]
  answer: 0
- q: Чем опасен прогон тестов на prod?
  options: ["Тесты могут изменить реальные данные — допустим только безопасный smoke", "Ничем", "Prod медленнее"]
  answer: 0
```
