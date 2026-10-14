# QA Forge — Java QA Automation с нуля

> Локальный курс с тренажёрами: уроки, учебное приложение QA Burger, задания с автопроверкой, браузерные игры и Test Lab.

Курс для тех, кто начинает с нуля: от теории тестирования и основ Java до собственного тестового фреймворка
(API + UI + БД + Kafka) с отчётами Allure и запуском в CI. Всё работает **локально** на Windows, macOS и Linux.

- **Портал курса** — http://localhost:8080: 134 урока, квизы, задания с кнопкой «Проверить», прогресс и достижения.
- **QA Burger** — http://localhost:8081: учебный интернет-магазин, который мы тестируем: REST API, Swagger, веб-интерфейс,
  GraphQL, SOAP, база данных, события Kafka и 30 «спрятанных» багов.
- **Тренажёры** — 7 браузерных игр (QA Marathon, Priority Poker, Log Detective, POJO Builder, Locator Shock, API Circus, System Autopsy).
- **Test Lab** — 30 багов «из продакшена»: нужно написать автотест, который падает на баге и проходит после исправления.

## Что нужно установить

| Что | Зачем | Обязательно |
|---|---|---|
| **JDK 21** (например, Eclipse Temurin) | Запуск портала, приложения и тестов | да |
| **Google Chrome** (или Chromium) | UI-тесты, модуль 10 | с модуля 10 |
| **Docker Desktop** / Docker Engine | PostgreSQL, Kafka, Selenoid, Jenkins — модули 11, 13, 15 | с модуля 11 |
| **IntelliJ IDEA Community** | Писать код и запускать тесты | рекомендуется |
| **Git** | Модуль 2 и дипломный проект | рекомендуется |

Maven ставить **не нужно** — в проекте есть Maven Wrapper (`mvnw` / `mvnw.cmd`), он скачает всё сам.
Подробная установка с командами для каждой ОС — в уроке **0.2** (после запуска портала).

### Коротко: установка JDK 21

- **Windows** (PowerShell): `winget install EclipseAdoptium.Temurin.21.JDK`
- **macOS** (Homebrew): `brew install --cask temurin@21`
- **Linux** (Ubuntu/Debian): `sudo apt install openjdk-21-jdk` · (Fedora): `sudo dnf install java-21-openjdk-devel` · (Arch): `sudo pacman -S jdk21-openjdk`

Проверка: `java -version` должна показать версию 21 или новее.

## Запуск

Windows — двойной клик по `start.cmd` или в терминале:

```bat
start.cmd
```

macOS / Linux:

```bash
./start.sh
```

Первый запуск собирает проект и скачивает зависимости (несколько минут, нужен интернет). Затем откройте
**http://localhost:8080** — начните с модуля «Старт».

Пересобрать после обновления курса: `start.cmd --rebuild` / `./start.sh --rebuild`.

### Остановка

- закройте окно, в котором запущен курс, или нажмите в нём **Ctrl+C**;
- или запустите `stop.cmd` (Windows) / `./stop.sh` (macOS, Linux) — сработает, даже если окно потерялось.

Повторный `start` при уже запущенном курсе ничего не сломает: скрипт скажет «Курс уже запущен».

### Инфраструктура в Docker (с модуля 11)

```bash
docker compose up -d postgres kafka kafka-ui   # PostgreSQL :5432, Kafka :9092, Kafka UI :8090
docker compose down                            # остановить
```

Если порт занят (например, у вас уже установлен PostgreSQL): `QABURGER_DB_PORT=55432 docker compose up -d postgres`
(в PowerShell: `$env:QABURGER_DB_PORT=55432; docker compose up -d postgres`).

## Программа

