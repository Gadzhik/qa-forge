package ru.qacourse.fw.api;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import ru.qacourse.fw.config.Config;

/** Спецификации запросов: адрес из конфигурации, JSON, запрос/ответ прикладываются в отчёт Allure. */
public final class Specs {
    private Specs() {
    }

    public static RequestSpecification base() {
        return new RequestSpecBuilder()
                .setBaseUri(Config.get().baseUrl())
                .setContentType(ContentType.JSON)
                .addFilter(new AllureRestAssured())
                .log(LogDetail.URI)
                .build();
    }

    public static RequestSpecification withToken(String token) {
        return new RequestSpecBuilder().addRequestSpecification(base()).addHeader("Authorization", "Bearer " + token).build();
    }
}
