package ru.qacourse.m06;

import org.aeonbits.owner.Config;

/**
 * Задание 6.7: те же настройки через библиотеку Owner.
 * Добавьте аннотации: источники (сначала системные свойства, потом файл) и ключи.
 */
public interface OwnerConfig extends Config {

    String baseUrl();

    int timeoutSeconds();

    String browser();
}
