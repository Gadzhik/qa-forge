package ru.qacourse.shop.web;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.qacourse.shop.admin.AdminController;
import ru.qacourse.shop.admin.DataSeeder;
import ru.qacourse.shop.auth.AuthInterceptor;
import ru.qacourse.shop.auth.AuthService;
import ru.qacourse.shop.auth.CurrentUser;
import ru.qacourse.shop.bugs.Bug;
import ru.qacourse.shop.bugs.BugRegistry;
import ru.qacourse.shop.cart.CartService;
import ru.qacourse.shop.common.ApiException;
import ru.qacourse.shop.order.Order;
import ru.qacourse.shop.order.OrderService;
import ru.qacourse.shop.product.Category;
import ru.qacourse.shop.product.Product;
import ru.qacourse.shop.product.ProductService;
import ru.qacourse.shop.product.ReviewRepository;
import ru.qacourse.shop.user.User;

import java.util.List;
import java.util.Optional;

/** Веб-страницы магазина (Thymeleaf, шаблоны в resources/templates). */
@Controller
public class PageController {
    private final ProductService products;
    private final ReviewRepository reviews;
    private final CartService cart;
    private final OrderService orders;
    private final AuthService auth;
    private final BugRegistry bugs;
    private final AdminController admin;
    private final DataSeeder seeder;

    public PageController(ProductService products, ReviewRepository reviews, CartService cart, OrderService orders,
                          AuthService auth, BugRegistry bugs, AdminController admin, DataSeeder seeder) {
        this.products = products;
        this.reviews = reviews;
        this.cart = cart;
        this.orders = orders;
        this.auth = auth;
        this.bugs = bugs;
        this.admin = admin;
        this.seeder = seeder;
    }

    @GetMapping("/")
    public String catalog(@RequestParam(required = false) String search,
                          @RequestParam(required = false) Category category,
                          @RequestParam(required = false) String sort,
                          Model model) {
        model.addAttribute("products", products.search(search, category, sort, 0, 100).content());
        model.addAttribute("categories", Category.values());
        model.addAttribute("search", search);
        model.addAttribute("category", category);
        model.addAttribute("sort", sort);
        return "catalog";
    }

    @GetMapping("/product/{id}")
    public String product(@PathVariable long id, Model model) {
        Product p = products.get(id);
        model.addAttribute("product", p);
        model.addAttribute("reviews", reviews.findByProductIdOrderByIdAsc(id));
        model.addAttribute("unsafeReviews", bugs.isOn(Bug.BUG_601));
        model.addAttribute("brokenAddButton", bugs.isOn(Bug.BUG_703));
        return "product";
    }

