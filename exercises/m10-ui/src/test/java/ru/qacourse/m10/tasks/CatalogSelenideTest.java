package ru.qacourse.m10.tasks;

import com.codeborne.selenide.Configuration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.qacourse.support.ShopConfig;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.CollectionCondition.*;
import static com.codeborne.selenide.Selenide.*;
import static org.assertj.core.api.Assertions.fail;

import com.codeborne.selenide.ElementsCollection;

import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Задание 10.11: коллекции элементов в Selenide.
 * 1) Выбрать категорию «Напитки» в [data-testid=category-select], нажать «Найти» →
 *    у всех карточек — напитки (5 штук), среди названий есть «Кола 0.5».
 * 2) Выбрать сортировку «Сначала дорогие» в [data-testid=sort-select] → цены [data-testid=product-price]
 *    идут по убыванию (преобразуйте тексты «289.90 ₽» в числа).
 * 3) Дизайн v2 (?design=v2): карточек столько же (20), локаторы по data-testid работают.
 */
public class CatalogSelenideTest {
    @Test
    void catalog() {
        fail("TODO: напишите тест");
    }
}
