# Job Scheduler Application

A full-stack job scheduling system that allows users to schedule execution of pre-uploaded JAR files at a specific time (either immediate or future) with optional recurrence.

## Tech Stack

- **Frontend**: Next.js
- **Backend**: Java Spring Boot
- **Database**: H2 Database (PostgreSQL mode)
- **Storage**: MinIO (S3-compatible object store)
- **Messaging Queue**: Kafka

## Prerequisites

Before running the application, make sure you have the following installed:

1. Java 17 or higher
2. Maven
3. Node.js and npm/yarn/pnpm
4. MinIO Server
5. Kafka

## Setup Instructions

### 1. Set up MinIO

1. Download and install MinIO from https://min.io/download
2. Start MinIO server:
   ```
   minio server /path/to/data
   ```
3. Access the MinIO web interface at http://localhost:9000
4. Create a bucket named `jars`

### 2. Set up Kafka

1. Download and install Kafka from https://kafka.apache.org/downloads
2. Start Zookeeper:
   ```
   bin/zookeeper-server-start.sh config/zookeeper.properties
   ```
3. Start Kafka server:
   ```
   bin/kafka-server-start.sh config/server.properties
   ```
4. Create the required topics:
   ```
   bin/kafka-topics.sh --create --topic job-execution --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1
   bin/kafka-topics.sh --create --topic job-result --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1
   ```

### 3. Build and Upload Sample JAR Files

1. Run the build script to compile the sample JAR files:
   ```
   scripts/build-jars.bat
   ```
2. Upload the JAR files to MinIO:
   - Open the MinIO web interface at http://localhost:9000
   - Navigate to the `jars` bucket
   - Upload the JAR files from the `sample-jars/*/target` directories

### 4. Build and Run the Backend

1. Build the application:
   ```
   mvn clean package
   ```
2. Run the application:
   ```
   java -jar target/job-scheduler-0.0.1-SNAPSHOT.jar
   ```
3. The backend API will be available at http://localhost:8080/api
4. H2 Console will be available at http://localhost:8080/api/h2-console

### 5. Run the Frontend

1. Navigate to the frontend directory:
   ```
   cd fe
   ```
2. Install dependencies:
   ```
   npm install
   ```
   or
   ```
   yarn install
   ```
   or
   ```
   pnpm install
   ```
3. Run the frontend:
   ```
   npm run dev
   ```
   or
   ```
   yarn dev
   ```
   or
   ```
   pnpm dev
   ```
4. The frontend will be available at http://localhost:3000

## API Endpoints

### JAR Files

- `GET /api/jar-files` - Get all JAR files
- `GET /api/jar-files/{id}` - Get JAR file by ID

### Job Schedules

- `POST /api/job-schedules` - Create a new job schedule
- `GET /api/job-schedules` - Get all job schedules
- `GET /api/job-schedules/{id}` - Get job schedule by ID
- `POST /api/job-schedules/{id}/cancel` - Cancel a job

### Job Executions

- `GET /api/job-executions/job-schedule/{jobScheduleId}` - Get job executions by job schedule ID
- `GET /api/job-executions/{id}` - Get job execution by ID

## Sample JAR Files

The application includes several sample JAR files that can be used for testing:

1. **hello-world.jar** - A simple Hello World application
2. **date-printer.jar** - Prints the current date and time in various formats
3. **data-processor.jar** - Processes data and calculates statistics
4. **report-generator.jar** - Generates a system performance report
