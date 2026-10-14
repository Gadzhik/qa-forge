---
title: "7.8 TestNG"
minutes: 40
---

**TestNG** — второй по популярности фреймворк тестов в Java. В новых проектах чаще выбирают JUnit 5, но TestNG по-прежнему встречается во многих компаниях (особенно в старых проектах UI-автоматизации), и его спрашивают на собеседованиях. Хорошая новость: идеи те же, отличаются названия.

## Подключение

```xml
<dependency>
    <groupId>org.testng</groupId>
    <artifactId>testng</artifactId>
    <version>7.12.0</version>
    <scope>test</scope>
</dependency>
```

Maven Surefire сам запускает TestNG-тесты, если в проекте **нет** JUnit 5 (или если подключён движок `testng-engine` для JUnit Platform). В нашем модуле `m07-junit` есть оба фреймворка — TestNG-тест удобно запускать из IDEA (▶ рядом с классом).

## Аннотации: JUnit 5 vs TestNG

| Смысл | JUnit 5 | TestNG |
|---|---|---|
| Тест | `@Test` (org.junit.jupiter.api) | `@Test` (org.testng.annotations) |
| Перед каждым тестом | `@BeforeEach` | `@BeforeMethod` |
| После каждого | `@AfterEach` | `@AfterMethod` |
| Один раз перед всеми тестами класса | `@BeforeAll` (static) | `@BeforeClass` (не static!) |
| После всех | `@AfterAll` | `@AfterClass` |
| Перед всем набором | — | `@BeforeSuite`, `@BeforeTest` |
| Отключить | `@Disabled` | `@Test(enabled = false)` |
| Группы | `@Tag("smoke")` | `@Test(groups = "smoke")` |
| Данные для теста | `@ParameterizedTest` + источник | `@DataProvider` |
| Таймаут | `@Timeout(5)` | `@Test(timeOut = 5000)` |
| Ожидаемое исключение | `assertThrows` | `@Test(expectedExceptions = X.class)` |
| Зависимость тестов | — (нет и не надо) | `@Test(dependsOnMethods = "login")` |
| Перезапуск | расширения / surefire | `IRetryAnalyzer` |
| Порядок | `@Order` | `@Test(priority = 1)` |

> ⚠️ **Импорты!** Самая частая ошибка: смешать `org.junit.jupiter.api.Test` и `org.testng.annotations.Test` или `Assertions` из JUnit и `Assert` из TestNG. Тест «не запускается» или ведёт себя странно. В одном классе — один фреймворк.

> ⚠️ **Порядок аргументов в TestNG `Assert.assertEquals` — наоборот: сначала ФАКТ, потом ОЖИДАНИЕ.** `assertEquals(actual, expected)`. С AssertJ (`assertThat(actual).isEqualTo(expected)`) эта путаница исчезает — его можно использовать и с TestNG.

## DataProvider

Аналог параметризованных тестов:

```java
public class PasswordTestNgTest {

    @DataProvider(name = "passwords")
    public Object[][] passwords() {
        return new Object[][]{
                {"abcde1", true},
                {"abcd1", false},
                {null, false}
        };
    }

    @Test(dataProvider = "passwords", groups = "regression")
    public void validation(String password, boolean expected) {
        assertEquals(PasswordValidator.isValid(password), expected, "пароль: " + password);
    }
}
```

- `@DataProvider(name = "...")` — метод, возвращающий **двумерный массив** `Object[][]`: строка — один запуск, столбцы — аргументы;
- `@Test(dataProvider = "passwords")` — связь теста с провайдером по имени;
- `groups = "regression"` — группа (тег);
- методы и класс в TestNG обычно `public`.

`@DataProvider(parallel = true)` — запускать наборы данных параллельно.

## testng.xml — описание набора

Особенность TestNG: тесты можно собирать в наборы XML-файлом:

```xml
<!DOCTYPE suite SYSTEM "https://testng.org/testng-1.0.dtd">
<suite name="QA Burger regression" parallel="classes" thread-count="4">
    <test name="API">
        <groups>
            <run>
                <include name="regression"/>
                <exclude name="slow"/>
            </run>
        </groups>
        <packages>
            <package name="ru.qacourse.tests.api.*"/>
        </packages>
    </test>
</suite>
```

- `<suite>` — весь прогон; `parallel="classes" thread-count="4"` — параллельно по классам в 4 потока;
- `<test>` — логическая часть прогона;
- `<groups>` — какие группы включить и исключить;
- `<packages>`/`<classes>` — где искать тесты.

Запуск из Maven:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <suiteXmlFiles>
            <suiteXmlFile>src/test/resources/testng.xml</suiteXmlFile>
        </suiteXmlFiles>
    </configuration>
</plugin>
```

или `./mvnw test -Dgroups=smoke` (свойство `groups` surefire понимает и для TestNG). Gradle: `test { useTestNG { includeGroups 'smoke' } }`.

После прогона TestNG создаёт `test-output/testng-failed.xml` — набор из упавших тестов для перезапуска (урок 7.7).

## Что выбрать

- Новый проект — **JUnit 5**: активнее развивается, богатая экосистема расширений, параметризация гибче.
- Существующий проект на TestNG — работайте на TestNG, переписывать ради моды не нужно.
- Allure, Selenide, REST Assured, AssertJ одинаково работают с обоими.

```task
id: m07-t08
title: TestNG и DataProvider
module: exercises/m07-junit
test: PasswordTestNgTestCheck
file: exercises/m07-junit/src/test/java/ru/qacourse/m07/tasks/PasswordTestNgTest.java
solution: solutions/exercises/m07-junit/src/test/java/ru/qacourse/m07/tasks/PasswordTestNgTest.java
text: |
  Напишите TestNG-версию тестов пароля: `@DataProvider` с парами (пароль, ожидание) и один `@Test(dataProvider = ..., groups = "regression")`.
  Должны быть пойманы те же три ошибки границ, что в задании 7.4.
hint: |
  Импорты только из `org.testng.annotations` и `org.testng.Assert`. Порядок в `Assert.assertEquals(actual, expected)`.
```

```quiz
- q: Аналог @BeforeEach в TestNG?
  options: ["@BeforeMethod", "@BeforeClass", "@BeforeSuite"]
  answer: 0
- q: Что возвращает метод @DataProvider?
  options: ["Object[][] — строки запусков и столбцы аргументов", "List<String>", "void"]
  answer: 0
- q: В каком порядке аргументы Assert.assertEquals в TestNG?
  options: ["Факт, затем ожидание", "Ожидание, затем факт"]
  answer: 0
- q: Как пометить тест группой в TestNG?
  options: ['@Test(groups = "smoke")', '@Tag("smoke")', '@Group("smoke")']
  answer: 0
- q: Что создаёт TestNG для перезапуска упавших?
  options: [test-output/testng-failed.xml, failed.txt, Ничего]
  answer: 0
```
