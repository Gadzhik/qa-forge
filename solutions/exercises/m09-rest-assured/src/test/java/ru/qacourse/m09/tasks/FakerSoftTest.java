package ru.qacourse.m09.tasks;

import org.junit.jupiter.api.Test;
import ru.qacourse.m09.Specs;
import ru.qacourse.m09.models.LoginRequest;
import ru.qacourse.m09.models.RegisterRequest;
import ru.qacourse.m09.models.UserResponse;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.fail;
import net.datafaker.Faker;
import org.assertj.core.api.SoftAssertions;

/**
 * Задание 9.10: Datafaker и мягкие проверки.
 * 1) Сгенерируйте уникального пользователя Datafaker'ом (username из 3–30 символов, email, пароль с цифрой),
 *    зарегистрируйте и проверьте ВСЕ поля ответа мягкими проверками (SoftAssertions).
 * 2) Тот же пользователь не может войти паролем В ВЕРХНЕМ РЕГИСТРЕ → 401.
 */
public class FakerSoftTest {

    private final Faker faker = new Faker();

    private RegisterRequest randomUser() {
        String username = faker.name().firstName().toLowerCase().replaceAll("[^a-z]", "") + faker.number().digits(6);
        return RegisterRequest.builder()
                .username(username)
                .email(faker.internet().emailAddress(username))
                .password("Pw" + faker.internet().password(6, 12) + "1")
                .build();
    }

    @Test
    void registerRandomUser() {
        RegisterRequest request = randomUser();
        UserResponse created = given(Specs.base()).body(request).post("/api/auth/register")
                .then().statusCode(201).extract().as(UserResponse.class);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(created.id()).isPositive();
            softly.assertThat(created.username()).isEqualTo(request.getUsername());
            softly.assertThat(created.email()).isEqualTo(request.getEmail());
            softly.assertThat(created.role()).isEqualTo("USER");
        });
    }

    @Test
    void passwordIsCaseSensitive() {
        RegisterRequest request = randomUser();
        given(Specs.base()).body(request).post("/api/auth/register").then().statusCode(201);

        given(Specs.base()).body(new LoginRequest(request.getUsername(), request.getPassword()))
                .post("/api/auth/login").then().statusCode(200);
        given(Specs.base()).body(new LoginRequest(request.getUsername(), request.getPassword().toUpperCase()))
                .post("/api/auth/login").then().statusCode(401);
    }
}
