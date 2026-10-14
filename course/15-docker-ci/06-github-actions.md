---
title: "15.6 GitHub Actions"
minutes: 35
---

**GitHub Actions** — CI, встроенный в GitHub. Бесплатен для открытых репозиториев (и с лимитом минут — для закрытых). Отличный способ показать в портфолио, что ваши тесты запускаются автоматически.

## Где лежит пайплайн

Файлы YAML в папке `.github/workflows/` репозитория. Пример для вашего проекта — `ci/github-actions-tests.yml` в курсе. А сам курс проверяет себя файлом `.github/workflows/course-verify.yml` — он прогоняет эталонные решения на **Linux, Windows и macOS**.

## Разбор workflow

```yaml
name: Автотесты

on:
  push:
    branches: [ main ]
  pull_request:
  workflow_dispatch:
    inputs:
      tags:
        description: 'JUnit-теги'
        default: 'smoke'
```

- `name` — название в интерфейсе (вкладка **Actions**);
- **`on`** — триггеры: push в `main`, каждый Pull Request, ручной запуск (`workflow_dispatch`) с параметром `tags`. Ещё бывает `schedule: - cron: '0 3 * * *'` — каждую ночь в 03:00 UTC.

```yaml
jobs:
  tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v7

      - uses: actions/setup-java@v6
        with:
          distribution: temurin
          java-version: '21'
          cache: maven
```

- **`jobs`** — задания; `tests` — имя задания;
- **`runs-on: ubuntu-latest`** — виртуальная машина GitHub (есть `windows-latest`, `macos-latest`). На ней уже установлены Chrome, Docker (Linux), curl;
- **`steps`** — шаги по порядку;
- **`uses:`** — готовое действие (action) из маркетплейса: `checkout` скачивает репозиторий, `setup-java` ставит JDK и **кэширует** `~/.m2` (`cache: maven`) — повторные запуски быстрее.

```yaml
      - name: Запустить QA Burger
        run: |
          ./mvnw -B -q install -Dmaven.test.skip=true -pl shop-app,exercises/test-support -am
          nohup java -jar shop-app/target/shop-app-1.0.0-exec.jar > shop.log 2>&1 &
          for i in $(seq 1 60); do curl -sf http://localhost:8081/api/products > /dev/null && break; sleep 1; done
```

- **`run: |`** — команды оболочки (несколько строк);
- `nohup ... &` — запустить приложение в фоне, чтобы шаг не ждал его завершения (`nohup` — не останавливать при закрытии терминала);
- цикл `for ... curl -sf ... && break; sleep 1` — **ждать готовности** приложения до 60 секунд (`-f` — считать HTTP-ошибку неудачей, `-s` — тихо).

```yaml
      - name: Тесты
        run: ./mvnw -B -f exercises/m14-framework/pom.xml test "-Dgroups=${{ github.event.inputs.tags || 'smoke | regression' }}"
```

- `${{ ... }}` — выражение GitHub Actions: взять параметр ручного запуска, а если его нет (push) — `'smoke | regression'`.

```yaml
      - name: Отчёт Allure
        if: always()
        run: ./mvnw -B -f exercises/m14-framework/pom.xml allure:report

      - uses: actions/upload-artifact@v7
        if: always()
        with:
          name: allure-report
          path: exercises/m14-framework/target/site/allure-maven-plugin
```

- **`if: always()`** — выполнить шаг, даже если тесты упали (по умолчанию шаги после упавшего пропускаются);
- **`upload-artifact`** — сохранить папку как **артефакт** сборки: её можно скачать архивом со страницы запуска. Для публикации отчёта на сайт используют GitHub Pages (action `peaceiris/actions-gh-pages` или официальный `actions/deploy-pages`).

## Матрица — кроссплатформенность и кроссбраузерность

```yaml
jobs:
  verify:
    strategy:
      fail-fast: false
      matrix:
        os: [ ubuntu-latest, windows-latest, macos-latest ]
    runs-on: ${{ matrix.os }}
```

- **`matrix`** — запустить job **для каждой** комбинации значений; так курс проверяется на трёх ОС одновременно;
- `fail-fast: false` — не отменять остальные варианты, если один упал.

Аналогично — матрица браузеров: `browser: [chrome, firefox]` и `-Dselenide.browser=${{ matrix.browser }}`.

## Секреты

**Settings → Secrets and variables → Actions → New repository secret**, например `STAGE_PASSWORD`. В workflow: `${{ secrets.STAGE_PASSWORD }}` — значение маскируется в логах `***`.

> 🧪 **Попробуй сам.** Создайте на GitHub репозиторий с решениями модуля 14 (или всего курса), положите `ci/github-actions-tests.yml` в `.github/workflows/tests.yml`, сделайте push и откройте вкладку **Actions**. Скачайте артефакт с отчётом и откройте его через `allure open`.

```quiz
- q: Где лежат файлы GitHub Actions?
  options: [".github/workflows/*.yml", ".gitlab-ci.yml", "Jenkinsfile"]
  answer: 0
- q: "Что делает if: always() у шага?"
  options: ["Выполняет шаг даже после падения предыдущих", "Запускает шаг дважды", "Пропускает шаг"]
  answer: 0
- q: Зачем matrix?
  options: ["Запустить job для каждой комбинации значений (ОС, браузеры, версии)", "Для красоты", "Хранить секреты"]
  answer: 0
- q: Как использовать секрет в workflow?
  options: ["${{ secrets.ИМЯ }}", "$ИМЯ", "Записать значение в файл"]
  answer: 0
```
