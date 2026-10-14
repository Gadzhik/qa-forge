---
title: "6.8 Настройка Maven и Gradle: settings.xml, прокси, корпоративные репозитории, IDEA"
minutes: 35
---

Дома всё качается из Maven Central «само». В компании в первый же день часто выясняется: интернет через **прокси**, библиотеки — только из **внутреннего репозитория** (Nexus, Artifactory), а сборка падает с `Could not resolve dependencies`. Этот урок — про то, где и как это настраивается.

## Где живут настройки Maven

| Файл | Для чего | Кладут ли в git |
|---|---|---|
| `pom.xml` | Описание **проекта**: зависимости, плагины | ✅ да |
| `~/.m2/settings.xml` | Настройки **вашего компьютера**: зеркала, прокси, пароли от репозиториев | ❌ нет (там пароли) |
| `.mvn/maven.config` | Опции командной строки по умолчанию для проекта | ✅ да |
| `.mvn/jvm.config` | Параметры JVM для самого Maven (память) | ✅ да |
| `.mvn/wrapper/maven-wrapper.properties` | Версия Maven для wrapper | ✅ да |

`~/.m2` — в Windows это `C:\Users\Имя\.m2`. Файла `settings.xml` по умолчанию **нет** — создайте его сами, если нужен.

## settings.xml

Пример со всеми частыми разделами:

```xml
<settings xmlns="http://maven.apache.org/SETTINGS/1.2.0">

    <!-- 1. Зеркало: ВСЕ запросы к репозиториям отправлять во внутренний Nexus компании -->
    <mirrors>
        <mirror>
            <id>company-nexus</id>
            <mirrorOf>*</mirrorOf>
            <url>https://nexus.company.ru/repository/maven-public/</url>
        </mirror>
    </mirrors>

    <!-- 2. Логин/пароль для репозитория с таким же id -->
    <servers>
        <server>
            <id>company-nexus</id>
            <username>anna</username>
            <password>${env.NEXUS_PASSWORD}</password>
        </server>
    </servers>

    <!-- 3. Прокси для выхода в интернет -->
    <proxies>
        <proxy>
            <id>office</id>
            <active>true</active>
            <protocol>https</protocol>
            <host>proxy.company.ru</host>
            <port>3128</port>
            <nonProxyHosts>localhost|127.0.0.1|*.company.ru</nonProxyHosts>
        </proxy>
    </proxies>
</settings>
```

Разбор:
- **`<mirror>`** — подменить адрес репозитория. `mirrorOf` = `*` — для всех; `central` — только вместо Maven Central;
- **`<server>`** — учётные данные; `id` должен совпадать с `id` зеркала или репозитория. `${env.NEXUS_PASSWORD}` — значение берётся из переменной окружения, чтобы пароль не лежал в файле открыто;
- **`<proxy>`** — прокси-сервер; `nonProxyHosts` — адреса, к которым ходить напрямую (обязательно `localhost`, иначе тесты против `localhost:8081` пойдут через прокси и сломаются).

Указать другой файл настроек: `./mvnw -s путь/к/settings.xml test`. Так часто делают в CI.

> 💡 Не знаете адрес Nexus и прокси? Спросите у коллег или DevOps в первый день — у них точно есть готовый `settings.xml`.

## .mvn/maven.config — опции по умолчанию

Если каждый раз пишете одни и те же опции, положите их в файл `.mvn/maven.config` в корне проекта (каждая опция с новой строки):

```text
-B
-Dsurefire.rerunFailingTestsCount=1
```

`-B` (batch) — без цветного вывода и интерактивных вопросов, удобно для CI.

## Память для Maven и тестов

Большие проекты иногда падают с `OutOfMemoryError: Java heap space`.

- Для самого Maven — файл `.mvn/jvm.config`: `-Xmx2g` (максимум 2 ГБ памяти).
- Для **тестов** (они идут в отдельном процессе) — в конфигурации surefire: `<argLine>-Xmx1g</argLine>`.
- Переменная окружения `MAVEN_OPTS="-Xmx2g"` — то же для Maven глобально.

## Какую Java использует Maven

Maven запускается той Java, которую найдёт:
1. если задан `JAVA_HOME` — оттуда;
2. иначе — первая `java` в `PATH`.

