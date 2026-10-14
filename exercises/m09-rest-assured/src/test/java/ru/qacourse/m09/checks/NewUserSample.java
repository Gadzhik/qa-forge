package ru.qacourse.m09.checks;

import org.junit.jupiter.api.Test;
import ru.qacourse.m09.Specs;
import ru.qacourse.m09.ext.ApiUser;
import ru.qacourse.m09.ext.NewUser;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

/** Тест-образец с @NewUser. Не изменяйте. */
public class NewUserSample {

    @Test
    void userIsRegisteredAndLoggedIn(@NewUser ApiUser user) {
        assertThat(user.username()).matches("u[0-9a-f]{10}");
        given(Specs.base()).header("Authorization", "Bearer " + user.token())
                .get("/api/auth/me")
                .then().statusCode(200).body("username", equalTo(user.username()));
    }

    @Test
    void twoUsersAreDifferent(@NewUser ApiUser first, @NewUser ApiUser second) {
        assertThat(first.username()).isNotEqualTo(second.username());
        assertThat(first.id()).isNotEqualTo(second.id());
    }
}
