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
    @BeforeEach
    void setUp() {
        Configuration.baseUrl = ShopConfig.baseUrl();
    }

    @AfterEach
    void tearDown() {
        closeWebDriver();
    }

    @Test
    void filterDrinks() {
        open("/");
        $("[data-testid=category-select]").selectOption("Напитки");
        $("[data-testid=search-button]").click();
        ElementsCollection names = $$("[data-testid=product-name]");
        names.shouldHave(size(5)).shouldHave(itemWithText("Кола 0.5"));
        names.shouldHave(texts("Кола 0.5", "Лимонад домашний", "Морс клюквенный", "Кофе латте", "Вода без газа"));
    }

    @Test
    void sortByPriceDesc() {
        open("/");
        $("[data-testid=sort-select]").selectOption("Сначала дорогие");
        $("[data-testid=search-button]").click();
        $$("[data-testid=product-card]").shouldHave(size(20));
        List<Double> prices = $$("[data-testid=product-price]").texts().stream()
                .map(t -> Double.parseDouble(t.replace("₽", "").trim()))
                .toList();
        assertThat(prices).isSortedAccordingTo(Comparator.reverseOrder());
    }

    @Test
    void designV2KeepsTestIds() {
        open("/?design=v2");
        $$("[data-testid=product-card]").shouldHave(size(20));
        $("[data-testid=search-input]").shouldBe(visible);
        open("/?design=v1");
    }
}