Проверка: `./mvnw -v` — в выводе строка `Java version: 21..., runtime: /путь`. Если там 17, а нужна 21 — поправьте `JAVA_HOME` (урок 0.2). Ошибка `release version 21 not supported` означает ровно это: Maven запущен старой Java.

## Настройка Maven в IntelliJ IDEA

**Settings → Build, Execution, Deployment → Build Tools → Maven**:

| Пункт | Что выбрать |
|---|---|
| **Maven home path** | *Use Maven wrapper* — та же версия, что в терминале |
| **User settings file** | `~/.m2/settings.xml` (поставьте галочку *Override*, если файл в другом месте) |
| **Local repository** | `~/.m2/repository` |
| **Importing → JDK for importer** | JDK 21 |
| **Runner → JRE** | *Use Project JDK* |

**Runner → Delegate IDE build/run actions to Maven** — если включить, кнопка ▶ в IDEA будет запускать тесты через Maven (как в CI). По умолчанию выключено: IDEA собирает сама, быстрее.

Если IDEA «не видит» зависимости, хотя в терминале всё собирается: панель **Maven** → 🔄 **Reload All Maven Projects**; если не помогло — **File → Invalidate Caches → Invalidate and Restart**.

## Gradle: аналогичные настройки

- `~/.gradle/gradle.properties` — настройки компьютера (прокси, пароли), в git не кладут:

```properties
systemProp.https.proxyHost=proxy.company.ru
systemProp.https.proxyPort=3128
systemProp.https.nonProxyHosts=localhost|127.0.0.1
nexusPassword=секрет
org.gradle.jvmargs=-Xmx2g
```

- `gradle.properties` в корне проекта — настройки проекта (в git).
- Внутренний репозиторий — в `build.gradle` или `settings.gradle`:

```groovy
repositories {
    maven {
        url = 'https://nexus.company.ru/repository/maven-public/'
        credentials {
            username = 'anna'
            password = findProperty('nexusPassword')
        }
    }
}
```

- IDEA: **Settings → Build Tools → Gradle** → *Gradle JVM* = JDK 21, *Build and run using* = Gradle или IntelliJ IDEA.

## Частые ошибки и что делать

| Ошибка | Причина | Решение |
|---|---|---|
| `Could not resolve dependencies` / `Could not transfer artifact` | Нет доступа к репозиторию: сеть, прокси, нет зеркала | Проверьте интернет, `settings.xml`, прокси |
| `PKIX path building failed` | Корпоративный прокси подменяет HTTPS-сертификат, Java ему не доверяет | Импортировать сертификат компании в хранилище Java (`keytool -importcert ...`) — обычно даёт DevOps |
| `401 Unauthorized` при скачивании | Неверный логин/пароль в `<servers>` или не совпадает `id` | Сверить `id` и учётные данные |
| `release version 21 not supported` | Maven запущен старой Java | `JAVA_HOME` → JDK 21, проверить `./mvnw -v` |
| `Non-resolvable parent POM` | Не найден родительский pom | Проверить `<relativePath>` или выполнить `install` родителя |
| Странные ошибки после обрыва сети | Битые файлы в кэше | Удалить проблемную папку в `~/.m2/repository` (или запуск с `-U`) |
| `Address already in use` / тесты не видят `localhost` | Прокси перехватывает localhost | Добавить `localhost` в `nonProxyHosts` |

```quiz
- q: Где хранятся пароли от корпоративного репозитория Maven?
  options: ["~/.m2/settings.xml (лучше со ссылкой на переменную окружения)", "pom.xml проекта", "В коде тестов"]
  answer: 0
- q: Что делает зеркало с mirrorOf = *?
  options:
    - Перенаправляет все запросы к репозиториям на указанный адрес (например, Nexus)
    - Копирует проект
    - Отключает интернет
  answer: 0
- q: "Ошибка `release version 21 not supported` означает..."
  options:
    - Maven запущен Java ниже 21 — нужно поправить JAVA_HOME
    - Нет интернета
    - Тесты упали
  answer: 0
- q: Зачем добавлять localhost в nonProxyHosts?
  options:
    - Чтобы запросы к локальному приложению не шли через прокси
    - Чтобы ускорить интернет
    - Это не нужно
  answer: 0
- q: Как заставить IDEA использовать ту же версию Maven, что и терминал?
  options: [Maven home path → Use Maven wrapper, Установить второй Maven, Никак]
  answer: 0
```
