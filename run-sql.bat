@echo off
echo Running SQL script to fix PostgreSQL user permissions...

REM Set the PostgreSQL password as an environment variable
set PGPASSWORD=postgres

REM Run the SQL script
psql -U postgres -f fix-user.sql

REM Reset the password environment variable
set PGPASSWORD=

echo.
echo Script execution completed.
echo Press any key to exit...
pause > nul
