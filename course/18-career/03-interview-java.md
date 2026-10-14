---
title: "18.3 Собеседование: Java и live coding"
minutes: 50
---

## Вопросы по Java

<details class="qa"><summary>1. Что такое JDK, JRE, JVM?</summary>

**JVM** — виртуальная машина, исполняющая байт-код. **JRE** — JVM + стандартные библиотеки для запуска программ. **JDK** — JRE + инструменты разработчика (компилятор `javac`, `jar`, отладчик). Для написания тестов нужен JDK. (0.2)
</details>

<details class="qa"><summary>2. Примитивы и ссылочные типы. Чем int отличается от Integer?</summary>

8 примитивов: `byte, short, int, long, float, double, char, boolean` — хранят значение. Ссылочные типы хранят ссылку на объект. `Integer` — объект-обёртка: может быть `null`, используется в коллекциях; автоупаковка превращает одно в другое. Ловушка: `Integer` сравнивать через `equals`, а не `==` (кэш только −128…127). (3.3)
</details>

<details class="qa"><summary>3. Чем == отличается от equals()?</summary>

`==` для объектов сравнивает **ссылки** (тот же ли это объект), `equals()` — **содержимое** (если метод переопределён). Строки и обёртки всегда сравнивают через `equals`. (3.3)
</details>

<details class="qa"><summary>4. Контракт equals и hashCode.</summary>

Если `a.equals(b)`, то `a.hashCode() == b.hashCode()`. Обратное не обязательно. Переопределяют оба метода вместе — иначе объекты «теряются» в `HashMap`/`HashSet`. `record` генерирует их автоматически. (4.4)
</details>

<details class="qa"><summary>5. Почему String неизменяемый? Когда нужен StringBuilder?</summary>

Неизменяемость даёт безопасность (строку нельзя подменить после проверки), потокобезопасность и пул строк. Любая «модификация» создаёт новую строку, поэтому при склейке в цикле используют `StringBuilder`. (3.5)
</details>

<details class="qa"><summary>6. Принципы ООП.</summary>

**Инкапсуляция** — скрыть данные, дать методы (private поля + геттеры). **Наследование** — класс расширяет другой (`extends`). **Полиморфизм** — один интерфейс, разные реализации; вызывается метод фактического объекта. **Абстракция** — выделить существенное (интерфейсы, абстрактные классы). Пример из автотестов: `BasePage` → страницы, интерфейс `Browser` с разными реализациями. (модуль 4)
</details>

<details class="qa"><summary>7. Абстрактный класс vs интерфейс.</summary>

Интерфейс — контракт, класс может реализовать много интерфейсов; полей-состояния нет (только константы), есть `default`-методы. Абстрактный класс может хранить состояние, иметь конструктор, наследуется только один. (4.5, 4.6)
</details>

<details class="qa"><summary>8. Перегрузка и переопределение.</summary>

**Перегрузка** (overload) — методы с одним именем и разными параметрами в одном классе, выбор при компиляции. **Переопределение** (override) — подкласс заменяет реализацию метода родителя с той же сигнатурой, выбор во время выполнения. `@Override` защищает от опечаток.
</details>

<details class="qa"><summary>9. Модификаторы доступа и static, final.</summary>

`private` → только класс; без модификатора → пакет; `protected` → пакет + наследники; `public` → все. `static` — принадлежит классу, а не объекту. `final`: переменная — нельзя переприсвоить, метод — нельзя переопределить, класс — нельзя наследовать.
</details>

<details class="qa"><summary>10. Иерархия коллекций. ArrayList vs LinkedList, HashMap vs TreeMap.</summary>

`Collection` → `List` (порядок, дубли), `Set` (без дублей), `Queue`; отдельно `Map` (ключ-значение). `ArrayList` — массив внутри, быстрый доступ по индексу; `LinkedList` — связный список, на практике почти не нужен. `HashMap` — без порядка, O(1); `LinkedHashMap` — порядок вставки; `TreeMap` — отсортирован по ключу. (5.2, 5.3)
</details>

<details class="qa"><summary>11. Как устроен HashMap?</summary>

Массив «корзин». По `hashCode()` ключа вычисляется корзина, внутри — список (при большом числе коллизий — дерево), ключ ищется через `equals()`. При заполнении массив увеличивается вдвое (rehash). Отсюда важность корректных `equals/hashCode`.
</details>

<details class="qa"><summary>12. Исключения: checked и unchecked. try-with-resources.</summary>

`Throwable` → `Error` (сбои JVM, не ловим) и `Exception`. **Checked** (`IOException`) обязательно обработать или объявить в `throws`. **Unchecked** (`RuntimeException`: NPE, IllegalArgument) — не обязательно. `try (var conn = ds.getConnection()) {...}` автоматически закрывает ресурс. Ошибка проверки в тесте — `AssertionError` (это `Error`!). (5.1)
</details>

<details class="qa"><summary>13. Что такое generics и зачем?</summary>

Параметризация типов: `List<Product>` — компилятор не даст положить туда строку и не нужно приводить тип. Пример в тестах: `response.as(Product.class)`, `<T> T get(String path, Class<T> type)`. (4.9)
</details>

<details class="qa"><summary>14. Stream API: промежуточные и терминальные операции.</summary>

Промежуточные (`filter`, `map`, `sorted`, `distinct`) ленивые и возвращают поток. Терминальные (`collect`, `toList`, `count`, `anyMatch`, `findFirst`) запускают вычисление. Поток одноразовый. (5.5, 5.6)
</details>

<details class="qa"><summary>15. Optional — зачем?</summary>

Явно показывает «значения может не быть» вместо `null`. `orElse`, `orElseThrow`, `map`, `ifPresent`. Не используют как поле класса и параметр метода. (5.7)
</details>

