package ru.qacourse.m10.checks;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import ru.qacourse.m10.Browser;
import ru.qacourse.m10.pages.CartPage;
import ru.qacourse.m10.pages.CatalogPage;
import ru.qacourse.m10.pages.LoginPage;

import static org.assertj.core.api.Assertions.assertThat;

/** Тест-образец на ваших Page Object. Не изменяйте. */
public class PageObjectSample {
    private WebDriver driver;

    @BeforeEach
    void setUp() {
        driver = Browser.chrome();
    }

    @AfterEach
    void tearDown() {
        driver.quit();
    }

    @Test
    void buyTwoBurgers() {
        CatalogPage catalog = new LoginPage(driver).open().loginAs("user", "user123");
        assertThat(catalog.userName()).isEqualTo("user");
        assertThat(catalog.productNames()).hasSize(20).contains("Классический бургер");

        catalog.addToCart(1).addToCart(1);
        assertThat(catalog.cartCount()).isEqualTo("2");

        CartPage cart = catalog.openCart();
        assertThat(cart.rowsCount()).isEqualTo(1);
        assertThat(cart.total()).isEqualTo("579.80");
    }
}
