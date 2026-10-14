---
title: "11.5 PostgreSQL в Docker, DBeaver и QA Burger на настоящей базе"
minutes: 40
---

До сих пор QA Burger работал на встроенной H2. На реальных проектах — PostgreSQL (или другая серверная БД). Поднимем её в **Docker**: без установки, одной командой, одинаково на любой ОС. Подробно про Docker — модуль 15; сейчас нужно только его установить и выполнить пару команд.

## Установка Docker

```tabs
Windows: |
  Установите **Docker Desktop**: https://www.docker.com/products/docker-desktop/ (или `winget install Docker.DockerDesktop`).
  При установке оставьте галочку **Use WSL 2** — Docker в Windows работает внутри лёгкой виртуальной машины Linux (WSL 2).
  После установки перезагрузитесь и запустите Docker Desktop — дождитесь зелёного статуса «Engine running».
macOS: |
  Установите **Docker Desktop** для своего процессора (Apple Silicon или Intel): https://www.docker.com/products/docker-desktop/
  или `brew install --cask docker`. Запустите приложение Docker — значок кита в строке меню.
Linux: |
  Docker Engine (Ubuntu):

  ~~~bash
  curl -fsSL https://get.docker.com | sudo sh
  sudo usermod -aG docker $USER
  ~~~

  - первая команда скачивает официальный установочный скрипт и выполняет его с правами администратора;
  - `usermod -aG docker $USER` — добавить себя в группу `docker`, чтобы запускать docker без `sudo`. **Выйдите из системы и войдите снова**, чтобы группа применилась.
```

Проверка: `docker --version` и `docker compose version`.

## Запуск PostgreSQL

В корне курса лежит `docker-compose.yml`. В папке курса:

```bash
docker compose up -d postgres
```

- `docker compose` — инструмент, запускающий набор контейнеров по описанию из `docker-compose.yml`;
- `up` — создать и запустить;
- `-d` (*detached*) — в фоне, вернуть терминал;
- `postgres` — только этот сервис из файла.

Первый раз скачается образ (~80 МБ). Проверка: `docker compose ps` — статус `Up (healthy)`.

Как описан сервис (фрагмент `docker-compose.yml`):

```yaml
postgres:
  image: postgres:16-alpine
  environment:
    POSTGRES_DB: qaburger
    POSTGRES_USER: qaburger
    POSTGRES_PASSWORD: qaburger
  ports:
    - "${QABURGER_DB_PORT:-5432}:5432"
  volumes:
    - pgdata:/var/lib/postgresql/data
```

- `image` — готовый образ PostgreSQL 16;
- `environment` — переменные, по которым образ при первом старте создаёт базу `qaburger` и пользователя `qaburger` с паролем `qaburger`;
- `ports` — «пробросить» порт: `ПОРТ_НА_КОМПЬЮТЕРЕ:ПОРТ_В_КОНТЕЙНЕРЕ`. `${QABURGER_DB_PORT:-5432}` — взять переменную окружения `QABURGER_DB_PORT`, а если её нет — 5432;
- `volumes` — данные хранятся в именованном томе и переживают перезапуск контейнера.

### Если порт 5432 занят

Частая ситуация: на компьютере уже стоит свой PostgreSQL (при подготовке курса было именно так). Docker сообщит: `failed to bind host port 0.0.0.0:5432/tcp: address already in use`. Опасный побочный эффект: QA Burger может подключиться к **чужой** базе на 5432 и упасть с `role "qaburger" does not exist`. Решение — другой порт:

```tabs
Windows (PowerShell): |
  ~~~powershell
  $env:QABURGER_DB_PORT = "55432"
  docker compose up -d postgres
  ~~~
macOS / Linux: |
  ~~~bash
  QABURGER_DB_PORT=55432 docker compose up -d postgres
  ~~~
```

## QA Burger на PostgreSQL

Остановите обычный запуск курса (Ctrl+C) и запустите приложение с **профилем docker** (PostgreSQL + Kafka):

