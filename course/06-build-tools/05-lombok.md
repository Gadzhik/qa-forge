---
title: "6.5 Lombok — меньше шаблонного кода"
minutes: 30
---

## Проблема

Класс-модель «покупатель» для тестовых данных: 4 поля. А к ним — конструктор, 4 геттера, 4 сеттера, `equals`, `hashCode`, `toString`. Около 70 строк кода, которые ничего интересного не делают. Таких классов в проекте автотестов десятки (модели запросов и ответов API).

**Lombok** — библиотека, которая **генерирует этот код при компиляции** по аннотациям. В исходнике остаются только поля.

## Подключение

**Maven** (сделано в `m06-build-tools/pom.xml`):

```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <scope>provided</scope>
</dependency>
```

и в `maven-compiler-plugin`:

```xml
<annotationProcessorPaths>
    <path>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <version>${lombok.version}</version>
    </path>
</annotationProcessorPaths>
```

- `provided` — Lombok нужен только при компиляции, в итоговую программу не попадает;
- `annotationProcessorPaths` — явно говорим компилятору запускать Lombok как **обработчик аннотаций** (annotation processor). В новых версиях JDK автоматический поиск обработчиков выключен, без этой настройки аннотации Lombok молча проигнорируются и вы получите `cannot find symbol: method getName()`.

**Gradle:**

```groovy
compileOnly 'org.projectlombok:lombok:1.18.48'
annotationProcessor 'org.projectlombok:lombok:1.18.48'
testCompileOnly 'org.projectlombok:lombok:1.18.48'
testAnnotationProcessor 'org.projectlombok:lombok:1.18.48'
```

**IDEA:** поддержка Lombok встроена. Если IDEA подчёркивает сгенерированные методы красным — **Settings → Build → Compiler → Annotation Processors → Enable annotation processing**.

## Главные аннотации

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {
    private String name;
    private String email;
    private String phone;
    private boolean vip;
}
```

| Аннотация | Что генерирует |
|---|---|
| `@Getter` / `@Setter` | Геттеры / сеттеры (на класс — для всех полей, на поле — для одного) |
| `@ToString` | `toString()` со всеми полями |
| `@EqualsAndHashCode` | `equals` и `hashCode` по полям |
| `@Data` | Всё сразу: `@Getter`, `@Setter`, `@ToString`, `@EqualsAndHashCode`, конструктор для `final` полей |
| `@NoArgsConstructor` | Конструктор без параметров (нужен Jackson для чтения JSON) |
| `@AllArgsConstructor` | Конструктор со всеми полями |
| `@RequiredArgsConstructor` | Конструктор для `final` полей |
| `@Builder` | Билдер: `Customer.builder().name("Анна").vip(true).build()` |
| `@Value` | Неизменяемый класс: все поля `private final`, только геттеры (похоже на record) |
| `@Slf4j` | Поле `log` для логирования |

Для поля `boolean vip` геттер называется `isVip()`, а не `getVip()` — по соглашению JavaBeans.

## Builder — самое полезное для тестов

Паттерн **Builder** позволяет собирать объект по частям, указывая только нужные поля:

```java
Customer anna = Customer.builder()
        .name("Анна")
        .email("anna@test.local")
        .vip(true)
        .build();
```

- `Customer.builder()` — получить «строителя»;
- каждый метод `.name(...)`, `.email(...)` задаёт поле и возвращает того же строителя — поэтому вызовы цепочкой;
- `.build()` — создать объект.

Не указанные поля получат значения по умолчанию (`null`, `false`). Если нужно своё значение по умолчанию — `@Builder.Default private boolean vip = false;`.

Почему это удобно в тестах: тестовые данные читаются как текст, а для каждого теста легко сделать вариацию: «тот же пользователь, но без email».

```java
Customer noEmail = anna.toBuilder().email(null).build();   // нужен @Builder(toBuilder = true)
```

## Как увидеть, что сгенерировал Lombok

- В IDEA: **Structure** (Alt+7 / ⌘7) — в структуре класса видны все сгенерированные методы.
- Правый клик по аннотации → **Refactor → Delombok** — показать код без Lombok.

## Минусы и альтернатива

- «Магия»: в исходнике методов не видно, новички путаются.
- Нужна поддержка в IDE и настройка компилятора.
- Для неизменяемых моделей в современной Java есть **record** (урок 4.8) — без всякой библиотеки. Многие команды используют record для ответов API и Lombok `@Builder` для запросов.

```task
id: m06-t05
title: Lombok
module: exercises/m06-build-tools
test: CustomerCheck
file: exercises/m06-build-tools/src/main/java/ru/qacourse/m06/Customer.java
solution: solutions/exercises/m06-build-tools/src/main/java/ru/qacourse/m06/Customer.java
text: |
  Добавьте к классу `Customer` аннотации Lombok, чтобы появились: геттеры и сеттеры, `equals`/`hashCode`, `toString`,
  конструктор без параметров, конструктор со всеми полями и билдер. Код методов писать не нужно!
hint: |
  Четыре аннотации: `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder` (и их импорты из пакета `lombok`).
  Проверка использует reflection, чтобы скомпилироваться ещё до того, как методы появятся.
```

```quiz
- q: Когда Lombok генерирует код?
  options: [При компиляции, При запуске программы, При открытии файла в IDE]
  answer: 0
- q: Что включает @Data?
  options:
    - Геттеры, сеттеры, toString, equals/hashCode
    - Только геттеры
    - Билдер
  answer: 0
- q: Как будет называться геттер для поля boolean vip?
  options: [getVip(), isVip(), vip()]
  answer: 1
- q: Чем удобен @Builder для тестовых данных?
  options:
    - Можно задать только нужные поля, код читается как описание данных
    - Он ускоряет тесты
    - Без него нельзя создать объект
  answer: 0
- q: Почему у Lombok scope provided?
  options:
    - Он нужен только при компиляции и не попадает в итоговую программу
    - Так быстрее скачивается
    - Это ошибка
  answer: 0
```
