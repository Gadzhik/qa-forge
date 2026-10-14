---
title: "13.3 Kafka руками: Docker, консольные утилиты, Kafka UI"
minutes: 40
---

## Запуск

```bash
docker compose up -d kafka kafka-ui
```

- `kafka` — брокер Apache Kafka 3.9 в режиме **KRaft** (без отдельного ZooKeeper — так работают современные версии);
- `kafka-ui` — веб-интерфейс для просмотра топиков: http://localhost:8090

Порт брокера для вашего компьютера — `localhost:9092`. Если он занят — `QABURGER_KAFKA_PORT=19092 docker compose up -d kafka` (и далее везде `localhost:19092`).

Разбор настроек брокера в `docker-compose.yml`:

```yaml
KAFKA_LISTENERS: PLAINTEXT://:9092,INTERNAL://:29092,CONTROLLER://:9093
KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092,INTERNAL://kafka:29092
```

- **listeners** — на каких портах брокер слушает внутри контейнера;
- **advertised listeners** — какой адрес брокер **сообщает клиентам**. Клиент сначала подключается к любому адресу, а брокер отвечает: «дальше ходи ко мне по адресу X». Для вашего компьютера X = `localhost:9092`, для соседних контейнеров (Kafka UI, QA Burger в Docker) — `kafka:29092`.

> ⚠️ Классическая проблема Kafka в Docker: клиент подключился, а потом падает с ошибками соединения — потому что брокер прислал адрес, недоступный клиенту (например, имя контейнера `kafka`, которое не знает ваш компьютер). Если видите такое — смотрите на advertised listeners.

## QA Burger с Kafka

Остановите обычный запуск курса и запустите приложение с профилем `kafka` (или `docker` — тогда ещё и PostgreSQL, урок 11.5):

```bash
java -jar shop-app/target/shop-app-1.0.0-exec.jar --spring.profiles.active=kafka
```

В логе при оформлении и оплате заказа появятся строки:

```text
KafkaEventPublisher : kafka -> qa-burger.orders ORDER_CREATED order=1
KafkaEventPublisher : kafka -> qa-burger.orders ORDER_PAID order=1
KafkaEventPublisher : kafka <- qa-burger.orders ORDER_PAID order=1
KafkaEventPublisher : kafka -> qa-burger.orders ORDER_COOKING order=1
```

`->` — отправлено в топик, `<-` — прочитано кухней.

## Kafka UI

Откройте http://localhost:8090 → кластер `qaburger` → **Topics** → `qa-burger.orders` → **Messages**:
- видны все сообщения: раздел, offset, ключ, значение (JSON), время;
- фильтр по ключу или содержимому;
- **Produce Message** — отправить своё сообщение в топик (проверка консьюмера руками!);
- **Consumers** — группы (`kitchen`), их отставание (**lag** — сколько сообщений ещё не прочитано).

> 🧪 **Попробуй сам.** Оформите и оплатите заказ на сайте. Найдите его события в Kafka UI по ключу (номер заказа). Затем через **Produce Message** отправьте в топик сообщение с ключом несуществующего заказа `999` и значением `{"type":"ORDER_PAID","orderId":999,"userId":1,"total":1,"status":"PAID","timestamp":"2024-01-01T00:00:00Z"}`. Что произошло в логе QA Burger? Упала ли кухня? (Так тестируют устойчивость консьюмера.)

## Консольные утилиты

Внутри контейнера есть стандартные скрипты Kafka:

```bash
docker exec -it qaburger-kafka /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --list
```

- `docker exec -it qaburger-kafka` — выполнить команду внутри контейнера Kafka;
- `kafka-topics.sh --list` — список топиков;
- `--bootstrap-server localhost:9092` — к какому брокеру подключиться (внутри контейнера он — localhost).

Описание топика (разделы, реплики):

```bash
docker exec -it qaburger-kafka /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --describe --topic qa-burger.orders
```

Читать сообщения с начала, с ключами:

```bash
docker exec -it qaburger-kafka /opt/kafka/bin/kafka-console-consumer.sh \
    --bootstrap-server localhost:9092 --topic qa-burger.orders \
    --from-beginning --property print.key=true
```

- `--from-beginning` — с самого начала журнала (иначе — только новые);
- `--property print.key=true` — печатать ключ;
- остановить — Ctrl+C.

Отправить сообщение (`ключ:значение` построчно):

```bash
docker exec -it qaburger-kafka /opt/kafka/bin/kafka-console-producer.sh \
    --bootstrap-server localhost:9092 --topic qa-burger.orders \
    --property parse.key=true --property key.separator=:
```

Группы и их отставание:

```bash
docker exec -it qaburger-kafka /opt/kafka/bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --describe --group kitchen
```

В Windows PowerShell перенос строки — обратный апостроф `` ` `` вместо `\`, либо пишите команду в одну строку.

```quiz
- q: Что такое advertised listeners?
  options:
    - "Адрес, который брокер сообщает клиентам для дальнейшего подключения"
    - "Список топиков"
    - "Пароль Kafka"
  answer: 0
- q: Как прочитать топик с самого начала консольным консьюмером?
  options: ["--from-beginning", "--all", "--reset"]
  answer: 0
- q: Что показывает lag группы консьюмеров?
  options: ["Сколько сообщений ещё не прочитано группой", "Скорость сети", "Количество разделов"]
  answer: 0
- q: Как вручную проверить, как сервис обработает нестандартное сообщение?
  options: ["Отправить его в топик (Produce Message или console-producer)", "Перезапустить Kafka", "Никак"]
  answer: 0
```
