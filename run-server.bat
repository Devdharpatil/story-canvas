@echo off
echo Starting Pocket Writer Backend Server...
echo.

cd StoryCanvas\story-canvas-backend

REM Check for existing Java processes that might be our backend
echo Checking for existing backend processes...
for /f "tokens=1" %%i in ('tasklist /FI "IMAGENAME eq java.exe" /NH') do (
    echo Found Java process, checking if it's our backend...
)
echo.

REM Clean and build the project
echo Building the backend server...
call gradlew clean build -x test

if %ERRORLEVEL% NEQ 0 (
    echo Failed to build the backend server. Please check for errors.
    pause
    exit /b %ERRORLEVEL%
)

echo Build successful! Starting the server...
echo.

REM Get the computer's IP address to display
echo Your computer's IP addresses:
ipconfig | findstr /C:"IPv4 Address"
echo.

echo This information might be useful for connecting your Android app.
echo The server will be accessible on all network interfaces on port 8080.
echo.

REM Run the server with explicit configuration to bind to all interfaces
java -jar build\libs\backend-0.0.1-SNAPSHOT.jar ^
    --server.address=0.0.0.0 ^
    --server.port=8080 ^
    --spring.datasource.url=jdbc:postgresql://localhost:5432/pocket_writer_db ^
    --spring.datasource.username=postgres ^
    --spring.datasource.password=postgres:@ ^
    --logging.level.com.pocketwriter.backend=DEBUG

REM Keep the window open if the server crashes
if %ERRORLEVEL% NEQ 0 (
    echo Server stopped with error code %ERRORLEVEL%.
    pause
) 