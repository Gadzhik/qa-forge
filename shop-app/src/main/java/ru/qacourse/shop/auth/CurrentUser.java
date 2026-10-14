package ru.qacourse.shop.auth;

import jakarta.servlet.http.HttpServletRequest;
import ru.qacourse.shop.common.ApiException;
import ru.qacourse.shop.user.Role;
import ru.qacourse.shop.user.User;

import java.util.Optional;

/** Достаёт пользователя, которого AuthInterceptor положил в запрос. */
public final class CurrentUser {
    public static final String ATTR = "currentUser";

    private CurrentUser() {
    }

    public static Optional<User> find(HttpServletRequest req) {
        return Optional.ofNullable((User) req.getAttribute(ATTR));
    }

    public static User require(HttpServletRequest req) {
        return find(req).orElseThrow(ApiException::unauthorized);
    }

    public static User requireAdmin(HttpServletRequest req) {
        User u = require(req);
        if (u.getRole() != Role.ADMIN) throw ApiException.forbidden();
        return u;
    }
}
