# Образ, который запускает автотесты курса внутри Docker (урок 15.2).
# Сборка из КОРНЯ курса: docker build -f ci/tests.Dockerfile -t qaburger-tests .

# Базовый образ: Maven 3.9 + JDK 21 — всё нужное для сборки и запуска тестов
FROM maven:3.9.9-eclipse-temurin-21

# Рабочая папка внутри контейнера
WORKDIR /course

# Копируем код курса (лишнее отсекает файл .dockerignore)
COPY . .

# Собираем и устанавливаем в локальный репозиторий контейнера то, от чего зависят тесты
RUN mvn -B -q install -Dmaven.test.skip=true -pl shop-app,exercises/test-support -am

# Команда по умолчанию: прогнать тесты фреймворка против сервиса shop (адрес — из переменной SHOP_URL)
CMD ["sh", "-c", "mvn -B -f exercises/m14-framework/pom.xml test -Dtest='*Test' -Dshop.url=${SHOP_URL}"]
