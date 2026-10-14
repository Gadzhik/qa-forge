---
title: "15.2 Docker Compose и тесты в контейнерах"
minutes: 45
---

## Docker Compose

Когда контейнеров несколько (приложение + база + Kafka + тесты), запускать каждый командой `docker run` неудобно. **Docker Compose** описывает всю связку в одном YAML-файле и поднимает её одной командой.

### docker-compose.yml курса построчно

```yaml
services:
  postgres:
    image: postgres:16-alpine
    container_name: qaburger-postgres
    environment:
      POSTGRES_DB: qaburger
      POSTGRES_USER: qaburger
      POSTGRES_PASSWORD: qaburger
    ports:
      - "${QABURGER_DB_PORT:-5432}:5432"
    volumes:
      - pgdata:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U qaburger -d qaburger"]
      interval: 5s
      retries: 20
```

- **`services:`** — список сервисов (контейнеров); `postgres` — имя сервиса, оно же — **имя хоста** в сети compose;
- **`image`** — какой образ (или `build:` — собрать из Dockerfile);
- **`container_name`** — имя контейнера (для `docker exec`, `docker logs`);
- **`environment`** — переменные окружения;
- **`ports`** — проброс портов; `${ПЕРЕМЕННАЯ:-по_умолчанию}` — подстановка из окружения или файла `.env`;
- **`volumes`** — `pgdata` — именованный том (объявлен внизу файла в разделе `volumes:`);
- **`healthcheck`** — как проверить, что сервис **готов** (а не просто запущен): `pg_isready` — утилита PostgreSQL. Каждые 5 с, до 20 попыток.

```yaml
  shop-app:
    build: ./shop-app
    profiles: ["app"]
    environment:
      SPRING_PROFILES_ACTIVE: docker
      DB_HOST: postgres
      SPRING_KAFKA_BOOTSTRAP_SERVERS: kafka:29092
    depends_on:
      postgres:
        condition: service_healthy
      kafka:
        condition: service_healthy
```

- **`build: ./shop-app`** — собрать образ из `shop-app/Dockerfile`;
- **`profiles: ["app"]`** — сервис запускается только явно (`docker compose --profile app up` или `docker compose up shop-app`) — чтобы по умолчанию поднимать только инфраструктуру;
- **`DB_HOST: postgres`** — внутри сети compose база доступна по **имени сервиса**, не `localhost`! (`localhost` внутри контейнера — это сам контейнер);
- `SPRING_KAFKA_BOOTSTRAP_SERVERS` — переменная окружения переопределяет `spring.kafka.bootstrap-servers` (Spring Boot сам сопоставляет `ИМЯ_ЧЕРЕЗ_ПОДЧЁРКИВАНИЯ` со свойствами);
- **`depends_on` + `condition: service_healthy`** — запускать приложение только после того, как база и Kafka **готовы**. Без `condition` Compose ждёт лишь старта контейнера, а не готовности — частая причина «приложение не смогло подключиться к базе».

### Команды

| Команда | Что делает |
|---|---|
| `docker compose up -d` | поднять всё в фоне |
| `docker compose up -d postgres kafka` | только перечисленные сервисы |
| `docker compose ps` | статус сервисов |
| `docker compose logs -f shop-app` | логи сервиса |
| `docker compose stop` / `start` | остановить / запустить |
| `docker compose down` | остановить и удалить контейнеры и сеть |
| `docker compose down -v` | …и тома с данными |
| `docker compose up --build` | пересобрать образы перед запуском |
| `docker compose -f другой.yml ...` | использовать другой файл |

## Связка «приложение + тесты» в Docker

На CI удобно запускать **и приложение, и тесты** в контейнерах — окружение полностью воспроизводимо. Файл `ci/docker-compose.tests.yml`:

```yaml
services:
  shop:
    build: ../shop-app
    healthcheck:
      test: ["CMD-SHELL", "bash -c '</dev/tcp/localhost/8081' || exit 1"]
      interval: 3s
      retries: 40

  tests:
    build:
      context: ..
      dockerfile: ci/tests.Dockerfile
    environment:
      SHOP_URL: http://shop:8081
    volumes:
      - ./allure-results:/course/exercises/m14-framework/target/allure-results
    depends_on:
      shop:
        condition: service_healthy
```

- `shop` — QA Burger; healthcheck проверяет, что порт 8081 принимает соединения (`</dev/tcp/host/port` — встроенная в bash проверка TCP-подключения);
- `tests` — образ из `ci/tests.Dockerfile`: Maven + JDK 21 + код курса; команда по умолчанию — `mvn test` модуля 14 с `-Dshop.url=${SHOP_URL}`;
- **`SHOP_URL: http://shop:8081`** — тесты обращаются к приложению по имени сервиса;
- **`volumes`** — результаты Allure пишутся в папку на вашем компьютере `ci/allure-results`;
- `depends_on ... service_healthy` — тесты стартуют после готовности приложения.

`ci/tests.Dockerfile`:

```dockerfile
FROM maven:3.9.9-eclipse-temurin-21
WORKDIR /course
COPY . .
RUN mvn -B -q install -Dmaven.test.skip=true -pl shop-app,exercises/test-support -am
CMD ["sh", "-c", "mvn -B -f exercises/m14-framework/pom.xml test -Dtest='*Test' -Dshop.url=${SHOP_URL}"]
```

Запуск (сначала выполните задания модуля 14, иначе тесты упадут на TODO):

```bash
./mvnw -q -pl shop-app -am package -DskipTests
docker compose -f ci/docker-compose.tests.yml up --build --abort-on-container-exit --exit-code-from tests
```

- `--abort-on-container-exit` — когда один контейнер завершился (тесты закончились) — остановить все;
- `--exit-code-from tests` — код возврата команды = код возврата тестов (0 — успех). Так CI поймёт, упали ли тесты (урок 6.3).

После — `docker compose -f ci/docker-compose.tests.yml down`, отчёт: `allure serve ci/allure-results` (если установлен Allure CLI) или скопируйте результаты в `target/allure-results` модуля и `./mvnw ... allure:serve`.

> ⚠️ На Linux файлы, созданные контейнером в примонтированной папке, могут принадлежать `root`. Удалить их: `sudo rm -rf ci/allure-results` или запускать контейнер от своего пользователя (`user: "${UID}:${GID}"`).

```quiz
- q: По какому адресу контейнер с тестами обращается к сервису shop в одной сети compose?
  options: ["http://shop:8081", "http://localhost:8081", "http://127.0.0.1:8081"]
  answer: 0
- q: Зачем condition service_healthy в depends_on?
  options:
    - "Ждать, пока зависимость готова к работе, а не просто запущена"
    - "Для красоты"
    - "Чтобы удалить контейнер"
  answer: 0
- q: Что делает --exit-code-from tests?
  options: ["Код возврата команды = код возврата контейнера с тестами", "Удаляет тесты", "Запускает тесты дважды"]
  answer: 0
- q: Что удалит docker compose down -v?
  options: ["Контейнеры, сеть и тома с данными", "Только образы", "Ничего"]
  answer: 0
```
