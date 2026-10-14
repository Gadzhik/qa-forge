---
title: "14.2 Allure: шаги, метки, вложения, отчёт"
minutes: 50
---

**Allure Report** — самый популярный формат отчётов об автотестах. Он показывает каждый тест **по шагам**, с вложениями (запросы, ответы, скриншоты), группирует тесты по функциональности, хранит историю прогонов и графики. Отчёт читают не только автоматизаторы, но и ручные тестировщики, разработчики и менеджеры.

## Как устроен Allure

1. Во время прогона **адаптер** (allure-junit5, allure-testng...) пишет **результаты** — JSON-файлы — в папку `allure-results`.
2. Отдельный инструмент (**Allure CLI** / плагин Maven/Gradle) превращает результаты в **HTML-отчёт**.

## Подключение (Maven)

Разберём `exercises/m14-framework/pom.xml`:

```xml
<dependency><groupId>io.qameta.allure</groupId><artifactId>allure-junit5</artifactId></dependency>
<dependency><groupId>io.qameta.allure</groupId><artifactId>allure-rest-assured</artifactId></dependency>
<dependency><groupId>org.aspectj</groupId><artifactId>aspectjweaver</artifactId><version>${aspectj.version}</version><scope>test</scope></dependency>
```

- **`allure-junit5`** — адаптер: следит за запуском тестов JUnit и пишет результаты (подключается автоматически, без аннотаций);
- **`allure-rest-assured`** — фильтр, прикладывающий запросы и ответы;
- **`aspectjweaver`** — нужен для `@Step` и `@Attachment` (ниже). Версии — из `allure-bom` в родителе.

```xml
<plugin>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <argLine>-javaagent:"${settings.localRepository}/org/aspectj/aspectjweaver/${aspectj.version}/aspectjweaver-${aspectj.version}.jar"</argLine>
        <systemPropertyVariables>
            <allure.results.directory>${project.build.directory}/allure-results</allure.results.directory>
        </systemPropertyVariables>
    </configuration>
</plugin>
```

- **`argLine`** — параметры JVM, в которой surefire запускает тесты;
- **`-javaagent:...aspectjweaver.jar`** — подключить **агент** AspectJ. Он при загрузке классов «вплетает» в методы с `@Step` код, который сообщает Allure о начале и конце шага. Без агента аннотации `@Step` **молча не работают** — самая частая проблема настройки Allure;
- `${settings.localRepository}` — путь к `~/.m2/repository`; кавычки вокруг пути — на случай пробелов (например, в Windows `C:\Users\Анна Иванова\.m2`);
- `allure.results.directory` — куда писать результаты (`target/allure-results`).

```xml
<plugin>
    <groupId>io.qameta.allure</groupId>
    <artifactId>allure-maven</artifactId>
    <version>2.18.0</version>
    <configuration>
        <reportVersion>2.46.1</reportVersion>
        <resultsDirectory>${project.build.directory}/allure-results</resultsDirectory>
    </configuration>
</plugin>
```

Плагин сам скачает Allure CLI нужной версии и построит отчёт.

## Запуск и отчёт

```bash
./mvnw -f exercises/m14-framework/pom.xml test          # тесты → target/allure-results
./mvnw -f exercises/m14-framework/pom.xml allure:serve  # построить отчёт и открыть в браузере
./mvnw -f exercises/m14-framework/pom.xml allure:report # только построить → target/site/allure-maven-plugin
```

`allure:serve` поднимает маленький веб-сервер (открывать `index.html` двойным кликом нельзя — браузер запретит загрузку данных с диска). Остановить — Ctrl+C.

Для Gradle — плагин `id 'io.qameta.allure' version '2.12.0'` и задачи `allureServe` / `allureReport`. Отдельно CLI можно поставить: `brew install allure`, `scoop install allure`, или скачать архив.

## Шаги: @Step

```java
@Step("Добавить в корзину товар {productId} × {quantity}")
public CartClient add(long productId, int quantity) { ... }
```

- в отчёте тест показывается деревом шагов: «Войти как user» → «Очистить корзину» → «Добавить в корзину товар 1 × 2» → «Оформить заказ с промокодом null»;
- **`{productId}`** — подставляется значение параметра метода; можно `{order.id}` — поле объекта;
- вложенные вызовы методов с `@Step` дают вложенные шаги;
- упавший шаг подсвечивается красным — сразу видно, **где** сломалось.

Без аннотации — лямбда-шаг прямо в тесте:

```java
Allure.step("Проверить сумму заказа", () -> assertThat(order.total()).isEqualByComparingTo("579.80"));
```

## Метки: группировка и важность

```java
@Epic("Магазин")
@Feature("Заказы")
public class OrderFlowTest extends BaseTest {

    @Test
    @Story("Оформление заказа")
    @Severity(SeverityLevel.CRITICAL)
    @TmsLink("TC-ORDER-1")
    @Issue("BUG-401")
    @Owner("anna")
    @Description("Подробное описание теста")
    void orderFromCart() { ... }
}
```

