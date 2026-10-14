package ru.qacourse.shop.admin;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.qacourse.shop.auth.PasswordHasher;
import ru.qacourse.shop.cart.CartRepository;
import ru.qacourse.shop.events.EventLog;
import ru.qacourse.shop.files.FileController;
import ru.qacourse.shop.order.OrderRepository;
import ru.qacourse.shop.order.PromoCode;
import ru.qacourse.shop.order.PromoRepository;
import ru.qacourse.shop.product.Category;
import ru.qacourse.shop.product.Product;
import ru.qacourse.shop.product.ProductRepository;
import ru.qacourse.shop.product.Review;
import ru.qacourse.shop.product.ReviewRepository;
import ru.qacourse.shop.user.Role;
import ru.qacourse.shop.user.User;
import ru.qacourse.shop.user.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Начальные данные. Вызывается при старте и по POST /api/admin/reset,
 * чтобы каждый прогон тестов начинался с одинакового состояния.
 * <p>
 * Пользователи: admin / admin123 (ADMIN), user / user123 (USER).
 */
@Component
public class DataSeeder implements ApplicationRunner {
    private final UserRepository users;
    private final ProductRepository products;
    private final ReviewRepository reviews;
    private final PromoRepository promos;
    private final CartRepository cart;
    private final OrderRepository orders;
    private final PasswordHasher hasher;
    private final EventLog eventLog;
    private final FileController files;

    public DataSeeder(UserRepository users, ProductRepository products, ReviewRepository reviews, PromoRepository promos,
                      CartRepository cart, OrderRepository orders, PasswordHasher hasher, EventLog eventLog,
                      FileController files) {
        this.users = users;
        this.products = products;
        this.reviews = reviews;
        this.promos = promos;
        this.cart = cart;
        this.orders = orders;
        this.hasher = hasher;
        this.eventLog = eventLog;
        this.files = files;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (users.count() == 0) seed();
    }

    @Transactional
    public void reset() {
        cart.deleteAll();
        orders.deleteAll();
        reviews.deleteAll();
        products.deleteAll();
        promos.deleteAll();
        users.deleteAll();
        eventLog.clear();
        files.clear();
        seed();
    }

    private void seed() {
        users.save(new User("admin", "admin@qaburger.local", hasher.hash("admin", "admin123"), Role.ADMIN));
        users.save(new User("user", "user@qaburger.local", hasher.hash("user", "user123"), Role.USER));

        List<Product> list = List.of(
                p("Классический бургер", "Говяжья котлета, сыр чеддер, томат, салат, фирменный соус", "289.90", Category.BURGER, 50, "🍔"),
                p("Чизбургер двойной", "Две котлеты и двойной чеддер", "379.90", Category.BURGER, 40, "🍔"),
                p("Чикенбургер", "Куриное филе в панировке, салат, майонез", "259.90", Category.BURGER, 35, "🐔"),
                p("Веганбургер", "Котлета из нута, авокадо, томат", "319.00", Category.BURGER, 20, "🌱"),
                p("Бургер Острый", "Котлета, халапеньо, острый соус", "339.50", Category.BURGER, 25, "🌶️"),
                p("Бургер с беконом", "Котлета, бекон, сыр, барбекю", "399.00", Category.BURGER, 30, "🥓"),
                p("Картофель фри", "Хрустящий, с солью", "129.00", Category.SIDE, 100, "🍟"),
                p("Картофель по-деревенски", "Дольки со специями", "149.00", Category.SIDE, 80, "🥔"),
                p("Наггетсы 6 шт", "Куриные наггетсы с соусом", "189.90", Category.SIDE, 60, "🍗"),
                p("Луковые кольца", "8 колец в кляре", "159.00", Category.SIDE, 45, "🧅"),
                p("Салат Цезарь", "Курица, салат, пармезан, сухарики", "249.00", Category.SIDE, 15, "🥗"),
                p("Кола 0.5", "Газированный напиток", "99.00", Category.DRINK, 200, "🥤"),
                p("Лимонад домашний", "Лимон, мята, сахарный сироп", "149.90", Category.DRINK, 50, "🍋"),
                p("Морс клюквенный", "Без сахара", "119.00", Category.DRINK, 40, "🍒"),
                p("Кофе латте", "Двойной эспрессо и молоко", "179.00", Category.DRINK, 70, "☕"),
                p("Вода без газа", "0.5 л", "69.00", Category.DRINK, 10, "💧"),
                p("Молочный коктейль", "Ванильный, 0.4 л", "199.00", Category.DESSERT, 30, "🥛"),
                p("Пирожок вишнёвый", "Горячий пирожок с вишней", "89.90", Category.DESSERT, 60, "🥧"),
                p("Мороженое рожок", "Пломбир", "79.00", Category.DESSERT, 100, "🍦"),
                p("Чизкейк", "Нью-Йорк", "229.00", Category.DESSERT, 12, "🍰"));
        List<Product> saved = products.saveAll(list);

        reviews.save(new Review(saved.get(0).getId(), "user", "Очень сочный, возьму ещё!", 5));
        reviews.save(new Review(saved.get(0).getId(), "admin", "Соуса маловато", 4));

        promos.save(new PromoCode("SALE10", 10, LocalDate.of(2099, 12, 31)));
        promos.save(new PromoCode("BURGER20", 20, LocalDate.of(2099, 12, 31)));
        promos.save(new PromoCode("SUMMER2020", 30, LocalDate.of(2020, 8, 31)));
    }

    private static Product p(String name, String desc, String price, Category c, int stock, String emoji) {
        return new Product(name, desc, new BigDecimal(price), c, stock, emoji);
    }
}
