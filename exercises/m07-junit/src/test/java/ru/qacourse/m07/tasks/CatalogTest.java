package ru.qacourse.m07.tasks;

import org.junit.jupiter.api.Test;
import ru.qacourse.m07.sut.Catalog;

import static org.assertj.core.api.Assertions.*;

/**
 * Задание 7.3: AssertJ. Протестируйте Catalog.search (требования — в комментарии к классу Catalog).
 * Используйте проверки AssertJ для коллекций: extracting, containsExactly, isSortedAccordingTo, hasSize...
 * Нужно поймать три ошибки: поиск с учётом регистра, неверная сортировка, потеря последнего элемента.
 */
public class CatalogTest {

    private final Catalog catalog = new Catalog();

    @Test
    void findsSomething() {
        assertThat(catalog.search("бургер")).isNotEmpty();
    }
}
