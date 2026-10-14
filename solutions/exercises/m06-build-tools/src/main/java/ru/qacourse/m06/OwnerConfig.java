package ru.qacourse.m06;

import org.aeonbits.owner.Config;

/**
 * Задание 6.7: те же настройки через библиотеку Owner.
 * Добавьте аннотации: источники (сначала системные свойства, потом файл) и ключи.
 */
@Config.LoadPolicy(Config.LoadType.MERGE)
@Config.Sources({"system:properties", "classpath:config.properties"})
public interface OwnerConfig extends Config {

    @Key("base.url")
    @DefaultValue("http://localhost:8081")
    String baseUrl();

    @Key("timeout.seconds")
    @DefaultValue("5")
    int timeoutSeconds();

    @Key("browser")
    @DefaultValue("chrome")
    String browser();
}
