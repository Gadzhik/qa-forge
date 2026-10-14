package ru.qacourse.m07.tasks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import ru.qacourse.m07.sut.Cart;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Задание 7.2: жизненный цикл тестов.
 * - создавайте новую корзину в методе с @BeforeEach (поле cart);
 * - сгруппируйте тесты удаления во вложенный класс @Nested;
 * - дайте тестам понятные названия через @DisplayName;
 * - тесты должны поймать ошибки в remove() и в applyPromo().
 */
@DisplayName("Корзина")
public class CartLifecycleTest {

    private Cart cart;

    @BeforeEach
    void setUp() {
        cart = new Cart();
        cart.add("Бургер", "200.00", 1);
    }

    @Test
    @DisplayName("SALE10 даёт ровно 10% скидки")
    void promo() {
        cart.applyPromo("SALE10");
        assertEquals(new BigDecimal("180.00"), cart.total());
    }

    @Nested
    @DisplayName("Удаление")
    class Removing {

        @Test
        @DisplayName("После удаления единственного товара корзина пуста")
        void removeLast() {
            cart.remove("Бургер");
            assertTrue(cart.isEmpty());
        }

        @Test
        @DisplayName("Удаление одного товара не трогает другие")
        void removeOne() {
            cart.add("Кола", "99.00", 2);
            cart.remove("Бургер");
            assertEquals(2, cart.totalQuantity());
        }
    }
}
