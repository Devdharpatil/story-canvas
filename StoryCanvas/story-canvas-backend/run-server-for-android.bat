@echo off
REM run-server-for-android.bat - Starts the Pocket Writer Backend on a specific port with network binding

echo Starting Pocket Writer Backend for Android connection...

cd %~dp0

REM Make sure application.properties doesn't exist to avoid conflicts
if exist "src\main\resources\application.properties" (
    echo Removing application.properties to avoid conflicts with application.yml...
    del "src\main\resources\application.properties"
)

REM Enable delayed expansion for variables inside loops
setlocal enabledelayedexpansion

REM Get the current computer's IP address
set "IP_ADDRESS=Unknown"
for /f "tokens=2 delims=:" %%a in ('ipconfig ^| findstr /c:"IPv4 Address"') do (
    set "IP_ADDRESS=%%a"
    set "IP_ADDRESS=!IP_ADDRESS:~1!"
    goto :found_ip
)
:found_ip

echo Computer IP address appears to be: %IP_ADDRESS%

REM Update application.yml to use specific port and bind to all interfaces
echo Configuring application to use port 8080 and bind to all interfaces...
powershell -Command "(Get-Content src\main\resources\application.yml) -replace 'server:\s*\n\s*port:\s*\d+', 'server:\n  port: 8080\n  address: 0.0.0.0' | Set-Content src\main\resources\application.yml"

echo Running application with Spring Boot...
start "Pocket Writer Backend" cmd /c "gradlew.bat bootRun"

echo Waiting for application to start...
timeout /t 5 /nobreak > NUL

echo Application is starting up...
echo.
echo Access URLs:
echo - From emulator:   http://10.0.2.2:8080/api/ping
echo - From local PC:   http://localhost:8080/api/ping
echo - From WiFi network: http://%IP_ADDRESS%:8080/api/ping
echo - Swagger UI:     http://localhost:8080/swagger-ui/index.html
echo.
echo Press Ctrl+C to stop the application (or close the terminal window).
pause 