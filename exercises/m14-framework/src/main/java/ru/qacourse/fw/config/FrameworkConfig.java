package ru.qacourse.fw.config;

import org.aeonbits.owner.Config;

/**
 * Настройки фреймворка (урок 14.3). Источники по приоритету:
 * системные свойства (-Dshop.url=...) → переменные окружения → framework.properties → значения по умолчанию.
 */
@Config.LoadPolicy(Config.LoadType.MERGE)
@Config.Sources({"system:properties", "system:env", "classpath:framework.properties"})
public interface FrameworkConfig extends Config {

    @Key("shop.url")
    @DefaultValue("http://localhost:8081")
    String baseUrl();

    @Key("user.login")
    @DefaultValue("user")
    String userLogin();

    @Key("user.password")
    @DefaultValue("user123")
    String userPassword();

    @Key("await.seconds")
    @DefaultValue("20")
    int awaitSeconds();
}