```tabs
Windows (PowerShell): |
  ~~~powershell
  docker compose up -d postgres kafka
  java -jar shop-app\target\shop-app-1.0.0-exec.jar --spring.profiles.active=docker
  ~~~
  Если база на другом порту: `$env:DB_PORT = "55432"` перед запуском.
macOS / Linux: |
  ~~~bash
  docker compose up -d postgres kafka
  java -jar shop-app/target/shop-app-1.0.0-exec.jar --spring.profiles.active=docker
  ~~~
  Если база на другом порту: `DB_PORT=55432 java -jar ...`.
```

- `--spring.profiles.active=docker` — включить профиль `docker`, который в `application.yml` объединяет профили `postgres` и `kafka`;
- в профиле `postgres` адрес базы: `jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/qaburger`;
- при старте **Flyway** создаст таблицы в пустой базе (`V1__schema.sql`), а приложение заполнит начальные данные.

> ⚠️ В отличие от H2, данные в PostgreSQL **сохраняются** между перезапусками. Вернуть начальное состояние: «Сбросить данные» в Тренажёре, `POST /api/admin/reset`, или полностью: `docker compose down -v` (`-v` удаляет тома с данными).

## psql — консольный клиент

```bash
docker exec -it qaburger-postgres psql -U qaburger -d qaburger
```

- `docker exec` — выполнить команду внутри работающего контейнера;
- `-it` — интерактивно, с терминалом;
- `psql -U пользователь -d база` — консольный клиент PostgreSQL.

Полезные команды psql: `\dt` — список таблиц, `\d orders` — структура таблицы, `\q` — выход. Обычные SQL-запросы заканчивайте `;`.

## DBeaver — графический клиент

**DBeaver Community** (https://dbeaver.io/download/) — бесплатная программа для работы с любыми БД. Альтернативы: DataGrip (платный, JetBrains), pgAdmin (только PostgreSQL).

Подключение к PostgreSQL QA Burger:
1. **Database → New Database Connection → PostgreSQL**.
2. Host `localhost`, Port `5432` (или ваш), Database `qaburger`, Username `qaburger`, Password `qaburger`.
3. **Test Connection** — DBeaver предложит скачать драйвер, согласитесь. **Finish**.

Подключение к H2 обычного запуска (без Docker): **H2 Embedded/Server**, JDBC URL `jdbc:h2:tcp://localhost:9093/mem:shop`, пользователь `sa`, пароль пустой.

Что удобно в DBeaver: дерево таблиц, редактор SQL с автодополнением (Ctrl+Enter — выполнить запрос под курсором), просмотр и редактирование данных как в Excel, **ER-диаграмма** (правый клик по схеме → View Diagram) — картинка связей таблиц, как в уроке 11.1.

> 🧪 **Попробуй сам.** Подключитесь DBeaver'ом к базе QA Burger, откройте ER-диаграмму. Оформите заказ на сайте и найдите его в таблицах `orders` и `order_items`.

```quiz
- q: Что делает docker compose up -d postgres?
  options: ["Запускает в фоне контейнер postgres из docker-compose.yml", "Устанавливает PostgreSQL в систему", "Удаляет базу"]
  answer: 0
- q: "Что означает запись ports: \"55432:5432\"?"
  options: ["Порт 55432 компьютера ведёт на порт 5432 контейнера", "Две базы на двух портах", "Ошибка"]
  answer: 0
- q: Docker пишет address already in use для 5432. Что делать?
  options: ["Запустить на другом порту через QABURGER_DB_PORT", "Удалить свой PostgreSQL", "Переустановить Docker"]
  answer: 0
- q: Как полностью удалить данные PostgreSQL QA Burger?
  options: ["docker compose down -v", "docker compose stop", "Перезагрузить компьютер"]
  answer: 0
- q: Как включить у QA Burger работу с PostgreSQL и Kafka?
  options: ["--spring.profiles.active=docker", "--db=postgres", "Никак"]
  answer: 0
```
