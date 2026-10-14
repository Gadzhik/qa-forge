package ru.qacourse.shop.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import ru.qacourse.shop.user.UserRepository;

/**
 * Для каждого запроса ищет токен:
 *  1) в заголовке Authorization: Bearer &lt;token&gt; (так работает API);
 *  2) в cookie "token" (так работают веб-страницы).
 * Благодаря п.2 можно авторизоваться в UI-тесте через API: получить токен и положить его в cookie браузера.
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {
    public static final String COOKIE = "token";

    private final JwtService jwt;
    private final UserRepository users;

    public AuthInterceptor(JwtService jwt, UserRepository users) {
        this.jwt = jwt;
        this.users = users;
    }

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) {
        String token = null;
        String header = req.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            token = header.substring(7).trim();
        } else if (req.getCookies() != null) {
            for (Cookie c : req.getCookies()) {
                if (COOKIE.equals(c.getName())) token = c.getValue();
            }
        }
        if (token != null) {
            jwt.verify(token).flatMap(users::findById).ifPresent(u -> req.setAttribute(CurrentUser.ATTR, u));
        }
        return true;
    }
}
