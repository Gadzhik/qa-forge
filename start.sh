#!/usr/bin/env bash
# ============================================================================
#  Запуск курса на macOS и Linux:   ./start.sh
#  Полная пересборка:               ./start.sh --rebuild
#
#  Что делает скрипт:
#   1. Проверяет, что установлена Java 21+.
#   2. При первом запуске собирает проект (Maven Wrapper скачает всё сам).
#   3. Запускает учебное приложение QA Burger  → http://localhost:8081
#   4. Запускает портал курса                  → http://localhost:8080
#   Остановить всё: Ctrl+C в этом окне, просто закрыть окно терминала
#   или из другого окна: ./stop.sh
#   Прогресс хранится в ~/.qa-course/progress.json и не теряется при перезапуске.
# ============================================================================
set -euo pipefail

# Переходим в папку, где лежит скрипт, — тогда его можно запускать откуда угодно
cd "$(dirname "$0")"

# ---- 1. Java -----------------------------------------------------------------
if ! command -v java >/dev/null 2>&1; then
  echo "❌ Java не найдена. Установите JDK 21 (урок 0.2 «Установка инструментов»):"
  echo "   macOS:  brew install --cask temurin@21"
  echo "   Ubuntu: sudo apt install openjdk-21-jdk"
  exit 1
fi
# "java -version" печатает версию в поток ошибок, поэтому 2>&1
JAVA_MAJOR=$(java -version 2>&1 | head -n1 | sed -E 's/.*version "([0-9]+).*/\1/')
if [ "${JAVA_MAJOR:-0}" -lt 21 ]; then
  echo "❌ Нужна Java 21 или новее, а установлена $JAVA_MAJOR. См. урок 0.2."
  exit 1
fi

chmod +x mvnw   # на случай, если архив распаковался без права на запуск

# ---- 2. Сборка -----------------------------------------------------------------
SHOP_JAR=shop-app/target/shop-app-1.0.0-exec.jar
PORTAL_JAR=portal/target/portal-1.0.0.jar
if [ "${1:-}" = "--rebuild" ] || [ ! -f "$SHOP_JAR" ] || [ ! -f "$PORTAL_JAR" ]; then
  echo "🔨 Собираю проект (первый раз это занимает 3–10 минут: скачиваются библиотеки)..."
  # -pl  — какие модули собрать, -am — и всё, от чего они зависят
  # -Dmaven.test.skip=true — тесты сейчас не компилируем и не запускаем (это делают проверки заданий)
  ./mvnw -q install -Dmaven.test.skip=true -pl shop-app,portal,exercises/test-support -am
fi

# ---- 3. Курс уже запущен? ------------------------------------------------------------
# curl -s  — тихо; -o /dev/null — ответ не нужен; если запрос прошёл, значит портал уже работает
if curl -s -o /dev/null http://localhost:8080 2>/dev/null; then
  echo "ℹ️  Курс уже запущен: http://localhost:8080"
  echo "   Чтобы перезапустить, сначала остановите его: ./stop.sh"
  exit 0
fi

mkdir -p logs

# ---- 4. QA Burger ----------------------------------------------------------------
echo "🍔 Запускаю QA Burger на http://localhost:8081 (лог: logs/shop-app.out)"
# & — запустить в фоне, чтобы скрипт пошёл дальше; $! — номер (PID) этого процесса
java -jar "$SHOP_JAR" > logs/shop-app.out 2>&1 &
SHOP_PID=$!
echo "$SHOP_PID" > logs/shop-app.pid   # PID-файл нужен stop.sh, чтобы знать, кого останавливать
PORTAL_PID=""
OPENER_PID=""

# Останавливаем оба приложения, как бы ни завершился скрипт:
#   EXIT — скрипт закончился сам; INT — Ctrl+C; TERM — команда kill / stop.sh;
#   HUP  — закрыли окно терминала.
cleanup() {
  trap - EXIT INT TERM HUP          # снимаем ловушку, чтобы cleanup не вызвался дважды
  set +e                            # дальше ошибки не прерывают уборку (окно терминала может быть уже закрыто)
  echo
  echo "⏹  Останавливаю курс..."
  [ -n "$OPENER_PID" ] && kill "$OPENER_PID" 2>/dev/null || true
  [ -n "$PORTAL_PID" ] && kill "$PORTAL_PID" 2>/dev/null || true
  kill "$SHOP_PID" 2>/dev/null || true
  wait 2>/dev/null || true          # ждём, пока Java-процессы корректно завершатся
  rm -f logs/shop-app.pid logs/portal.pid
  echo "✅ Курс остановлен. Прогресс сохранён в ~/.qa-course/progress.json"
}
trap cleanup EXIT INT TERM HUP

# ---- 5. Портал -------------------------------------------------------------------
(
  # Ждём, пока портал начнёт отвечать, и открываем браузер
  for _ in $(seq 1 60); do
    if curl -s -o /dev/null http://localhost:8080 2>/dev/null; then break; fi
    sleep 1
  done
  if command -v open >/dev/null 2>&1; then open http://localhost:8080          # macOS
  elif command -v xdg-open >/dev/null 2>&1; then xdg-open http://localhost:8080 >/dev/null 2>&1  # Linux
  fi
) &
OPENER_PID=$!

echo "📚 Запускаю портал курса на http://localhost:8080"
echo "   Остановить: Ctrl+C, закрыть это окно или ./stop.sh"
# Портал тоже в фоне, а скрипт ждёт его через wait: так ловушка срабатывает сразу,
# а не только после того, как Java закончит работу
java -jar "$PORTAL_JAR" &
PORTAL_PID=$!
echo "$PORTAL_PID" > logs/portal.pid
wait "$PORTAL_PID" || true
