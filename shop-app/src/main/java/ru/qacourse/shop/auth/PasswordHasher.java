package ru.qacourse.shop.auth;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Упрощённое хеширование пароля (SHA-256 с солью из имени пользователя).
 * В реальных системах используют bcrypt/argon2 — здесь достаточно, чтобы пароли не лежали в БД открытым текстом.
 */
@Component
public class PasswordHasher {
    public String hash(String username, String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest((username.toLowerCase() + ":" + password).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
