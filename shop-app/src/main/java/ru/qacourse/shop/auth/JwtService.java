package ru.qacourse.shop.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Минимальная реализация JWT (HS256) без сторонних библиотек.
 * Токен = base64url(header).base64url(payload).base64url(подпись).
 * Его удобно разбирать на jwt.io в уроке про авторизацию.
 */
@Component
public class JwtService {
    private static final Base64.Encoder ENC = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DEC = Base64.getUrlDecoder();

    private final ObjectMapper mapper;
    private final byte[] secret;
    private final Duration ttl;

    public JwtService(ObjectMapper mapper,
                      @Value("${shop.jwt.secret:qa-burger-training-secret}") String secret,
                      @Value("${shop.jwt.ttl:PT8H}") Duration ttl) {
        this.mapper = mapper;
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.ttl = ttl;
    }

    public String issue(long userId, String username, String role) {
        try {
            Map<String, Object> header = Map.of("alg", "HS256", "typ", "JWT");
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("sub", String.valueOf(userId));
            payload.put("username", username);
            payload.put("role", role);
            long now = Instant.now().getEpochSecond();
            payload.put("iat", now);
            payload.put("exp", now + ttl.toSeconds());
            String h = ENC.encodeToString(mapper.writeValueAsBytes(header));
            String p = ENC.encodeToString(mapper.writeValueAsBytes(payload));
            return h + "." + p + "." + sign(h + "." + p);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /** Возвращает id пользователя, если токен корректен и не истёк. */
    public Optional<Long> verify(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) return Optional.empty();
            byte[] expected = sign(parts[0] + "." + parts[1]).getBytes(StandardCharsets.US_ASCII);
            if (!MessageDigest.isEqual(expected, parts[2].getBytes(StandardCharsets.US_ASCII))) return Optional.empty();
            Map<?, ?> payload = mapper.readValue(DEC.decode(parts[1]), Map.class);
            long exp = ((Number) payload.get("exp")).longValue();
            if (Instant.now().getEpochSecond() > exp) return Optional.empty();
            return Optional.of(Long.parseLong((String) payload.get("sub")));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private String sign(String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret, "HmacSHA256"));
        return ENC.encodeToString(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
    }
}
