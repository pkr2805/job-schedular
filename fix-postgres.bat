@echo off
echo Fixing PostgreSQL database and user for Job Scheduler...

REM Create a temporary SQL file
echo -- Drop the user if it exists > fix-postgres.sql
echo DROP USER IF EXISTS jobscheduler_user; >> fix-postgres.sql
echo. >> fix-postgres.sql
echo -- Create the database if it doesn't exist >> fix-postgres.sql
echo DROP DATABASE IF EXISTS jobscheduler; >> fix-postgres.sql
echo CREATE DATABASE jobscheduler; >> fix-postgres.sql
echo. >> fix-postgres.sql
echo -- Create the user with a new password >> fix-postgres.sql
echo CREATE USER jobscheduler_user WITH PASSWORD 'jobscheduler_password'; >> fix-postgres.sql
echo. >> fix-postgres.sql
echo -- Grant privileges to the user >> fix-postgres.sql
echo GRANT ALL PRIVILEGES ON DATABASE jobscheduler TO jobscheduler_user; >> fix-postgres.sql
echo. >> fix-postgres.sql
echo -- Make the user the owner of the database >> fix-postgres.sql
echo ALTER DATABASE jobscheduler OWNER TO jobscheduler_user; >> fix-postgres.sql

REM Run the SQL script as the postgres user
echo Running SQL commands as postgres user...
psql -U postgres -f fix-postgres.sql

REM Connect to the jobscheduler database and grant schema privileges
echo Granting schema privileges...
echo -- Grant schema privileges > fix-schema.sql
echo GRANT ALL ON SCHEMA public TO jobscheduler_user; >> fix-schema.sql
echo ALTER ROLE jobscheduler_user WITH LOGIN; >> fix-schema.sql

psql -U postgres -d jobscheduler -f fix-schema.sql

if %ERRORLEVEL% EQU 0 (
    echo Database setup completed successfully.
) else (
    echo Failed to set up database. Please check the error message above.
)

REM Clean up temporary files
del fix-postgres.sql
del fix-schema.sql

echo.
echo Press any key to exit...
pause > nul