| # | Модуль | # | Модуль |
|---|---|---|---|
| 0 | Старт: установка, терминал, первый запуск | 10 | UI: Selenium WebDriver и Selenide |
| 1 | Теория тестирования | 11 | Базы данных: SQL, PostgreSQL, JDBC |
| 2 | Git, IntelliJ IDEA, DevTools | 12 | Заглушки внешних сервисов: WireMock |
| 3 | Java: основы | 13 | Асинхронность и Kafka |
| 4 | Java: ООП | 14 | Тестовый фреймворк и Allure |
| 5 | Java: продвинутое | 15 | Docker, Testcontainers, Selenoid, CI (GitHub Actions, GitLab CI, Jenkins) |
| 6 | Maven, Gradle, Lombok, Jackson, Owner | 16 | Разбор падений и инцидентов |
| 7 | JUnit 5, AssertJ, TestNG | 17 | AI в работе QA |
| 8 | HTTP и API | 18 | Карьера и дипломный проект |
| 9 | REST Assured | | |

## Как устроены задания

Задание в уроке ссылается на файл в `exercises/`. Вы пишете код в IDEA, затем нажимаете **«Проверить»** в портале —
портал запускает скрытые проверки (`*Check`) через Maven Wrapper и показывает, что не так.
То же самое из терминала:

```bash
./mvnw -q -f exercises/m09-rest-assured/pom.xml test -Dtest='*Check'
```

Эталонные решения лежат в `solutions/` — портал показывает их после попытки. Подсматривать до попытки не стоит 🙂

## Структура репозитория

```
course/        уроки (Markdown) — по папке на модуль
portal/        портал курса (Spring Boot + страница на JavaScript), порт 8080
  └─ src/main/resources/games/   данные тренажёров (YAML)
shop-app/      учебное приложение QA Burger, порт 8081
exercises/     задания: по Maven-модулю на модуль курса + test-support (общие утилиты проверок)
testlab/       Test Lab: 30 сценариев-багов и проверки
solutions/     эталонные решения заданий и Test Lab
ci/            примеры CI: GitHub Actions, GitLab CI, Jenkins, Selenium Grid / Selenoid, тесты в Docker
tools/         validate_course.py — проверка разметки уроков; verify-solutions.sh — прогон всех эталонов
docker-compose.yml   PostgreSQL, Kafka, Kafka UI, QA Burger в контейнере
start.sh / start.cmd запуск курса; stop.sh / stop.cmd — остановка
```

## Где хранится прогресс

В файле `~/.qa-course/progress.json` (Windows: `%USERPROFILE%\.qa-course\progress.json`). Портал записывает его
сразу после каждого действия, поэтому прогресс переживает остановку курса, перезагрузку компьютера и обновление курса.
Рядом лежит резервная копия `progress.json.bak`: если основной файл повреждён, портал восстановит прогресс из неё.
Сбросить прогресс — на странице «Мой прогресс» в портале.

Данные QA Burger (заказы, пользователи, корзины) хранятся в памяти и при перезапуске сбрасываются — так задумано.

## Частые проблемы

| Симптом | Что делать |
|---|---|
| `Java не найдена` / версия ниже 21 | Установите JDK 21, перезапустите терминал (урок 0.2) |
| `Курс уже запущен` / порт 8080 или 8081 занят | Остановите предыдущий запуск: `stop.cmd` / `./stop.sh`. Не помогло — порт занят другой программой |
| Первый запуск долго «висит» | Maven скачивает зависимости — это нормально, нужен интернет |
| Корпоративная сеть, Maven не может скачать библиотеки | Настройте прокси или зеркало в `~/.m2/settings.xml` (урок 6.8) |
| UI-тесты: браузер не найден | Установите Chrome; драйвер Selenium Manager скачает сам |
| Docker: `port is already allocated` | Задайте другой порт через `QABURGER_DB_PORT` / `QABURGER_KAFKA_PORT` |

## Для сопровождающих

```bash
python3 tools/validate_course.py         # разметка уроков, квизы, задания, данные тренажёров (нужен PyYAML)
SKIP_DOCKER=1 tools/verify-solutions.sh  # все эталонные решения без Docker-модулей
tools/verify-solutions.sh                # всё, включая PostgreSQL и Kafka (нужен docker compose up -d kafka)
```

Курс — оригинальный учебный материал; стек и темы ориентированы на актуальные требования к Java QA Automation.
