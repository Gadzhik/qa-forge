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

    // TODO: тест на total() для товара с количеством больше 1
    // TODO: тест на повторное добавление того же товара
    // TODO: тест на исключение при количестве 0
}
