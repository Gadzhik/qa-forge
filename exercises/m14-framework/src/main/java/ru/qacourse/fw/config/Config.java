package ru.qacourse.fw.config;

import org.aeonbits.owner.ConfigFactory;

/** Точка доступа к конфигурации: Config.get().baseUrl(). */
public final class Config {
    private Config() {
    }

    public static FrameworkConfig get() {
        // создаём каждый раз заново: значения системных свойств могут меняться (так работают проверки курса)
        return ConfigFactory.create(FrameworkConfig.class, System.getProperties());
    }
}
