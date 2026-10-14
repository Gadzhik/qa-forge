package ru.qacourse.shop.web;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import ru.qacourse.shop.auth.CurrentUser;
import ru.qacourse.shop.bugs.Bug;
import ru.qacourse.shop.bugs.BugRegistry;
import ru.qacourse.shop.cart.CartService;
import ru.qacourse.shop.cart.CartView;
import ru.qacourse.shop.user.User;

/**
 * Общие данные для всех страниц: текущий пользователь, счётчик корзины, версия дизайна.
 * Дизайн v2 включается параметром ?design=v2 (запоминается в cookie) и выключается ?design=v1.
 * В v2 меняются классы и разметка, но атрибуты data-testid остаются — урок про устойчивые локаторы.
 */
@ControllerAdvice(annotations = Controller.class)
public class UiModelAdvice {
    private final CartService cart;
    private final BugRegistry bugs;

    public UiModelAdvice(CartService cart, BugRegistry bugs) {
        this.cart = cart;
        this.bugs = bugs;
    }

    @ModelAttribute
    public void common(HttpServletRequest req, HttpServletResponse res, Model model) {
        String design = req.getParameter("design");
        if (design != null) {
            Cookie c = new Cookie("design", design.equals("v2") ? "v2" : "v1");
            c.setPath("/");
            res.addCookie(c);
        } else if (req.getCookies() != null) {
            for (Cookie c : req.getCookies()) if (c.getName().equals("design")) design = c.getValue();
        }
        model.addAttribute("v2", "v2".equals(design));

        User user = CurrentUser.find(req).orElse(null);
        model.addAttribute("user", user);
        if (user != null) {
            model.addAttribute("headerName", bugs.isOn(Bug.BUG_702) ? user.getEmail() : user.getUsername());
            CartView view = cart.view(user.getId());
            model.addAttribute("cartCount", bugs.isOn(Bug.BUG_701) ? view.items().size() : view.totalQuantity());
        } else {
            model.addAttribute("cartCount", 0);
        }
    }
}
