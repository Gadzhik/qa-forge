#!/usr/bin/env bash
# ============================================================================
#  Остановка курса на macOS и Linux:   ./stop.sh
#
#  Останавливает портал (порт 8080) и QA Burger (порт 8081), запущенные start.sh,
#  даже если окно, где их запускали, уже закрыто или потерялось.
#  Прогресс не теряется: он сохраняется в ~/.qa-course/progress.json при каждом действии.
#  Контейнеры Docker (PostgreSQL, Kafka) этот скрипт не трогает: docker compose down
# ============================================================================
set -uo pipefail                 # без -e: если процесса уже нет, это не ошибка
cd "$(dirname "$0")"

# Номер процесса, который слушает порт. lsof есть в macOS и большинстве Linux, ss — в Linux.
pid_on_port() {
  if command -v lsof >/dev/null 2>&1; then
    lsof -t -iTCP:"$1" -sTCP:LISTEN 2>/dev/null | head -n1
  elif command -v ss >/dev/null 2>&1; then
    ss -ltnpH "sport = :$1" 2>/dev/null | grep -o 'pid=[0-9]*' | head -n1 | cut -d= -f2
  fi
}

# Останавливаем процесс, только если это действительно наш jar —
# чтобы случайно не закрыть чужую программу на том же порту.
stop_one() {
  local name=$1 port=$2 jar=$3 pidfile=$4 pid=""
  [ -f "$pidfile" ] && pid=$(cat "$pidfile")
  # PID-файла нет или процесс уже умер — ищем по порту
  if [ -z "$pid" ] || ! kill -0 "$pid" 2>/dev/null; then
    pid=$(pid_on_port "$port")
  fi
  if [ -z "$pid" ]; then
    echo "   $name: не запущен"
    rm -f "$pidfile"
    return
  fi
  # ps -o args= — полная командная строка процесса без заголовка
  if ! ps -p "$pid" -o args= 2>/dev/null | grep -q "$jar"; then
    echo "   $name: порт $port занят другой программой (PID $pid) — её не трогаю"
    return
  fi
  kill "$pid"                     # вежливая просьба завершиться (сигнал TERM)
  for _ in $(seq 1 15); do        # ждём до 15 секунд
    kill -0 "$pid" 2>/dev/null || break
    sleep 1
  done
  kill -0 "$pid" 2>/dev/null && kill -9 "$pid"   # не послушался — завершаем принудительно
  rm -f "$pidfile"
  echo "   $name: остановлен"
}

echo "⏹  Останавливаю курс..."
stop_one "QA Burger"    8081 "shop-app-1.0.0-exec.jar" logs/shop-app.pid
stop_one "Портал курса" 8080 "portal-1.0.0.jar" logs/portal.pid
echo "✅ Готово. Прогресс сохранён в ~/.qa-course/progress.json"
