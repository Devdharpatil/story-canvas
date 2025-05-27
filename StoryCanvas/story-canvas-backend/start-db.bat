@echo off
REM start-db.bat

echo Setting up PostgreSQL database for Pocket Writer...

REM Check if database exists
psql -U postgres -c "SELECT 1 FROM pg_database WHERE datname='pocket_writer_db'" | findstr /r "1 row" > NUL
if errorlevel 1 (
    echo Creating database pocket_writer_db...
    psql -U postgres -c "CREATE DATABASE pocket_writer_db;"
) else (
    echo Database pocket_writer_db already exists.
)

REM Check if user exists
psql -U postgres -c "SELECT 1 FROM pg_roles WHERE rolname='pocket_writer_user'" | findstr /r "1 row" > NUL
if errorlevel 1 (
    echo Creating user pocket_writer_user...
    psql -U postgres -c "CREATE USER pocket_writer_user WITH ENCRYPTED PASSWORD 'postgres';"
    psql -U postgres -c "GRANT ALL PRIVILEGES ON DATABASE pocket_writer_db TO pocket_writer_user;"
    psql -U postgres -d pocket_writer_db -c "GRANT ALL ON SCHEMA public TO pocket_writer_user;"
) else (
    echo User pocket_writer_user already exists.
)

echo Database setup complete.
echo You can now run the application with: gradlew bootRun
pause 