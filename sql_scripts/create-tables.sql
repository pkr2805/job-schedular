-- Run this script as the postgres superuser in pgAdmin or another PostgreSQL client

-- Create the jar_file table
CREATE TABLE jar_file (
    id UUID PRIMARY KEY,
    name VARCHAR(255),
    description VARCHAR(255),
    path VARCHAR(255),
    size BIGINT NOT NULL,
    uploaded_at TIMESTAMP
);

-- Create the job_schedule table
CREATE TABLE job_schedule (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    scheduled_time TIMESTAMP,
    status VARCHAR(255) CHECK (status IN ('SCHEDULED', 'RUNNING', 'COMPLETED', 'FAILED', 'CANCELLED')),
    execution_type VARCHAR(255) CHECK (execution_type IN ('IMMEDIATE', 'SCHEDULED')),
    recurrence_type VARCHAR(255) CHECK (recurrence_type IN ('ONE_TIME', 'HOURLY', 'DAILY', 'WEEKLY')),
    jar_file_id UUID REFERENCES jar_file(id)
);

-- Create the job_execution table
CREATE TABLE job_execution (
    id UUID PRIMARY KEY,
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    status VARCHAR(255) CHECK (status IN ('STARTED', 'COMPLETED', 'FAILED')),
    execution_time VARCHAR(255),
    logs TEXT,
    response TEXT,
    error_message TEXT,
    job_schedule_id UUID REFERENCES job_schedule(id)
);

-- Grant all privileges on these tables to jobscheduler_user
GRANT ALL PRIVILEGES ON TABLE jar_file TO jobscheduler_user;
GRANT ALL PRIVILEGES ON TABLE job_schedule TO jobscheduler_user;
GRANT ALL PRIVILEGES ON TABLE job_execution TO jobscheduler_user;
