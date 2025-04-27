-- Step 1: Create the database if it doesn't exist
CREATE DATABASE jobscheduler;

-- Step 2: Create a dedicated user for the application
CREATE USER jobscheduler_user WITH PASSWORD 'jobscheduler_password';

-- Step 3: Grant privileges to the user
GRANT ALL PRIVILEGES ON DATABASE jobscheduler TO jobscheduler_user;

-- The following commands need to be run after connecting to the jobscheduler database
-- You can run these in pgAdmin after selecting the jobscheduler database

-- Step 4: Create extensions if needed
-- CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Step 5: Grant schema privileges (run these after the application has created the tables)
-- GRANT ALL PRIVILEGES ON SCHEMA public TO jobscheduler_user;
-- GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO jobscheduler_user;
-- GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO jobscheduler_user;
-- GRANT ALL PRIVILEGES ON ALL FUNCTIONS IN SCHEMA public TO jobscheduler_user;
