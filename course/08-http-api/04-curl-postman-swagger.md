---
title: "8.4 curl, Postman и Swagger: ручные запросы"
minutes: 45
---

Прежде чем автоматизировать API-проверку, её нужно выполнить руками: понять, какой запрос отправить и что придёт в ответ. Для этого есть три инструмента.

## Swagger UI — проще всего

Откройте http://localhost:8081/swagger-ui.html.

1. Разверните **Товары → GET /api/products**.
2. **Try it out** — поля станут редактируемыми.
3. Заполните `category` = `DRINK`, `size` = `3`.
4. **Execute**.

Swagger покажет:
- **Curl** — готовую команду для терминала;
- **Request URL** — итоговый адрес;
- **Server response** — код, тело и заголовки ответа.

**Авторизация в Swagger:** выполните `POST /api/auth/login` с телом `{"username": "user", "password": "user123"}`, скопируйте `token` из ответа. Для запросов с токеном в Swagger можно передать заголовок, но проще в curl или Postman (ниже).

## curl — запросы из терминала

**curl** — консольная утилита для HTTP-запросов, есть во всех ОС (в Windows 10+ встроена). Её используют все: разработчики, тестировщики, DevOps. Команду curl прикладывают к баг-репортам (урок 2.5 → Copy as cURL).

> ⚠️ **Windows PowerShell:** слово `curl` там — псевдоним другой команды (`Invoke-WebRequest`) с другими опциями. Пишите **`curl.exe`**. Кроме того, кавычки внутри JSON в PowerShell приходится экранировать — поэтому для запросов с телом ниже есть отдельные вкладки. В Git Bash работают команды как для macOS/Linux.

### GET

```tabs
macOS / Linux / Git Bash: |
  ~~~bash
  curl http://localhost:8081/api/products/1
  ~~~
Windows PowerShell: |
  ~~~powershell
  curl.exe http://localhost:8081/api/products/1
  ~~~
```

Без опций curl делает GET и печатает тело ответа.

```bash
curl -i "http://localhost:8081/api/products?category=DRINK&size=2"
```

- `-i` (*include*) — показать и **заголовки ответа** (код, Content-Type...);
- URL с `?` и `&` — **в кавычках**: иначе терминал воспримет `&` как «запустить в фоне».

```bash
curl -v http://localhost:8081/api/products/999
```

`-v` (*verbose*) — показать **всё**: и запрос (строки с `>`), и ответ (строки с `<`). Лучший способ понять, что на самом деле уходит на сервер.

```bash
curl -s http://localhost:8081/api/products/1 | jq .
```

- `-s` (*silent*) — без индикатора загрузки;
- `| jq .` — передать ответ утилите **jq**, которая красиво форматирует JSON (её нужно установить отдельно: `brew install jq`, `sudo apt install jq`, `winget install jqlang.jq`). Можно и без неё.

### POST с JSON — вход

```tabs
macOS / Linux / Git Bash: |
  ~~~bash
  curl -X POST http://localhost:8081/api/auth/login \
       -H "Content-Type: application/json" \
       -d '{"username": "user", "password": "user123"}'
  ~~~

  - `-X POST` — метод (с `-d` curl и так сделает POST, но явно — понятнее);
  - `-H "Имя: значение"` — заголовок; здесь сообщаем, что тело — JSON;
  - `-d '...'` (*data*) — тело запроса; JSON в **одинарных** кавычках, тогда двойные внутри не мешают;
  - `\` в конце строки — команда продолжается на следующей строке.
Windows PowerShell: |
  ~~~powershell
  curl.exe -X POST http://localhost:8081/api/auth/login `
       -H "Content-Type: application/json" `
       -d '{\"username\": \"user\", \"password\": \"user123\"}'
  ~~~

  - перенос строки в PowerShell — обратный апостроф `` ` ``;
  - кавычки внутри JSON приходится экранировать `\"` — особенность передачи аргументов в PowerShell.

  Удобнее положить тело в файл `login.json` и отправить его: `curl.exe -X POST ... -d "@login.json"` (`@` — «взять содержимое файла»).
```

Ответ: `{"token":"eyJhbGciOiJIUzI1NiIs...","type":"Bearer"}`.

### Запрос с токеном

Сохраним токен в переменную и используем:

```tabs
macOS / Linux / Git Bash: |
  ~~~bash
  TOKEN=$(curl -s -X POST http://localhost:8081/api/auth/login \
    -H "Content-Type: application/json" \
    -d '{"username":"user","password":"user123"}' | jq -r .token)

  curl -X POST http://localhost:8081/api/cart/items \
       -H "Authorization: Bearer $TOKEN" \
       -H "Content-Type: application/json" \
       -d '{"productId": 1, "quantity": 2}'
  ~~~

  - `$( ... )` — выполнить команду и подставить результат;
  - `jq -r .token` — достать поле `token` из JSON (`-r` — без кавычек);
  - `$TOKEN` внутри двойных кавычек подставится.
