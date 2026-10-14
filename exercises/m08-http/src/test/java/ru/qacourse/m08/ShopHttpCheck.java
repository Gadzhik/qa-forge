package ru.qacourse.m08;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.qacourse.shop.launcher.ShopAppLauncher;

import static org.assertj.core.api.Assertions.*;

/** Проверка задания 8.7: запускает свой экземпляр QA Burger на свободном порту. Не изменяйте. */
class ShopHttpCheck {
    static ShopAppLauncher.Running app;
    static ShopHttp shop;

    @BeforeAll
    static void start() {
        app = ShopAppLauncher.start();
        shop = new ShopHttp(app.baseUrl());
    }

    @AfterAll
    static void stop() {
        app.close();
    }

    @Test
    @DisplayName("Пример productStatus: 200 и 404")
    void status() throws Exception {
        assertThat(shop.productStatus(1)).isEqualTo(200);
        assertThat(shop.productStatus(9999)).isEqualTo(404);
    }

    @Test
    @DisplayName("productName(1) = «Классический бургер»")
    void name() throws Exception {
        assertThat(shop.productName(1)).isEqualTo("Классический бургер");
        assertThat(shop.productName(12)).isEqualTo("Кола 0.5");
    }

    @Test
    @DisplayName("countInCategory: DRINK = 5, DESSERT = 4")
    void count() throws Exception {
        assertThat(shop.countInCategory("DRINK")).isEqualTo(5);
        assertThat(shop.countInCategory("DESSERT")).isEqualTo(4);
    }

    @Test
    @DisplayName("login возвращает JWT, неверный пароль — исключение с кодом 401")
    void login() throws Exception {
        assertThat(shop.login("user", "user123")).matches("[\\w-]+\\.[\\w-]+\\.[\\w-]+");
        assertThatThrownBy(() -> shop.login("user", "wrong"))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("401");
    }

    @Test
    @DisplayName("cartQuantity с токеном: пустая корзина = 0")
    void cart() throws Exception {
        assertThat(shop.cartQuantity(shop.login("admin", "admin123"))).isZero();
    }
}
