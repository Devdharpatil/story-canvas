@echo off
REM start-reliable.bat - Starts the Pocket Writer Backend on a random available port

echo Starting Pocket Writer Backend with dynamic port allocation...

cd %~dp0

REM Make sure application.properties doesn't exist to avoid conflicts
if exist "src\main\resources\application.properties" (
    echo Removing application.properties to avoid conflicts with application.yml...
    del "src\main\resources\application.properties"
)

REM Update application.yml to use dynamic port
echo Configuring application to use dynamic port...
powershell -Command "(Get-Content src\main\resources\application.yml) -replace 'server:\s*\n\s*port:\s*\d+', 'server:\n  port: 0' | Set-Content src\main\resources\application.yml"

echo Running application with Spring Boot...
start "Pocket Writer Backend" cmd /c "gradlew.bat bootRun > backend_log.txt 2>&1"

echo Waiting for application to start...
timeout /t 5 /nobreak > NUL

echo Finding the assigned port...
powershell -Command "Get-Content -Wait backend_log.txt | Select-String -Pattern 'Tomcat started on port (\d+)' -Context 0,1 | ForEach-Object { $_.Matches.Groups[1].Value } | Select-Object -First 1" > port.txt

set /p PORT=<port.txt
if "%PORT%"=="" (
    echo Could not determine the port. Check backend_log.txt for details.
) else (
    echo Application is running on port %PORT%
    echo Access URLs:
    echo - Local:      http://localhost:%PORT%/api/ping
    echo - Health:     http://localhost:%PORT%/api/health
    echo - Swagger UI: http://localhost:%PORT%/swagger-ui/index.html
    
    REM Open the Swagger UI in the default browser
    start http://localhost:%PORT%/swagger-ui/index.html
)

echo Press Ctrl+C to stop the application (or close the terminal window).
pause 