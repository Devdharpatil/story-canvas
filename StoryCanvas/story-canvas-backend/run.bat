@echo off
echo ===== Running Pocket Writer Backend =====

REM Run the application with DevTools disabled
set SPRING_DEVTOOLS_RESTART_ENABLED=false
set JAVA_OPTS=-Xmx512m
cd /d "%~dp0"
gradlew bootRun

echo ===== Backend stopped =====
pause 