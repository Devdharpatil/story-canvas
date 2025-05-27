@echo off
REM start-backend.bat

echo Starting Pocket Writer Backend...

cd %~dp0

REM Make sure application.properties doesn't exist to avoid conflicts
if exist "src\main\resources\application.properties" (
    echo Removing application.properties to avoid conflicts with application.yml...
    del "src\main\resources\application.properties"
)

echo Running application with Spring Boot...
gradlew bootRun --stacktrace > backend_log.txt 2>&1

if %errorlevel% neq 0 (
    echo Error starting application. Check backend_log.txt for details.
) else (
    echo Application started successfully. 
    echo The server will be assigned a random available port.
    echo Check the console output or backend_log.txt for the actual port number.
    echo Press Ctrl+C to stop.
)

pause 