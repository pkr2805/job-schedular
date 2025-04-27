@echo off
echo Running SQL script to fix PostgreSQL permissions...

REM Set the PostgreSQL password as an environment variable
set PGPASSWORD=postgres

REM Run the SQL script
psql -U postgres -d jobscheduler -f fix-permissions.sql

REM Reset the password environment variable
set PGPASSWORD=

echo.
echo Script execution completed.
echo Press any key to exit...
pause > nul
