package ru.qacourse.m09.tasks;

import org.junit.jupiter.api.Test;
import ru.qacourse.m09.Specs;
import ru.qacourse.m09.models.LoginRequest;
import ru.qacourse.m09.models.RegisterRequest;
import ru.qacourse.m09.models.UserResponse;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.fail;

/**
 * Задание 9.10: Datafaker и мягкие проверки.
 * 1) Сгенерируйте уникального пользователя Datafaker'ом (username из 3–30 символов, email, пароль с цифрой),
 *    зарегистрируйте и проверьте ВСЕ поля ответа мягкими проверками (SoftAssertions).
 * 2) Тот же пользователь не может войти паролем В ВЕРХНЕМ РЕГИСТРЕ → 401.
 */
public class FakerSoftTest {

    @Test
    void faker() {
        fail("TODO: напишите тест");
    }
}
