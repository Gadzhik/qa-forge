package ru.qacourse.m07.tasks;

import org.junit.jupiter.api.Test;
import ru.qacourse.m07.sut.Cart;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Задание 7.1: первые тесты. Напишите тесты для Cart (требования — в комментарии к классу Cart).
 * Используйте пока только assertEquals / assertTrue / assertThrows из JUnit.
 */
public class CartTest {

    @Test
    void newCartIsEmpty() {
        Cart cart = new Cart();
        assertTrue(cart.isEmpty());
    }

    @Test
    void totalIsPriceTimesQuantity() {
        Cart cart = new Cart();
        cart.add("Кола", "99.00", 3);
        assertEquals(new BigDecimal("297.00"), cart.total());
    }

    @Test
    void sameItemQuantitiesAreSummed() {
        Cart cart = new Cart();
        cart.add("Бургер", "289.90", 2);
        cart.add("Бургер", "289.90", 1);
        assertEquals(3, cart.quantityOf("Бургер"));
    }

    @Test
    void zeroQuantityIsRejected() {
        Cart cart = new Cart();
        assertThrows(IllegalArgumentException.class, () -> cart.add("Кола", "99.00", 0));
    }
}
