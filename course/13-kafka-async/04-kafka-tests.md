---
title: "13.4 Kafka в автотестах"
minutes: 50
---

## Подготовка

```bash
docker compose up -d kafka
java -jar shop-app/target/shop-app-1.0.0-exec.jar --spring.profiles.active=kafka
```

Тест читает топик `qa-burger.orders` клиентом `kafka-clients`:

```xml
<dependency>
    <groupId>org.apache.kafka</groupId>
    <artifactId>kafka-clients</artifactId>
    <scope>test</scope>
</dependency>
```

## Консьюмер в тесте

```java
Properties props = new Properties();
props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, ShopConfig.kafkaBootstrap());
props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
consumer = new KafkaConsumer<>(props);
```

- **`bootstrap.servers`** — адрес брокера (`localhost:9092`, из конфигурации);
- **десериализаторы** — как превратить байты ключа и значения в Java-объекты; у нас строки (JSON потом разберём Jackson'ом);
- `enable.auto.commit=false` — тест ничего не «отмечает» в Kafka.

### Главная ловушка: старые сообщения

Топик хранит **все** прошлые события. Если тест прочитает с начала, он найдёт события заказа №1 из вчерашнего прогона — и пройдёт, даже если сегодня событие не отправилось (именно так при подготовке курса проверка поначалу находила «ложные» события). Решение — **читать только новое**: до действия встать в конец топика.

```java
List<TopicPartition> partitions = consumer.partitionsFor(TOPIC, Duration.ofSeconds(10)).stream()
        .map(p -> new TopicPartition(TOPIC, p.partition()))
        .toList();
consumer.assign(partitions);
consumer.seekToEnd(partitions);
partitions.forEach(consumer::position);
```

- `partitionsFor(...)` — узнать разделы топика;
- **`assign(...)`** — читать эти разделы напрямую, **без группы**: не нужно ждать распределения разделов (rebalance) и тест не мешает настоящим потребителям;
- **`seekToEnd(...)`** — перемотать в конец;
- `position(...)` — Kafka перематывает «лениво»; запрос позиции заставляет зафиксировать её **сейчас**, до создания заказа. Без этой строки перемотка может случиться уже после того, как событие отправлено, и тест его пропустит.

Альтернатива — `subscribe` с уникальным `group.id` и `auto.offset.reset=latest`, но тогда нужно дождаться назначения разделов, прежде чем действовать.

## Действие и чтение событий

```java
int id = ... оформить и оплатить заказ через API ...;

Map<String, JsonNode> events = new LinkedHashMap<>();
await().atMost(Duration.ofSeconds(20)).pollInterval(Duration.ofMillis(200)).untilAsserted(() -> {
    for (ConsumerRecord<String, String> record : consumer.poll(Duration.ofMillis(500))) {
        if (String.valueOf(id).equals(record.key())) {
            JsonNode event = mapper.readTree(record.value());
            events.put(event.get("type").asText(), event);
        }
    }
    assertThat(events).containsKeys("ORDER_CREATED", "ORDER_PAID");
});

JsonNode paid = events.get("ORDER_PAID");
assertThat(paid.get("status").asText()).isEqualTo("PAID");
assertThat(paid.get("total").decimalValue()).isPositive();
```

- **`consumer.poll(500 мс)`** — забрать пачку новых сообщений (ждёт до 500 мс, если их нет);
- **фильтр по ключу** — в топике события всех заказов; нам нужны только наши (ключ = id заказа);
- события накапливаются в `Map` между итерациями Awaitility, поэтому не теряются;
- проверка повторяется, пока не появятся оба события или не выйдет время;
- затем проверяем **содержимое** события — это проверка **контракта** сообщения.

С BUG-501 событие ORDER_PAID не отправляется — тест падает по таймауту с понятным сообщением: в `events` есть только ORDER_CREATED.

## Тест как продюсер

Проверка консьюмера: положить сообщение и убедиться, что сервис его обработал.

```java
Properties p = new Properties();
p.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, ShopConfig.kafkaBootstrap());
p.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
p.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

try (KafkaProducer<String, String> producer = new KafkaProducer<>(p)) {
    String json = """
            {"type":"ORDER_PAID","orderId":%d,"userId":2,"total":99.0,"status":"PAID","timestamp":"2024-01-01T00:00:00Z"}
            """.formatted(id);
    producer.send(new ProducerRecord<>(TOPIC, String.valueOf(id), json)).get();
}
await().atMost(...).untilAsserted(() -> assertThat(status(id)).isEqualTo("COOKING"));
```

- `ProducerRecord(топик, ключ, значение)`;
- `send(...).get()` — `send` асинхронный; `.get()` дожидается подтверждения от брокера.

## Testcontainers — Kafka прямо из теста

Вместо ручного `docker compose up` тест может сам поднять Kafka в Docker (модуль 15):

```java
@Container
static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("apache/kafka:3.9.1"));
// kafka.getBootstrapServers() → адрес для клиентов
```

## Советы

- Таймауты чтения — с запасом: первая `poll` у нового клиента может занимать секунды (подключение, метаданные).
- Закрывайте консьюмеры и продюсеры (`@AfterEach`).
- Разбирайте значение в модель (record + Jackson) — тогда контракт сообщения проверяется полностью.
- Не используйте общую `group.id` с настоящими сервисами — вы «украдёте» их сообщения.

```task
id: m13-t04
title: События заказа в Kafka
module: exercises/m13-kafka-async
test: KafkaEventsTestCheck
file: exercises/m13-kafka-async/src/test/java/ru/qacourse/m13/tasks/KafkaEventsTest.java
solution: solutions/exercises/m13-kafka-async/src/test/java/ru/qacourse/m13/tasks/KafkaEventsTest.java
text: |
  Нужна Kafka: `docker compose up -d kafka`. Тест: consumer на все разделы `qa-burger.orders` с перемоткой в конец →
  оформить и оплатить заказ → в Awaitility читать события своего заказа до ORDER_CREATED и ORDER_PAID → проверить поля ORDER_PAID.

  Для ручного запуска из IDEA запустите QA Burger с `--spring.profiles.active=kafka`. Проверка поднимет его сама.
  Ловит BUG-501.
hint: |
  Код целиком разобран в уроке. Не забудьте `partitions.forEach(consumer::position)` после `seekToEnd`.
```

```quiz
- q: Почему тест должен перемотать консьюмер в конец топика перед действием?
  options:
    - "Иначе он найдёт старые события прошлых прогонов и пройдёт ложно"
    - "Так быстрее"
    - "Kafka требует"
  answer: 0
- q: Чем assign отличается от subscribe?
  options:
    - "assign читает заданные разделы напрямую без группы и rebalance"
    - "Ничем"
    - "assign только для продюсеров"
  answer: 0
- q: Как отфильтровать события своего заказа?
  options: ["По ключу сообщения (id заказа)", "По времени", "Никак"]
  answer: 0
- q: Что делает producer.send(...).get()?
  options: ["Отправляет и ждёт подтверждения брокера", "Читает сообщение", "Удаляет топик"]
  answer: 0
```
