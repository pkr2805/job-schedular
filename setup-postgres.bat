@echo off
echo Setting up PostgreSQL database for Job Scheduler...

REM Set PostgreSQL connection parameters
set PGHOST=localhost
set PGPORT=5432
set PGUSER=postgres
set PGPASSWORD=postgres

REM Create the database
echo Creating database...
psql -c "CREATE DATABASE jobscheduler;"

REM Check if database creation was successful
if %ERRORLEVEL% NEQ 0 (
    echo Failed to create database. Make sure PostgreSQL is running and the credentials are correct.
    exit /b 1
)

REM Create a dedicated user for the application
echo Creating application user...
psql -c "CREATE USER jobscheduler_user WITH PASSWORD 'jobscheduler_password';"

REM Grant privileges to the user
echo Granting privileges...
psql -d jobscheduler -c "GRANT ALL PRIVILEGES ON DATABASE jobscheduler TO jobscheduler_user;"
psql -d jobscheduler -c "GRANT ALL PRIVILEGES ON SCHEMA public TO jobscheduler_user;"
psql -d jobscheduler -c "GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO jobscheduler_user;"
psql -d jobscheduler -c "GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO jobscheduler_user;"
psql -d jobscheduler -c "GRANT ALL PRIVILEGES ON ALL FUNCTIONS IN SCHEMA public TO jobscheduler_user;"

REM Create extensions if needed
echo Creating extensions...
psql -d jobscheduler -c "CREATE EXTENSION IF NOT EXISTS \"uuid-ossp\";"

echo PostgreSQL database setup completed successfully.
