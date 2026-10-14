---
title: "15.7 GitLab CI: runner, пайплайн с тестами и отчётом"
minutes: 45
---

## Как устроен GitLab CI

- Пайплайн описывается файлом **`.gitlab-ci.yml`** в корне репозитория.
- Задания выполняет **GitLab Runner** — программа на отдельной машине (или в Docker), которая забирает задания у GitLab-сервера. На gitlab.com есть общие (shared) раннеры; в компаниях обычно свои.
- Задания чаще всего выполняются в **Docker-образе**, указанном в `image:`.

## Установка своего runner'а (для своего GitLab или для учёбы)

Самый простой способ — runner в Docker:

```bash
docker run -d --name gitlab-runner --restart always \
    -v /var/run/docker.sock:/var/run/docker.sock \
    -v gitlab-runner-config:/etc/gitlab-runner \
    gitlab/gitlab-runner:latest
```

- `--restart always` — перезапускаться вместе с Docker;
- `-v /var/run/docker.sock:...` — дать runner'у управлять Docker хозяина: он будет запускать контейнеры для заданий (executor `docker`). В Windows Docker Desktop путь тот же;
- `gitlab-runner-config` — том для настроек.

Регистрация: в GitLab — **Settings → CI/CD → Runners → New project runner** → получить токен, затем:

```bash
docker exec -it gitlab-runner gitlab-runner register \
    --url https://gitlab.com \
    --token ТОКЕН_ИЗ_GITLAB \
    --executor docker \
    --docker-image maven:3.9.9-eclipse-temurin-21
```

- `--executor docker` — каждое задание в отдельном контейнере;
- `--docker-image` — образ по умолчанию, если в задании не указан `image`.

После этого runner виден в настройках проекта с зелёной точкой.

## .gitlab-ci.yml построчно

Файл `ci/gitlab-ci.yml` курса:

```yaml
stages:
  - test
  - report
```

**Стадии** выполняются по порядку; задания одной стадии — параллельно.

```yaml
variables:
  MAVEN_OPTS: "-Dmaven.repo.local=$CI_PROJECT_DIR/.m2/repository"
  SHOP_URL: "http://localhost:8081"
  TAGS: "smoke | regression"

cache:
  key: maven
  paths:
    - .m2/repository
```

- **`variables`** — переменные окружения для всех заданий. `$CI_PROJECT_DIR` — встроенная переменная GitLab: папка проекта на runner'е;
- `-Dmaven.repo.local=...` — хранить скачанные библиотеки внутри папки проекта: GitLab кэширует только её содержимое;
- **`cache`** — сохранять `.m2/repository` между запусками → зависимости не качаются заново.

```yaml
tests:
  stage: test
  image: maven:3.9.9-eclipse-temurin-21
  script:
    - ./mvnw -B -q install -Dmaven.test.skip=true -pl shop-app,exercises/test-support -am
    - nohup java -jar shop-app/target/shop-app-1.0.0-exec.jar > shop.log 2>&1 &
    - for i in $(seq 1 60); do curl -sf $SHOP_URL/api/products > /dev/null && break; sleep 1; done
    - ./mvnw -B -f exercises/m14-framework/pom.xml test "-Dgroups=$TAGS" -Dshop.url=$SHOP_URL
  artifacts:
    when: always
    paths:
      - exercises/m14-framework/target/allure-results
      - shop.log
    reports:
      junit: exercises/m14-framework/target/surefire-reports/TEST-*.xml
    expire_in: 7 days
```

- `tests` — имя задания; **`stage: test`**;
- **`image`** — контейнер с Maven и JDK 21;
- **`script`** — команды; упала любая (ненулевой код возврата) — задание красное;
- запуск QA Burger в фоне и ожидание готовности — как в GitHub Actions;
- **`artifacts`**:
  - `when: always` — сохранять и при падении;
  - `paths` — какие файлы сохранить (результаты Allure, лог приложения — для разбора);
  - **`reports: junit`** — GitLab разберёт JUnit XML и покажет результаты тестов прямо в Merge Request (вкладка Tests) — с упавшими тестами и сообщениями;
  - `expire_in` — сколько хранить.

```yaml
allure:
  stage: report
  image: maven:3.9.9-eclipse-temurin-21
  when: always
  needs: [ tests ]
  script:
    - ./mvnw -B -f exercises/m14-framework/pom.xml allure:report
  artifacts:
    when: always
    paths:
      - exercises/m14-framework/target/site/allure-maven-plugin
```

- **`when: always`** — выполнять даже если `tests` упал;
- **`needs: [tests]`** — получить артефакты задания `tests` (папку allure-results) и не ждать остальных;
- результат — HTML-отчёт в артефактах; его можно опубликовать в **GitLab Pages** (задание с именем `pages` и артефактом `public/`).

## Параметры запуска

**Build → Pipelines → Run pipeline** — можно переопределить переменные (`TAGS=smoke`, `SHOP_URL=https://stage...`). Переменные-секреты — **Settings → CI/CD → Variables** с флагами *Masked* (скрывать в логах) и *Protected* (только для защищённых веток).

## Расписание

**Build → Pipeline schedules** — ночной регресс по cron, например `0 3 * * 1-5` (в 03:00 по будням).

## Тесты в Docker Compose внутри GitLab CI

Чтобы задание само поднимало связку из урока 15.2, нужен **Docker-in-Docker**:

```yaml
tests-in-docker:
  stage: test
  image: docker:27
  services: [ docker:27-dind ]
  script:
    - docker compose -f ci/docker-compose.tests.yml up --build --abort-on-container-exit --exit-code-from tests
```

- `services: docker:27-dind` — рядом с заданием запускается Docker-демон;
- runner должен быть настроен с `privileged = true` (в `config.toml`) — уточните у DevOps.

```quiz
- q: Кто выполняет задания GitLab CI?
  options: ["GitLab Runner", "Сам GitLab-сервер всегда", "Браузер"]
  answer: 0
- q: Как задания одной стадии выполняются относительно друг друга?
  options: ["Параллельно", "Строго по очереди", "Не выполняются"]
  answer: 0
- q: Что даёт artifacts:reports:junit?
  options: ["Результаты тестов видны прямо в Merge Request", "Удаляет отчёты", "Ускоряет тесты"]
  answer: 0
- q: Зачем MAVEN_OPTS с maven.repo.local внутри папки проекта?
  options: ["GitLab кэширует только файлы проекта — так кэшируются зависимости Maven", "Для безопасности", "Так требует Java"]
  answer: 0
- q: Как сделать, чтобы отчёт строился даже при упавших тестах?
  options: ["when: always у задания отчёта", "allow_failure", "Удалить тесты"]
  answer: 0
```
