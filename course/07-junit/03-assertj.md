---
title: "7.3 AssertJ и мягкие проверки"
minutes: 40
---

Проверки JUnit (`assertEquals`, `assertTrue`) работают, но быстро становятся неудобными. Сравните:

```java
// JUnit
assertTrue(names.contains("Кола") && names.size() == 3);
// упало: expected: <true> but was: <false>  — и что не так?

// AssertJ
assertThat(names).hasSize(3).contains("Кола");
// упало: Expected size: 3 but was: 2 in: ["Фри", "Бургер"]
```

**AssertJ** — библиотека «текучих» (fluent) проверок. Это стандарт де-факто в проектах автотестов на Java.

## Основа

```java
import static org.assertj.core.api.Assertions.*;

assertThat(факт).проверка(ожидание).ещё_проверка(...);
```

- всё начинается с `assertThat(факт)` — путаницы «где ожидание, где факт» нет;
- дальше IDEA сама подсказывает доступные проверки (Ctrl+Space) — набор зависит от типа значения;
- проверки цепляются через точку.

## Самые нужные проверки

**Любые объекты:**

```java
assertThat(order).isNotNull();
assertThat(status).isEqualTo("PAID");
assertThat(status).isIn("PAID", "COOKING", "READY");
assertThat(product).isInstanceOf(ProductDto.class);
```

**Строки:**

```java
assertThat(message)
        .isNotBlank()
        .startsWith("Количество")
        .contains("от 1 до 99")
        .doesNotContain("null")
        .matches("Количество .* \\d+")
        .isEqualToIgnoringCase("количество должно быть от 1 до 99");
```

**Числа:**

```java
assertThat(quantity).isPositive().isBetween(1, 99);
assertThat(total).isGreaterThan(0).isLessThanOrEqualTo(1000);
assertThat(price).isCloseTo(289.9, within(0.001));            // для double
assertThat(new BigDecimal("289.90")).isEqualByComparingTo("289.9"); // для BigDecimal
```

**Коллекции** — самое мощное:

```java
assertThat(names).hasSize(3);
assertThat(names).isEmpty();                                   // / isNotEmpty()
assertThat(names).contains("Кола");                            // есть (порядок и остальные — неважно)
assertThat(names).containsExactly("Кола", "Фри", "Бургер");    // ровно эти, в этом порядке
assertThat(names).containsExactlyInAnyOrder("Фри", "Кола", "Бургер");
assertThat(names).containsOnly("Кола", "Фри");                 // только эти (повторы допустимы)
assertThat(names).doesNotContain("Пиво");
assertThat(names).doesNotHaveDuplicates();
assertThat(prices).isSorted();                                 // по возрастанию
assertThat(prices).isSortedAccordingTo(Comparator.reverseOrder()); // по убыванию
assertThat(products).allMatch(p -> p.price() > 0);
assertThat(products).anySatisfy(p -> assertThat(p.category()).isEqualTo("DRINK"));
```

**extracting** — достать поле у каждого элемента и проверить список значений:

```java
assertThat(catalog.search("бургер"))
        .extracting(Catalog.Product::name)
        .containsExactlyInAnyOrder("Классический бургер", "Чизбургер двойной", "Бургер Острый");

assertThat(catalog.search("бургер"))
        .extracting(Catalog.Product::price)
        .containsExactly(379.9, 339.5, 289.9);
```

Без `extracting` пришлось бы писать стрим `.stream().map(...).toList()` в каждом тесте.

**Map:**

```java
assertThat(stock).containsEntry("Кола", 200).containsKeys("Фри").hasSize(2);
```

**Исключения:**

```java
assertThatThrownBy(() -> cart.add("Кола", "99", 0))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Количество должно быть от 1 до 99");

assertThatCode(() -> cart.add("Кола", "99", 1)).doesNotThrowAnyException();
```

## Описание проверки: as()

```java
assertThat(cart.totalQuantity())
        .as("количество после добавления 2 + 1 бургеров")
        .isEqualTo(3);
```

`as(...)` (вызывать **до** проверки!) добавляет описание в сообщение об ошибке: `[количество после добавления 2 + 1 бургеров] expected: 3 but was: 1`. В длинных тестах очень помогает.

## Сравнение объектов по полям

```java
assertThat(actualOrder)
        .usingRecursiveComparison()
        .ignoringFields("id", "createdAt")
        .isEqualTo(expectedOrder);
```

Сравнить два объекта поле за полем, игнорируя то, что каждый раз разное (id, время). Удобно для ответов API.

## Мягкие проверки (Soft Assertions)

Обычная проверка останавливает тест на первой ошибке. Если в ответе неверны и статус, и сумма, и скидка — вы узнаете только о первом, исправите, перезапустите, узнаете о втором... **Мягкие проверки** собирают все ошибки и сообщают о них разом в конце:

```java
@Test
void orderFields() {
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(order.status()).isEqualTo("NEW");
    softly.assertThat(order.total()).isEqualByComparingTo("521.82");
    softly.assertThat(order.promoCode()).isEqualTo("SALE10");
    softly.assertAll();          // ← без этой строки ошибки потеряются!
}
```

Короче, без риска забыть `assertAll`:

```java
SoftAssertions.assertSoftly(softly -> {
    softly.assertThat(order.status()).isEqualTo("NEW");
    softly.assertThat(order.total()).isEqualByComparingTo("521.82");
});
```

Или через расширение JUnit:

```java
@ExtendWith(SoftAssertionsExtension.class)
class OrderTest {
    @InjectSoftAssertions
    SoftAssertions softly;

    @Test
    void fields() {
        softly.assertThat(...)...;   // assertAll вызовется автоматически
    }
}
```

> 💡 Когда мягкие проверки уместны: проверка нескольких полей **одного** результата (ответа API, формы). Когда нет: если следующие шаги зависят от предыдущей проверки (нет смысла проверять сумму заказа, если заказ не создался).

```task
id: m07-t03
title: AssertJ для коллекций
module: exercises/m07-junit
test: CatalogTestCheck
file: exercises/m07-junit/src/test/java/ru/qacourse/m07/tasks/CatalogTest.java
solution: solutions/exercises/m07-junit/src/test/java/ru/qacourse/m07/tasks/CatalogTest.java
text: |
  Протестируйте `Catalog.search` с помощью AssertJ. Нужно поймать три ошибки:
  поиск с учётом регистра, сортировку по возрастанию вместо убывания и потерю последнего найденного товара.
hint: |
  Поиск по слову «бургер» (с маленькой буквы) должен найти ровно 3 товара — проверьте `extracting(Catalog.Product::name)` + `containsExactlyInAnyOrder`.
  Порядок цен — `extracting(Catalog.Product::price).containsExactly(379.9, 339.5, 289.9)`.
```

```quiz
- q: Чем containsExactly отличается от contains?
  options:
    - containsExactly требует ровно эти элементы в этом порядке, contains — что они просто есть
    - Ничем
    - contains проверяет порядок
  answer: 0
- q: Что делает extracting(Product::name)?
  options:
    - Превращает список товаров в список их названий для дальнейшей проверки
    - Удаляет поле name
    - Сортирует по названию
  answer: 0
- q: Что произойдёт, если забыть softly.assertAll()?
  options: [Ошибки мягких проверок будут потеряны, Тест упадёт, Ничего]
  answer: 0
- q: Где вызывать as("описание")?
  options: [До проверки, После проверки, Где угодно]
  answer: 0
- q: Как сравнить два BigDecimal без учёта масштаба (289.9 и 289.90)?
  options: [isEqualByComparingTo, isEqualTo, isSameAs]
  answer: 0
```
