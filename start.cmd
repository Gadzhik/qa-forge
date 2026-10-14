@echo off
rem ===========================================================================
rem  Запуск курса в Windows: двойной клик по start.cmd или в терминале: start.cmd
rem  Полная пересборка:      start.cmd --rebuild
rem
rem  1. Проверяет Java 21+.
rem  2. При первом запуске собирает проект (Maven Wrapper скачает всё сам).
rem  3. Запускает QA Burger                    -> http://localhost:8081
rem  4. Запускает портал курса                 -> http://localhost:8080
rem  Остановить всё: закройте это окно, нажмите Ctrl+C или запустите stop.cmd.
rem  Прогресс хранится в %USERPROFILE%\.qa-course\progress.json и не теряется.
rem ===========================================================================
setlocal
rem Русские буквы в консоли
chcp 65001 >nul
rem Переходим в папку со скриптом
cd /d "%~dp0"

where java >nul 2>nul
if errorlevel 1 (
  echo [X] Java не найдена. Установите JDK 21: урок 0.2 "Установка инструментов".
  echo     Например: winget install EclipseAdoptium.Temurin.21.JDK
  pause
  exit /b 1
)

rem Достаём "главный" номер версии Java из вывода java -version
for /f "tokens=3" %%v in ('java -version 2^>^&1 ^| findstr /i "version"') do set JAVA_VER=%%~v
for /f "delims=." %%m in ("%JAVA_VER%") do set JAVA_MAJOR=%%m
if %JAVA_MAJOR% LSS 21 (
  echo [X] Нужна Java 21 или новее, а установлена %JAVA_VER%. См. урок 0.2.
  pause
  exit /b 1
)

set SHOP_JAR=shop-app\target\shop-app-1.0.0-exec.jar
set PORTAL_JAR=portal\target\portal-1.0.0.jar

set NEED_BUILD=0
if "%~1"=="--rebuild" set NEED_BUILD=1
if not exist "%SHOP_JAR%" set NEED_BUILD=1
if not exist "%PORTAL_JAR%" set NEED_BUILD=1
if %NEED_BUILD%==1 (
  echo Собираю проект. Первый раз это 3-10 минут: скачиваются библиотеки...
  call mvnw.cmd -q install -Dmaven.test.skip=true -pl shop-app,portal,exercises/test-support -am
  if errorlevel 1 (
    echo [X] Сборка не удалась. Сообщения об ошибках выше.
    pause
    exit /b 1
  )
)

rem Курс уже запущен? curl есть в Windows 10 и новее. Если портал ответил - второй раз не запускаем.
curl -s -o nul http://localhost:8080 2>nul
if not errorlevel 1 (
  echo Курс уже запущен: http://localhost:8080
  echo Чтобы перезапустить, сначала остановите его: stop.cmd
  start http://localhost:8080
  pause
  exit /b 0
)

if not exist logs mkdir logs

rem start /b - запустить в ЭТОМ же окне, в фоне. Когда окно закроют, Windows
rem завершит все программы, привязанные к нему: и QA Burger, и портал.
echo Запускаю QA Burger на http://localhost:8081 (лог: logs\shop-app.out)
start "" /b java -jar "%SHOP_JAR%" > logs\shop-app.out 2>&1

rem Откроем браузер примерно через 15 секунд, когда портал успеет стартовать
rem (ping 16 раз по 1 секунде - простой способ подождать в cmd)
start "" /b cmd /c "ping -n 16 127.0.0.1 >nul & start http://localhost:8080"

echo Запускаю портал курса на http://localhost:8080
echo Остановить: закройте это окно, Ctrl+C или stop.cmd
java -jar "%PORTAL_JAR%"

rem Портал завершился (Ctrl+C или stop.cmd) - останавливаем и QA Burger
call "%~dp0stop.cmd" --quiet
