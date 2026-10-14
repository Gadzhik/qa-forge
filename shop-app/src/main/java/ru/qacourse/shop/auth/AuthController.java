package ru.qacourse.shop.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.qacourse.shop.user.User;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Авторизация")
public class AuthController {

    public record RegisterRequest(@NotBlank @Size(min = 3, max = 30) String username,
                                  @NotBlank @Email String email,
                                  @NotBlank String password) {
    }

    public record LoginRequest(@NotBlank String username, @NotBlank String password) {
    }

    public record TokenResponse(String token, String type) {
    }

    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Регистрация нового пользователя")
    public User register(@Valid @RequestBody RegisterRequest body) {
        return auth.register(body.username(), body.email(), body.password());
    }

    @PostMapping("/login")
    @Operation(summary = "Вход: возвращает JWT-токен")
    public TokenResponse login(@Valid @RequestBody LoginRequest body) {
        return new TokenResponse(auth.login(body.username(), body.password()), "Bearer");
    }

    @GetMapping("/me")
    @Operation(summary = "Текущий пользователь (нужен токен)")
    public User me(HttpServletRequest req) {
        return CurrentUser.require(req);
    }
}
