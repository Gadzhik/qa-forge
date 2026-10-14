package ru.qacourse.m07.tasks;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import ru.qacourse.m07.sut.Cart;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Задание 7.5: теги. Пометьте тест newCartIsEmpty тегом "smoke",
 * а totalWithPromo — тегами "regression" и "promo".
 * Затем запустите из терминала только smoke: ./mvnw -f exercises/m07-junit/pom.xml test -Dgroups=smoke -Dtest=TaggedTest
 */
public class TaggedTest {

    @Test
    @Tag("smoke")
    void newCartIsEmpty() {
        assertTrue(new Cart().isEmpty());
    }

    @Test
    @Tag("regression")
    @Tag("promo")
    void totalWithPromo() {
        Cart cart = new Cart();
        cart.add("Бургер", "100.00", 1);
        cart.applyPromo("SALE10");
        assertEquals("90.00", cart.total().toPlainString());
    }
}
