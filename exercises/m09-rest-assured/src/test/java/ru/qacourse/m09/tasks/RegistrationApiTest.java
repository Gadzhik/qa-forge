package ru.qacourse.m09.tasks;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import ru.qacourse.m09.models.RegisterRequest;
import ru.qacourse.m09.models.UserResponse;
import ru.qacourse.support.ShopConfig;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.*;

/**
 * Задание 9.3: POST с телом-объектом и ответ в объект (POJO).
 * Сначала допишите модели RegisterRequest и UserResponse (src/main/java/.../models).
 * 1) Успешная регистрация: 201, ответ → UserResponse, username и email совпадают, role = USER.
 * 2) Повторная регистрация с тем же email (другой username) → 409.
 * 3) Пароль короче 6 символов → 400.
 */
public class RegistrationApiTest {

    @Test
    void register() {
        fail("TODO: напишите тест");
    }
}