    @PostMapping("/cart/add")
    public String addToCart(@RequestParam long productId, @RequestParam(defaultValue = "1") int quantity,
                            @RequestHeader(value = "Referer", required = false) String referer,
                            HttpServletRequest req, RedirectAttributes ra) {
        Optional<User> user = CurrentUser.find(req);
        if (user.isEmpty()) return "redirect:/login";
        try {
            cart.add(user.get().getId(), productId, quantity);
            ra.addFlashAttribute("message", "Товар добавлен в корзину");
        } catch (ApiException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:" + (referer == null ? "/" : referer);
    }

    @GetMapping("/cart")
    public String cart(HttpServletRequest req, Model model) {
        Optional<User> user = CurrentUser.find(req);
        if (user.isEmpty()) return "redirect:/login";
        model.addAttribute("cart", cart.view(user.get().getId()));
        return "cart";
    }

    @PostMapping("/cart/remove")
    public String removeFromCart(@RequestParam long productId, HttpServletRequest req) {
        CurrentUser.find(req).ifPresent(u -> cart.remove(u.getId(), productId));
        return "redirect:/cart";
    }

    @PostMapping("/checkout")
    public String checkout(@RequestParam(required = false) String promoCode, HttpServletRequest req, RedirectAttributes ra) {
        Optional<User> user = CurrentUser.find(req);
        if (user.isEmpty()) return "redirect:/login";
        try {
            Order order = orders.create(user.get(), promoCode);
            return "redirect:/orders/" + order.getId();
        } catch (ApiException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/cart";
        }
    }

    @GetMapping("/orders")
    public String orders(HttpServletRequest req, Model model) {
        Optional<User> user = CurrentUser.find(req);
        if (user.isEmpty()) return "redirect:/login";
        model.addAttribute("orders", orders.list(user.get()));
        return "orders";
    }

    @GetMapping("/orders/{id}")
    public String order(@PathVariable long id, HttpServletRequest req, Model model) {
        Optional<User> user = CurrentUser.find(req);
        if (user.isEmpty()) return "redirect:/login";
        model.addAttribute("order", orders.get(user.get(), id));
        return "order";
    }

    @PostMapping("/orders/{id}/pay")
    public String pay(@PathVariable long id, HttpServletRequest req, RedirectAttributes ra) {
        CurrentUser.find(req).ifPresent(u -> {
            try {
                orders.pay(u, id);
            } catch (ApiException e) {
                ra.addFlashAttribute("error", e.getMessage());
            }
        });
        return "redirect:/orders/" + id;
    }

    @PostMapping("/orders/{id}/cancel")
    public String cancel(@PathVariable long id, HttpServletRequest req, RedirectAttributes ra) {
        CurrentUser.find(req).ifPresent(u -> {
            try {
                orders.cancel(u, id);
            } catch (ApiException e) {
                ra.addFlashAttribute("error", e.getMessage());
            }
        });
        return "redirect:/orders/" + id;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password,
                        HttpServletResponse res, Model model) {
        try {
            String token = auth.login(username, password);
            Cookie c = new Cookie(AuthInterceptor.COOKIE, token);
            c.setPath("/");
            c.setHttpOnly(false); // специально доступна из JS, чтобы её было видно в DevTools на уроке
            res.addCookie(c);
            return "redirect:/";
        } catch (ApiException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("username", username);
            return "login";
        }
    }

    @PostMapping("/logout")
    public String logout(HttpServletResponse res) {
        Cookie c = new Cookie(AuthInterceptor.COOKIE, "");
        c.setPath("/");
        c.setMaxAge(0);
        res.addCookie(c);
        return "redirect:/";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username, @RequestParam String email, @RequestParam String password,
                           Model model, RedirectAttributes ra) {
        try {
            if (username.isBlank() || email.isBlank() || !email.contains("@")) {
                throw ApiException.badRequest("VALIDATION_ERROR", "Заполните имя и корректный email");
            }
            auth.register(username.trim(), email.trim(), password);
            ra.addFlashAttribute("message", "Регистрация прошла успешно, войдите");
            return "redirect:/login";
        } catch (ApiException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("username", username);
            model.addAttribute("email", email);
            return "register";
        }
    }

    @GetMapping("/admin")
    public String adminPage(Model model) {
        model.addAttribute("bugs", admin.bugs());
        model.addAttribute("mystery", mysteryMode);
        model.addAttribute("enabledCount", bugs.enabled().size());
        return "admin";
    }

    @PostMapping("/admin/bugs")
    public String saveBugs(@RequestParam(name = "bug", required = false) List<String> codes, RedirectAttributes ra) {
        bugs.set(codes == null ? List.of() : codes.stream().map(Bug::fromCode).toList());
        mysteryMode = false;
        ra.addFlashAttribute("message", "Сохранено. Включено багов: " + bugs.enabled().size());
        return "redirect:/admin";
    }

    /** «Режим загадки»: включает N случайных багов и не показывает, какие именно. */
    @PostMapping("/admin/mystery")
    public String mystery(@RequestParam(defaultValue = "5") int count, RedirectAttributes ra) {
        List<Bug> all = new java.util.ArrayList<>(List.of(Bug.values()));
        java.util.Collections.shuffle(all);
        bugs.set(all.subList(0, Math.max(1, Math.min(count, all.size()))));
        mysteryMode = true;
        ra.addFlashAttribute("message", "Включено случайных багов: " + bugs.enabled().size() + ". Найдите их вручную!");
        return "redirect:/admin";
    }

    @PostMapping("/admin/reveal")
    public String reveal(RedirectAttributes ra) {
        mysteryMode = false;
        ra.addFlashAttribute("message", "Загадка раскрыта: включённые баги отмечены галочками");
        return "redirect:/admin";
    }

    private volatile boolean mysteryMode;

    @PostMapping("/admin/reset")
    public String reset(RedirectAttributes ra) {
        seeder.reset();
        ra.addFlashAttribute("message", "Данные сброшены к начальному состоянию");
        return "redirect:/admin";
    }

    @ExceptionHandler(ApiException.class)
    public String error(ApiException e, Model model, HttpServletResponse res) {
        res.setStatus(e.status().value());
        model.addAttribute("status", e.status().value());
        model.addAttribute("message", e.getMessage());
        return "error-page";
    }
}
