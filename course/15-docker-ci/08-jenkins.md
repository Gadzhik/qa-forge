---
title: "15.8 Jenkins: установка в Docker, агент, Jenkinsfile, параметризованный пайплайн"
minutes: 50
---

**Jenkins** — старейшая и до сих пор одна из самых распространённых CI-систем. Огромное количество компаний гоняет автотесты именно в нём.

## Установка в Docker

```bash
docker compose -f ci/jenkins/docker-compose.yml up -d
```

`ci/jenkins/docker-compose.yml`:

```yaml
services:
  jenkins:
    image: jenkins/jenkins:lts-jdk21
    container_name: qa-jenkins
    ports:
      - "8085:8080"
      - "50000:50000"
    volumes:
      - jenkins_home:/var/jenkins_home
```

- `jenkins/jenkins:lts-jdk21` — LTS-версия (долгой поддержки) на Java 21;
- **8085** — веб-интерфейс (8080 у нас занят порталом курса); **50000** — порт для подключения агентов;
- том `jenkins_home` — все настройки, задачи и история сохраняются между перезапусками.

Первый вход:
1. Откройте http://localhost:8085.
2. Пароль администратора: `docker exec qa-jenkins cat /var/jenkins_home/secrets/initialAdminPassword`.
3. **Install suggested plugins** (Git, Pipeline, JUnit и др.).
4. Создайте пользователя.
5. Дополнительно: **Manage Jenkins → Plugins → Available** → **Allure Jenkins Plugin** (для отчётов).

## Агент и JAVA_HOME

Jenkins состоит из **контроллера** (веб-интерфейс, планирование) и **агентов** (машины, где выполняются сборки). Для учёбы сборки можно выполнять прямо на контроллере (built-in node), но в компаниях — только на агентах.

Подключение агента: **Manage Jenkins → Nodes → New Node** → тип *Permanent Agent* → указать рабочую папку (*Remote root directory*) и метку (*Labels*, например `java21`) → Jenkins покажет команду для запуска агента на нужной машине:

```bash
java -jar agent.jar -url http://localhost:8085/ -secret СЕКРЕТ -name "my-laptop" -workDir "/home/anna/jenkins-agent"
```

На агенте нужна Java. Чтобы Jenkins знал, какой JDK использовать в сборках:
- **Manage Jenkins → Tools → JDK installations → Add JDK**: имя `jdk21`, путь `JAVA_HOME` на агенте (например, `/usr/lib/jvm/java-21-openjdk` или `C:\Program Files\Eclipse Adoptium\jdk-21...`) или галочка *Install automatically*;
- в Jenkinsfile: `tools { jdk 'jdk21' }` — Jenkins выставит `JAVA_HOME` и `PATH` для сборки.

Если на агенте Windows — команды в Jenkinsfile вызываются через `bat`, на Linux/macOS — через `sh`. Для Git на Windows нужен Git for Windows (в нём есть Git Bash).

## Задача Pipeline из репозитория

1. **New Item** → имя → **Pipeline** → OK.
2. **Pipeline → Definition: Pipeline script from SCM** → SCM: Git → URL репозитория (для приватного — **Credentials**: SSH-ключ из урока 2.3 или токен) → ветка → **Script Path**: путь к Jenkinsfile (у нас `ci/Jenkinsfile`).
3. **Build Now**.

## Простой Jenkinsfile

`ci/Jenkinsfile`:

```groovy
pipeline {
    agent any

    tools {
        jdk 'jdk21'
    }

    stages {
        stage('Тесты') {
            steps {
                script {
                    if (isUnix()) {
                        sh './mvnw -B -f exercises/m14-framework/pom.xml test'
                    } else {
                        bat 'mvnw.cmd -B -f exercises/m14-framework/pom.xml test'
                    }
                }
            }
        }
    }

    post {
        always {
            junit testResults: 'exercises/m14-framework/target/surefire-reports/*.xml', allowEmptyResults: true
            allure results: [[path: 'exercises/m14-framework/target/allure-results']]
        }
    }
}
```