<details class="qa"><summary>16. Что такое потокобезопасность? Как она связана с параллельными тестами?</summary>

Код корректен при одновременном вызове из нескольких потоков. Изменяемые `static`-поля (общий WebDriver, общий токен) ломают параллельные тесты — решение: `ThreadLocal`, отсутствие общего состояния, потокобезопасные коллекции (`ConcurrentHashMap`). (5.10, 14.4)
</details>

<details class="qa"><summary>17. Что такое record? Lombok?</summary>

`record Product(long id, String name)` — неизменяемый класс данных с конструктором, геттерами, `equals/hashCode/toString`. Lombok генерирует подобное аннотациями (`@Data`, `@Builder`) для обычных классов. (4.8, 6.5)
</details>

## Live coding

Типичные задачи — 15–30 минут. Говорите **вслух**: уточните условие, предложите решение, напишите, проверьте на примерах (включая пустой ввод и `null`). Ниже — решения; сначала попробуйте сами в `exercises/m05-java-advanced` (создайте класс в `src/test/java` и проверьте JUnit-тестом).

<details class="qa"><summary>Задача 1. Перевернуть строку и проверить палиндром</summary>

```java
static boolean isPalindrome(String s) {
    String clean = s.toLowerCase().replaceAll("[^\\p{L}\\p{N}]", ""); // оставляем только буквы и цифры: «А роза упала...» тоже палиндром
    return new StringBuilder(clean).reverse().toString().equals(clean); // StringBuilder.reverse — готовый разворот
}
```
Вопрос интервьюера: «а без StringBuilder?» — два индекса с краёв, идём навстречу, сравниваем символы.
</details>

<details class="qa"><summary>Задача 2. Посчитать частоту слов в тексте</summary>

```java
static Map<String, Long> wordFrequency(String text) {
    return Arrays.stream(text.toLowerCase().split("\\W+"))        // разбиваем по всему, что не буква/цифра
            .filter(w -> !w.isBlank())                            // пустые строки (в начале текста) отбрасываем
            .collect(Collectors.groupingBy(w -> w, Collectors.counting())); // слово → сколько раз
}
```
Для кириллицы `\\W` не подойдёт — используйте `[^\\p{L}\\p{N}]+`.
</details>

<details class="qa"><summary>Задача 3. Найти дубликаты в списке</summary>

```java
static Set<Integer> duplicates(List<Integer> list) {
    Set<Integer> seen = new HashSet<>();        // уже встречавшиеся
    Set<Integer> dups = new LinkedHashSet<>();  // дубликаты в порядке обнаружения
    for (Integer x : list) {
        if (!seen.add(x)) {                     // add возвращает false, если элемент уже был
            dups.add(x);
        }
    }
    return dups;
}
```
Сложность O(n) — проговорите это.
</details>

<details class="qa"><summary>Задача 4. FizzBuzz</summary>

```java
for (int i = 1; i <= 100; i++) {
    if (i % 15 == 0) System.out.println("FizzBuzz");  // сначала проверка на 15, иначе сработает ветка 3 или 5
    else if (i % 3 == 0) System.out.println("Fizz");
    else if (i % 5 == 0) System.out.println("Buzz");
    else System.out.println(i);
}
```
</details>

<details class="qa"><summary>Задача 5. Проверить скобки ({[]})</summary>

```java
static boolean balanced(String s) {
    Deque<Character> stack = new ArrayDeque<>();      // стек открытых скобок
    Map<Character, Character> pairs = Map.of(')', '(', ']', '[', '}', '{');
    for (char c : s.toCharArray()) {
        if (pairs.containsValue(c)) stack.push(c);    // открывающая — кладём
        else if (pairs.containsKey(c)) {              // закрывающая — должна совпасть с верхней
            if (stack.isEmpty() || stack.pop() != pairs.get(c)) return false;
        }
    }
    return stack.isEmpty();                           // не осталось незакрытых
}
```
</details>

<details class="qa"><summary>Задача 6. Stream: самые дорогие товары по категориям</summary>

```java
record Product(String name, String category, BigDecimal price) {}

static Map<String, Optional<Product>> mostExpensive(List<Product> products) {
    return products.stream()
            .collect(Collectors.groupingBy(Product::category,                        // группируем по категории
                    Collectors.maxBy(Comparator.comparing(Product::price))));        // в группе — максимум по цене
}
```
</details>

<details class="qa"><summary>Задача 7. Написать автотест на ходу</summary>

«Напишите тест: POST /api/auth/login с неверным паролем возвращает 401 и поле code = BAD_CREDENTIALS».

```java
@Test
void loginWithWrongPasswordReturns401() {
    given()
        .contentType(ContentType.JSON)
        .body(Map.of("username", "user", "password", "wrong"))  // Map сериализуется Jackson в JSON
    .when()
        .post("/api/auth/login")
    .then()
        .statusCode(401)
        .body("code", equalTo("BAD_CREDENTIALS"));
}
```
Проговорите, какие ещё проверки добавили бы: пустой пароль (400), несуществующий пользователь, блокировка после N попыток, токен не выдаётся. (модуль 9)
</details>

```quiz
- q: Чем == отличается от equals для строк?
  options: ["== сравнивает ссылки, equals — содержимое", "Ничем", "equals сравнивает ссылки"]
  answer: 0
- q: Что будет, если переопределить equals без hashCode?
  options: ["Объекты будут некорректно работать в HashMap/HashSet", "Ошибка компиляции", "Ничего"]
  answer: 0
- q: Какая операция Stream терминальная?
  options: ["collect", "filter", "map"]
  answer: 0
- q: Какой тип исключения — AssertionError?
  options: ["Error (unchecked)", "Checked Exception", "RuntimeException"]
  answer: 0
```