| Аннотация | Для чего |
|---|---|
| `@Epic` / `@Feature` / `@Story` | Иерархия во вкладке **Behaviors**: эпик → фича → история |
| `@Severity` | Важность: `BLOCKER`, `CRITICAL`, `NORMAL`, `MINOR`, `TRIVIAL` |
| `@TmsLink("TC-1")` | Ссылка на тест-кейс в TMS (урок 1.7) |
| `@Issue("BUG-401")` | Ссылка на баг в трекере |
| `@Link(name, url)` | Любая ссылка |
| `@Owner` | Ответственный |
| `@DisplayName` (JUnit) | Название теста в отчёте |

Шаблоны ссылок задаются в `src/test/resources/allure.properties`:

```properties
allure.link.tms.pattern=http://tms.qaburger.local/{}
allure.link.issue.pattern=http://localhost:8081/admin#{}
```

`{}` заменится значением из аннотации.

## Вложения

```java
@Attachment(value = "Ответ сервера", type = "application/json")
public String attachJson(String json) {
    return json;
}

Allure.addAttachment("Данные пользователя", "text/plain", user.toString());
Allure.addAttachment("receipt.pdf", "application/pdf", new ByteArrayInputStream(pdf), ".pdf");
```

- **`AllureRestAssured`** (у нас в `Specs`) прикладывает **каждый** запрос и ответ — с методом, URL, заголовками, телом, в виде curl;
- **Selenide**: `SelenideLogger.addListener("AllureSelenide", new AllureSelenide().screenshots(true).savePageSource(true));` — каждое действие становится шагом, при падении — скриншот и HTML (урок 10.15);
- свои вложения — тестовые данные, логи, файлы.

## Что есть в отчёте

- **Overview** — сводка: сколько прошло/упало, графики, окружение;
- **Categories** — упавшие тесты по типам: дефекты продукта (`AssertionError`) и проблемы тестов (исключения);
- **Suites** — по классам; **Behaviors** — по эпикам/фичам;
- **Timeline** — когда и в каком потоке шёл каждый тест (видна параллельность);
- **History** и **Retries** — если сохранять историю между прогонами (в CI, модуль 15);
- у каждого теста: шаги, вложения, параметры, длительность, ссылки.

> 💡 **Окружение в отчёте**: файл `environment.properties` в папке результатов (`Stand=local`, `Browser=Chrome 130`) показывается на Overview. Удобно генерировать его в `@BeforeAll`/расширении.

```task
id: m14-t02
title: Шаги и метки Allure
module: exercises/m14-framework
test: AllureSetupCheck
file: exercises/m14-framework/src/main/java/ru/qacourse/fw/api/CartClient.java
solution: solutions/exercises/m14-framework/src/main/java/ru/qacourse/fw/api/CartClient.java
text: |
  1. Добавьте `@Step` всем методам `CartClient` и реализуйте `OrderClient` (create, tryCreate, pay, get) — тоже с `@Step`.
  2. В `OrderFlowTest` добавьте `@Epic("Магазин")`, `@Feature("Заказы")`, `@Story("Оформление заказа")`, `@Severity(SeverityLevel.CRITICAL)`, `@TmsLink("TC-ORDER-1")`.

  Проверка прогонит тест и прочитает JSON-результаты Allure. Затем постройте отчёт сами: `./mvnw -f exercises/m14-framework/pom.xml test allure:serve`.
hint: |
  Тексты шагов CartClient: «Очистить корзину», «Добавить в корзину товар {productId} × {quantity}», «Получить корзину»;
  OrderClient: «Оформить заказ с промокодом {promoCode}» и т.д. `create` можно построить на `tryCreate(...).statusCode(201).extract().as(OrderDto.class)`.
```

```quiz
- q: Что произойдёт, если не подключить агент AspectJ?
  options: ["@Step и @Attachment молча не будут работать", "Тесты не запустятся", "Отчёт не построится"]
  answer: 0
- q: Где лежат результаты Allure после прогона?
  options: ["В папке allure-results (у нас target/allure-results) — JSON-файлы", "Сразу HTML в target", "В базе данных"]
  answer: 0
- q: Почему отчёт открывают через allure:serve, а не двойным кликом по index.html?
  options: ["Браузер запрещает странице грузить данные с диска", "index.html не существует", "Так быстрее"]
  answer: 0
- q: Как в шаге показать значение параметра метода?
  options: ['@Step("Оплатить заказ {id}")', '@Step("Оплатить заказ " + id)', "Никак"]
  answer: 0
- q: Что делает фильтр AllureRestAssured?
  options: ["Прикладывает в отчёт каждый запрос и ответ", "Фильтрует тесты", "Ускоряет запросы"]
  answer: 0
```
