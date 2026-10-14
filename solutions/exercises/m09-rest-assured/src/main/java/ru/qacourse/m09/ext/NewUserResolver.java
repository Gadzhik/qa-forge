package ru.qacourse.m09.ext;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import ru.qacourse.m09.Specs;
import ru.qacourse.m09.models.RegisterRequest;

import java.util.UUID;

import static io.restassured.RestAssured.given;

/**
 * Задание 9.11: регистрирует через API нового пользователя для каждого параметра @NewUser ApiUser
 * и получает его токен.
 * username: "u" + 10 случайных символов, email: username + "@test.local", пароль: "Secret1".
 */
public class NewUserResolver implements ParameterResolver {

    @Override
    public boolean supportsParameter(ParameterContext pc, ExtensionContext ec) {
        return pc.isAnnotated(NewUser.class) && pc.getParameter().getType() == ApiUser.class;
    }

    @Override
    public Object resolveParameter(ParameterContext pc, ExtensionContext ec) {
        String username = "u" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        String password = "Secret1";
        RegisterRequest request = new RegisterRequest(username, username + "@test.local", password);
        int id = given(Specs.base()).body(request).post("/api/auth/register")
                .then().statusCode(201).extract().path("id");
        return new ApiUser(id, username, password, Specs.token(username, password));
    }
}