Windows PowerShell: |
  ~~~powershell
  $login = Invoke-RestMethod -Method Post -Uri http://localhost:8081/api/auth/login `
           -ContentType "application/json" -Body '{"username":"user","password":"user123"}'
  $token = $login.token

  Invoke-RestMethod -Method Post -Uri http://localhost:8081/api/cart/items `
      -Headers @{ Authorization = "Bearer $token" } `
      -ContentType "application/json" -Body '{"productId": 1, "quantity": 2}'
  ~~~

  `Invoke-RestMethod` — встроенная в PowerShell команда для API: сама разбирает JSON-ответ в объект, поэтому `$login.token` сразу достаёт поле. Внутри `-Body '...'` экранировать кавычки не нужно.
```

### Другие методы

```bash
curl -X DELETE http://localhost:8081/api/cart -H "Authorization: Bearer $TOKEN"
curl -X POST http://localhost:8081/api/orders -H "Authorization: Bearer $TOKEN" \
     -H "Content-Type: application/json" -d '{"promoCode": "SALE10"}'
curl -o receipt.pdf http://localhost:8081/api/orders/1/receipt.pdf -H "Authorization: Bearer $TOKEN"
curl -F "file=@menu.txt" http://localhost:8081/api/files
curl -u admin:admin http://localhost:8081/playground/basic-auth
```

- `-o файл` — сохранить ответ в файл (скачивание);
- `-F "file=@путь"` — отправить файл как `multipart/form-data` (загрузка);
- `-u логин:пароль` — Basic-авторизация (урок 8.5).

### Шпаргалка curl

| Опция | Что делает |
|---|---|
| `-X МЕТОД` | Метод запроса |
| `-H "Имя: значение"` | Заголовок |
| `-d 'тело'` / `-d @файл` | Тело запроса |
| `-i` | Показать заголовки ответа |
| `-v` | Подробно: весь запрос и ответ |
| `-s` | Тихо |
| `-o файл` | Сохранить ответ в файл |
| `-F "поле=@файл"` | Загрузить файл |
| `-u логин:пароль` | Basic Auth |
| `-L` | Следовать перенаправлениям 3xx |
| `-k` | Не проверять HTTPS-сертификат (только для тестовых стендов!) |
| `-w "%{http_code}"` | Напечатать код ответа |

## Postman — графический клиент

**Postman** (https://www.postman.com/downloads/) — популярная программа для ручной работы с API. Альтернативы: Insomnia, Bruno (открытый, хранит запросы в файлах), встроенный HTTP Client в IntelliJ IDEA Ultimate.

Базовая работа:
1. **New → HTTP Request**.
2. Выберите метод, вставьте URL.
3. Вкладка **Params** — query-параметры таблицей.
4. Вкладка **Headers** — заголовки.
5. Вкладка **Body → raw → JSON** — тело (Postman сам поставит `Content-Type`).
6. Вкладка **Authorization → Bearer Token** — вставьте токен.
7. **Send**.

Полезное:
- **Collections** — сохранять запросы папками («QA Burger → Корзина»);
- **Environments** — переменные окружения: `{{baseUrl}}`, `{{token}}`; переключение между local и stage одним кликом;
- **Tests** — маленькие JavaScript-проверки ответа прямо в Postman;
- **Import → Raw text** — вставить curl-команду, Postman превратит её в запрос; **Code** (`</>`) — наоборот, получить curl или Java-код из запроса;
- **Import → OpenAPI** (http://localhost:8081/v3/api-docs) — получить коллекцию со всеми ручками QA Burger.

> 💡 Postman хорош для исследования API, но не заменяет автотесты в коде: их сложнее хранить в git, ревьюить и запускать в CI вместе с остальными тестами.

> 🧪 **Попробуй сам.** Пройдите через API весь путь покупателя: вход → добавить 2 товара в корзину → посмотреть корзину → оформить заказ с SALE10 → оплатить → через 5 секунд проверить статус (READY). Сделайте это в curl или Postman и сохраните команды — это ваш первый API-сценарий, который в модуле 9 станет автотестом.

```quiz
- q: Как в PowerShell вызвать настоящий curl?
  options: ["curl.exe", "curl", "Invoke-curl"]
  answer: 0
- q: Какая опция curl показывает весь запрос и ответ?
  options: ["-v", "-s", "-o"]
  answer: 0
- q: Почему URL с & берут в кавычки?
  options:
    - "Иначе терминал воспримет & как команду «запустить в фоне»"
    - "Так требует сервер"
    - "Для красоты"
  answer: 0
- q: Как в curl передать заголовок Authorization?
  options: ['-H "Authorization: Bearer ТОКЕН"', '-a ТОКЕН', '-d ТОКЕН']
  answer: 0
- q: Как в Postman быстро переключаться между стендами local и stage?
  options: ["Environments с переменными вроде {{baseUrl}}", "Переписывать URL в каждом запросе", "Никак"]
  answer: 0
```
