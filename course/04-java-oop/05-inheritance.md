---
title: "4.5 Наследование"
minutes: 35
---

## Идея

В QA Burger есть пользователи и администраторы. Администратор — это **тоже пользователь** (у него есть имя, он может войти и сделать заказ), но с дополнительными возможностями. Писать класс `Admin` с нуля, копируя всё из `User`, — плохо: дублирование кода.

**Наследование** позволяет создать класс на основе существующего: новый класс получает все поля и методы родителя и может добавить свои или изменить унаследованные.

```java
public class User {
    protected final String username;

    public User(String username) {
        this.username = username;
    }

    public boolean canDeleteProducts() {
        return false;
    }

    public String describe() {
        return "Пользователь " + username;
    }
}
```

```java
public class Admin extends User {

    public Admin(String username) {
        super(username);
    }

    @Override
    public boolean canDeleteProducts() {
        return true;
    }

    @Override
    public String describe() {
        return super.describe() + " (администратор)";
    }
}
```

## Разбор

- **`extends User`** — «расширяет User»: `Admin` — **наследник** (подкласс, дочерний класс), `User` — **родитель** (суперкласс). У класса может быть **только один** родитель.
- **`super(username)`** — вызов **конструктора родителя**. Конструкторы не наследуются; дочерний конструктор обязан первой строкой вызвать конструктор родителя, чтобы тот заполнил свои поля. Если у родителя есть конструктор без параметров, Java вызовет его автоматически; у `User` его нет — значит, `super(...)` обязателен.
- **`@Override public boolean canDeleteProducts()`** — **переопределение**: у администратора своя версия метода.
- **`super.describe()`** — вызов **родительской** версии метода. Так мы не дублируем «Пользователь boss», а дополняем: результат — «Пользователь boss (администратор)».
- **`protected`** у поля `username` — доступно наследникам. При `private` класс `Admin` не мог бы обратиться к `username` напрямую (только через `getUsername()`).

## Отношение «является» (is-a)

Наследование уместно, только когда наследник **является** разновидностью родителя: администратор **является** пользователем. Кот является животным. А корзина **не является** списком товаров — она **содержит** список. Для «содержит» используют поле (**композицию**), а не наследование.

> 💡 Правило: «предпочитайте композицию наследованию». Наследование создаёт сильную связь: изменение родителя ломает всех наследников.

## Цепочка до Object

Если `extends` не написан, класс наследуется от `Object`. Поэтому у любого объекта есть `toString`, `equals`, `hashCode`. Цепочка для `Admin`: `Admin → User → Object`.

## abstract — класс-заготовка

Иногда родитель — это общая идея, а не что-то, что можно создать:

```java
public abstract class BaseTest {
    protected String baseUrl = "http://localhost:8081";

    protected abstract String endpoint();   // наследник обязан реализовать

    protected String url() {
        return baseUrl + endpoint();
    }
}

public class ProductsTest extends BaseTest {
    @Override
    protected String endpoint() {
        return "/api/products";
    }
}
```

- `abstract class` — нельзя создать через `new BaseTest()`, только наследовать;
- `abstract` метод — без тела; каждый наследник обязан его реализовать.

## Наследование в автотестах

Самый частый пример — **базовый тестовый класс**:

```java
public abstract class BaseApiTest {
    @BeforeAll
    static void setUp() {
        RestAssured.baseURI = Config.baseUrl();   // общая настройка для всех API-тестов
    }
}

class CartTest extends BaseApiTest { ... }
class OrderTest extends BaseApiTest { ... }
```

Все тест-классы получают общую подготовку. Тот же приём — `BasePage` для Page Object в модуле 10.

> ⚠️ Не увлекайтесь глубокими иерархиями `BaseTest → BaseApiTest → BaseAuthApiTest → ...`. Через полгода никто не поймёт, откуда берётся настройка. Две ступени — обычно максимум.

## final для классов и методов

- `final class` — от класса нельзя наследоваться (так устроен `String`);
- `final` метод — нельзя переопределить.

```task
id: m04-t05
title: Администратор
module: exercises/m04-java-oop
test: AdminCheck
file: exercises/m04-java-oop/src/main/java/ru/qacourse/m04/Admin.java
solution: solutions/exercises/m04-java-oop/src/main/java/ru/qacourse/m04/Admin.java
text: |
  В классе `Admin` (наследник `User`) переопределите:
  - `canDeleteProducts()` — вернуть `true`;
  - `describe()` — описание родителя + `" (администратор)"`.
hint: |
  Не забудьте `@Override`. Родительскую версию вызывайте через `super.describe()`.
```

```quiz
- q: Сколько родителей может быть у класса в Java?
  options: ["Один", "Два", "Сколько угодно"]
  answer: 0
- q: Что делает super(username) в конструкторе наследника?
  options:
    - Вызывает конструктор родителя
    - Вызывает метод username
    - Создаёт новый объект родителя отдельно
  answer: 0
- q: Когда наследование уместно?
  options:
    - Когда наследник является разновидностью родителя (is-a)
    - Когда нужно переиспользовать любой код
    - Всегда
  answer: 0
- q: Можно ли создать объект абстрактного класса через new?
  options: ["Да", "Нет"]
  answer: 1
- q: Какой модификатор даёт доступ к полю наследникам, но не всем?
  options: [private, protected, public]
  answer: 1
```