- `pipeline { }` — **декларативный** пайплайн (современный стандарт; бывает ещё «скриптовый» `node { }`);
- **`agent any`** — на любом агенте; `agent { label 'java21' }` — на агенте с меткой;
- **`tools`** — подключить JDK из настроек;
- **`stages` / `stage` / `steps`** — этапы и шаги; в интерфейсе — красивая диаграмма этапов;
- **`script { }`** — блок с обычным Groovy-кодом (условия, переменные); `isUnix()` — Linux/macOS агент или Windows;
- `sh` / `bat` — выполнить команду оболочки;
- **`post { always { } }`** — после всех этапов, **всегда**:
  - `junit` — разобрать JUnit XML: на странице сборки появятся результаты тестов и график по сборкам;
  - `allure` — построить отчёт Allure (нужен плагин и **Allure Commandline** в *Tools*).

Ещё варианты `post`: `success`, `failure`, `unstable` — например, уведомление в мессенджер только при падении.

## Параметризованный пайплайн

Запуск с выбором стенда, тегов, браузера — `ci/Jenkinsfile.parametrized`:

```groovy
parameters {
    choice(name: 'ENV', choices: ['local', 'stage'], description: 'Стенд')
    string(name: 'SHOP_URL', defaultValue: 'http://localhost:8081', description: 'Адрес QA Burger')
    choice(name: 'TAGS', choices: ['smoke', 'regression', 'smoke | regression'], description: 'Какие тесты')
    choice(name: 'BROWSER', choices: ['chrome', 'firefox'], description: 'Браузер')
    booleanParam(name: 'HEADLESS', defaultValue: true, description: 'Без окна')
    string(name: 'THREADS', defaultValue: '3', description: 'Потоков')
}
```

- `choice` — выпадающий список; `string` — поле ввода; `booleanParam` — галочка;
- после первого запуска у задачи появляется кнопка **Build with Parameters** — форма с этими полями;
- значения доступны как `params.ИМЯ`.

```groovy
def cmd = "-B -f exercises/m14-framework/pom.xml test " +
          "-Dshop.url=${params.SHOP_URL} " +
          "\"-Dgroups=${params.TAGS}\" " +
          "-Dselenide.browser=${params.BROWSER} -Dselenide.headless=${params.HEADLESS} " +
          "-Djunit.jupiter.execution.parallel.config.fixed.parallelism=${params.THREADS}"
if (isUnix()) { sh "./mvnw ${cmd}" } else { bat "mvnw.cmd ${cmd}" }
```

Параметры превращаются в системные свойства Maven — те самые, что читают конфигурация (урок 6.7), теги (урок 7.5), Selenide (урок 10.10) и JUnit-параллельность (урок 14.4). Фреймворк, построенный на конфигурации, легко запускать из любого CI с любыми настройками.

```groovy
options {
    timeout(time: 60, unit: 'MINUTES')
    buildDiscarder(logRotator(numToKeepStr: '20'))
}
```

- `timeout` — убить зависшую сборку;
- `buildDiscarder` — хранить только последние 20 сборок (место на диске).

## Расписание и триггеры

```groovy
triggers {
    cron('H 3 * * 1-5')          // по будням около 03:00 (H — Jenkins сам «размазывает» минуты)
}
```

А запуск по push/MR настраивается вебхуком из GitLab/GitHub в Jenkins.

```quiz
- q: Что такое агент Jenkins?
  options: ["Машина, на которой выполняются сборки", "Пользователь Jenkins", "Плагин отчётов"]
  answer: 0
- q: Как в Jenkinsfile подключить JDK, настроенный в Tools под именем jdk21?
  options: ["tools { jdk 'jdk21' }", "env.JAVA = 21", "Никак"]
  answer: 0
- q: Где разместить публикацию отчётов, чтобы она выполнялась всегда?
  options: ["post { always { ... } }", "stage('Отчёт') в начале", "В parameters"]
  answer: 0
- q: Как получить значение параметра TAGS в Jenkinsfile?
  options: ["params.TAGS", "$TAGS", "env.params"]
  answer: 0
- q: Как выполнить команду на Windows-агенте?
  options: ["bat '...'", "sh '...'", "cmd '...'"]
  answer: 0
```
