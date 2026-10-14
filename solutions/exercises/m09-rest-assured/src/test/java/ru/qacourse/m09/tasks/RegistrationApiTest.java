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

    private RegisterRequest user(String name) {
        return RegisterRequest.builder()
                .username(name)
                .email(name + "@test.local")
                .password("Secret1")
                .build();
    }

    @Test
    void registerNewUser() {
        RegisterRequest request = user("anna");

        UserResponse created = given()
                .baseUri(ShopConfig.baseUrl())
                .contentType(ContentType.JSON)
                .body(request)
                .post("/api/auth/register")
                .then().statusCode(201)
                .extract().as(UserResponse.class);

        assertThat(created.username()).isEqualTo("anna");
        assertThat(created.email()).isEqualTo("anna@test.local");
        assertThat(created.role()).isEqualTo("USER");
    }

    @Test
    void duplicateEmailIsRejected() {
        RegisterRequest first = user("boris");
        given().baseUri(ShopConfig.baseUrl()).contentType(ContentType.JSON).body(first)
                .post("/api/auth/register").then().statusCode(201);

        RegisterRequest second = user("boris2");
        second.setEmail(first.getEmail());
        given().baseUri(ShopConfig.baseUrl()).contentType(ContentType.JSON).body(second)
                .post("/api/auth/register").then().statusCode(409);
    }

    @Test
    void shortPasswordIsRejected() {
        RegisterRequest request = user("vera");
        request.setPassword("12345");
        given().baseUri(ShopConfig.baseUrl()).contentType(ContentType.JSON).body(request)
                .post("/api/auth/register").then().statusCode(400);
    }
}
