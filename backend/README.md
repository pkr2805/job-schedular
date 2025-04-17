# Job Scheduler Backend

A Spring Boot application for scheduling and executing Java JAR files based on various triggers (immediate, scheduled, recurring).

## Features

- **Job Management**: Create, read, update, and delete jobs
- **Job Types**: Support for immediate, scheduled, and recurring jobs
- **Job Status Tracking**: Monitor job status including pending, scheduled, running, completed, failed, etc.
- **JAR File Management**: Upload, download, and manage job JAR files
- **Job Execution**: Execute Java JAR files with arguments
- **Output Capture**: Capture and store job output and errors
- **Scheduled Execution**: Automatically execute jobs based on schedules
- **REST API**: Complete REST API for frontend integration

## Technology Stack

- **Spring Boot 3.x**: For building the REST API and application
- **Spring Data JPA**: For database access
- **PostgreSQL/YugabyteDB**: For persistent storage
- **Spring Validation**: For request validation
- **Spring Actuator**: For application monitoring
- **Spring Scheduler**: For recurring job execution
- **Lombok**: For reducing boilerplate code

## Project Structure

```
backend/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── jobscheduler/
│   │   │           ├── config/          # Application configuration
│   │   │           ├── controller/      # REST API controllers
│   │   │           ├── model/           # Domain models
│   │   │           ├── repository/      # JPA repositories
│   │   │           ├── service/         # Business services
│   │   │           │   └── impl/        # Service implementations
│   │   │           └── JobSchedulerApplication.java
│   │   └── resources/
│   │       └── application.yml          # Application configuration
│   └── test/                            # Test classes
└── binaries/                            # JAR files and job binaries
```

## API Endpoints

### Job Management

- `GET /api/jobs` - Get all jobs
- `GET /api/jobs/{id}` - Get job by ID
- `POST /api/jobs` - Create a new job
- `PUT /api/jobs/{id}` - Update a job
- `DELETE /api/jobs/{id}` - Delete a job

### Job Operations

- `POST /api/jobs/{id}/schedule` - Schedule a job
- `POST /api/jobs/{id}/execute` - Execute a job
- `POST /api/jobs/{id}/pause` - Pause a job
- `POST /api/jobs/{id}/resume` - Resume a job
- `POST /api/jobs/{id}/cancel` - Cancel a job
- `POST /api/jobs/{id}/restart` - Restart a job

### Job Queries

- `GET /api/jobs/status/{status}` - Get jobs by status
- `GET /api/jobs/type/{type}` - Get jobs by type
- `GET /api/jobs/search?keyword=xyz` - Search jobs
- `GET /api/jobs/{id}/output` - Get job output
- `GET /api/jobs/{id}/error` - Get job error

### JAR File Management

- `GET /api/jarfiles` - Get all JAR files
- `GET /api/jarfiles/{id}` - Get JAR file details
- `POST /api/jarfiles` - Upload a JAR file
- `DELETE /api/jarfiles/{id}` - Delete a JAR file
- `GET /api/jarfiles/{id}/download` - Download a JAR file
- `GET /api/jarfiles/{id}/manifest` - Get JAR manifest
- `PUT /api/jarfiles/{id}/description` - Update JAR description

## Setup and Configuration

### Prerequisites

- JDK 17 or higher
- Maven
- PostgreSQL/YugabyteDB

### Database Setup

1. Create a PostgreSQL database:

```sql
CREATE DATABASE jobscheduler;
```

2. Update `application.yml` with your database connection details.

### Building the Application

```bash
mvn clean package
```

### Running the Application

```bash
java -jar target/backend-0.0.1-SNAPSHOT.jar
```

## Job Types

### Immediate

Executes right away. Example:

```json
{
  "name": "Immediate Job",
  "jarFile": "immediate-execution.txt",
  "type": "IMMEDIATE"
}
```

### Scheduled

Executes at a specific time. Example:

```json
{
  "name": "Scheduled Job",
  "jarFile": "channel-subscribe-1.jar",
  "type": "SCHEDULED",
  "scheduledAt": "2023-06-15T14:30:00"
}
```

### Recurring

Executes on a recurring schedule. Example:

```json
{
  "name": "Recurring Job",
  "jarFile": "wake-up-reminder.jar",
  "type": "RECURRING",
  "cronExpression": "0 0 8 * * *"
}
```

## Job Statuses

- `PENDING`: Job created but not yet scheduled
- `SCHEDULED`: Job scheduled for execution
- `RUNNING`: Job currently running
- `COMPLETED`: Job completed successfully
- `FAILED`: Job execution failed
- `CANCELLED`: Job was cancelled by user
- `PAUSED`: Job execution is paused

## Integration with Frontend

The backend is designed to work with a React or Next.js frontend. The API follows REST principles and returns JSON responses. Cross-Origin Resource Sharing (CORS) is configured to allow requests from any origin. 