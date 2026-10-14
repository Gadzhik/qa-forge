package ru.qacourse.shop.auth;

import org.springframework.stereotype.Service;
import ru.qacourse.shop.bugs.Bug;
import ru.qacourse.shop.bugs.BugRegistry;
import ru.qacourse.shop.common.ApiException;
import ru.qacourse.shop.user.Role;
import ru.qacourse.shop.user.User;
import ru.qacourse.shop.user.UserRepository;

import org.springframework.http.HttpStatus;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordHasher hasher;
    private final JwtService jwt;
    private final BugRegistry bugs;

    public AuthService(UserRepository users, PasswordHasher hasher, JwtService jwt, BugRegistry bugs) {
        this.users = users;
        this.hasher = hasher;
        this.jwt = jwt;
        this.bugs = bugs;
    }

    public User register(String username, String email, String password) {
        if (password.length() < 6 && !bugs.isOn(Bug.BUG_202)) {
            throw ApiException.badRequest("WEAK_PASSWORD", "Пароль должен быть не короче 6 символов");
        }
        if (users.existsByUsername(username)) {
            throw ApiException.conflict("USERNAME_TAKEN", "Имя пользователя уже занято");
        }
        if (users.existsByEmailIgnoreCase(email) && !bugs.isOn(Bug.BUG_201)) {
            throw ApiException.conflict("EMAIL_TAKEN", "Email уже зарегистрирован");
        }
        return users.save(new User(username, email, hashFor(username, password), Role.USER));
    }

    public String login(String username, String password) {
        User user = users.findByUsername(username)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "BAD_CREDENTIALS", "Неверное имя пользователя или пароль"));
        if (!user.getPasswordHash().equals(hashFor(username, password))) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "BAD_CREDENTIALS", "Неверное имя пользователя или пароль");
        }
        return jwt.issue(user.getId(), user.getUsername(), user.getRole().name());
    }

    private String hashFor(String username, String password) {
        // BUG-203: пароль приводится к нижнему регистру перед хешированием
        String effective = bugs.isOn(Bug.BUG_203) ? password.toLowerCase() : password;
        return hasher.hash(username, effective);
    }
}
