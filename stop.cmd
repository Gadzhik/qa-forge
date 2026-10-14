@echo off
rem ===========================================================================
rem  Остановка курса в Windows: двойной клик по stop.cmd или в терминале: stop.cmd
rem
rem  Останавливает портал (порт 8080) и QA Burger (порт 8081), запущенные start.cmd,
rem  даже если окно, где их запускали, потерялось.
rem  Прогресс не теряется: он сохраняется в %USERPROFILE%\.qa-course\progress.json при каждом действии.
rem  Контейнеры Docker (PostgreSQL, Kafka) этот скрипт не трогает: docker compose down
rem ===========================================================================
setlocal
chcp 65001 >nul

echo Останавливаю курс...
call :stop 8081 "QA Burger" shop-app-1.0.0-exec.jar
call :stop 8080 "Портал курса" portal-1.0.0.jar
echo Готово. Прогресс сохранён в %USERPROFILE%\.qa-course\progress.json
rem --quiet передаёт start.cmd: тогда окно не ждёт нажатия клавиши
if not "%~1"=="--quiet" pause
exit /b 0

rem ---- Подпрограмма: остановить программу на порту %1 (имя %2, jar-файл %3) ----
:stop
set PID=
rem netstat -ano - все соединения с номерами процессов; ищем строку «:порт ... LISTENING».
rem 5-я колонка строки - PID процесса, который слушает порт.
for /f "tokens=5" %%p in ('netstat -ano ^| findstr /r /c:":%1 .*LISTENING"') do if not defined PID set PID=%%p
if not defined PID (
  echo    %~2: не запущен
  exit /b 0
)
rem Проверяем, что это именно наш jar, чтобы не закрыть чужую программу на том же порту
powershell -NoProfile -Command "(Get-CimInstance Win32_Process -Filter 'ProcessId=%PID%').CommandLine" | find /i "%3" >nul
if errorlevel 1 (
  echo    %~2: порт %1 занят другой программой ^(PID %PID%^) - её не трогаю
  exit /b 0
)
rem /F - завершить принудительно: у консольной Java-программы нет окна, чтобы «вежливо» попросить её закрыться.
rem Прогресс при этом не пострадает: портал записывает его сразу после каждого действия.
taskkill /PID %PID% /F >nul 2>&1
echo    %~2: остановлен
exit /b 0
