# Job Scheduler Application

A full-stack job scheduling system that allows users to schedule execution of JAR files at specific times with support for immediate execution, future scheduling, and recurring jobs (hourly, daily, weekly).

![Job Scheduler Dashboard](https://via.placeholder.com/800x400?text=Job+Scheduler+Dashboard)

## 🚀 Features

- **JAR File Management**: Upload, view, and manage JAR files stored in MinIO
- **Flexible Job Scheduling**: Schedule jobs to run immediately or at a future date/time
- **Recurrence Options**: Configure jobs to run once or recur hourly, daily, or weekly
- **Job Monitoring**: Track job execution status, logs, and results
- **Job Control**: Cancel scheduled jobs or view execution history
- **Real-time Updates**: Receive real-time notifications of job status changes

## 🛠️ Tech Stack

- **Frontend**:
  - Next.js 15.x (React framework)
  - TypeScript
  - Tailwind CSS (with shadcn/ui components)
  - SWR for data fetching

- **Backend**:
  - Java 17
  - Spring Boot 3.x
  - Spring Data JPA
  - Spring Kafka

- **Database**:
  - PostgreSQL (production)
  - H2 Database (development/testing)

- **Storage**:
  - MinIO (S3-compatible object store)

- **Messaging**:
  - Apache Kafka for job execution messaging

## 📋 Prerequisites

Before running the application, ensure you have the following installed:

1. **Java 17 or higher**
2. **Maven 3.6+**
3. **Node.js 18+ and npm/yarn/pnpm**
4. **PostgreSQL 14+**
5. **MinIO Server**
6. **Apache Kafka**

## 🔧 Setup Instructions

### 1. Database Setup

1. Install PostgreSQL if not already installed
2. Create a database named `jobscheduler`:
   ```sql
   CREATE DATABASE jobscheduler;
   ```
3. Create a user with appropriate permissions:
   ```sql
   CREATE USER jobscheduler_user WITH PASSWORD 'jobscheduler_password';
   GRANT ALL PRIVILEGES ON DATABASE jobscheduler TO jobscheduler_user;
   ```
4. Run the setup script to create necessary tables:
   ```
   setup-postgres.bat
   ```

### 2. Set up MinIO

1. Download and install MinIO from https://min.io/download
2. Start MinIO server (Windows example):
   ```
   minio.exe server C:\minio\data
   ```
3. Access the MinIO web interface at http://localhost:9000 (default credentials: minioadmin/minioadmin)
4. Create a bucket named `data`

### 3. Set up Kafka

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

### 4. Prepare JAR Files

1. The project includes sample JAR files in the `jar_files-main` directory
2. Upload these JAR files to MinIO using the provided script:
   ```
   register-jars.ps1
   ```
   Or manually upload them to the `jars` folder in the `data` bucket

### 5. Build and Run the Backend

1. Build the application:
   ```
   mvn clean package
   ```
2. Run the application:
   ```
   mvn spring-boot:run
   ```
   Or using the JAR file:
   ```
   java -jar target/job-scheduler-0.0.1-SNAPSHOT.jar
   ```
3. The backend API will be available at http://localhost:8080/api

### 6. Run the Frontend

1. Navigate to the frontend directory:
   ```
   cd fe
   ```
2. Install dependencies:
   ```
   npm install
   ```
3. Run the frontend:
   ```
   npm run dev
   ```
4. The frontend will be available at http://localhost:3000

## 📁 Project Structure

```
job-scheduler/
├── fe/                         # Frontend (Next.js)
│   ├── app/                    # Next.js app directory
│   ├── components/             # React components
│   ├── lib/                    # Utility functions and API client
│   └── public/                 # Static assets
├── src/                        # Backend (Spring Boot)
│   └── main/
│       ├── java/com/lemnisk/jobscheduler/
│       │   ├── config/         # Configuration classes
│       │   ├── controller/     # REST controllers
│       │   ├── dto/            # Data Transfer Objects
│       │   ├── model/          # Entity models
│       │   ├── repository/     # Data repositories
│       │   └── service/        # Business logic
│       └── resources/          # Application properties
├── jar_files-main/             # Sample JAR files
├── scripts/                    # Utility scripts
└── sql_scripts/                # Database setup scripts
```

## 🔄 Workflow

1. **Upload JAR Files**: JAR files are stored in MinIO and registered in the database
2. **Create Job Schedule**: Select a JAR file and configure when it should run
3. **Job Execution**: The scheduler picks up due jobs and sends execution messages to Kafka
4. **Execution Processing**: The system executes the JAR file and captures the output
5. **Result Handling**: Execution results are stored and displayed in the UI

## 🌐 API Endpoints

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

## 📦 Available JAR Files

The application includes several sample JAR files:

1. **instant-job.jar** - A simple job that executes immediately
2. **subscribe-channel-1.jar** - Simulates subscribing to a channel
3. **subscribe-channel-2.jar** - Another channel subscription simulation
4. **ten-minute-reminder.jar** - Sets a reminder for 10 minutes
5. **wake-up-reminder.jar** - Sets a wake-up reminder

## 🧹 Maintenance

### Cleaning Up Unnecessary Files

A cleanup script is provided to remove unnecessary build artifacts and temporary files:

```
./cleanup.sh
```

### Database Maintenance

For database maintenance, use the following scripts:
- `setup-postgres.bat` - Initial database setup
- `fix-postgres.bat` - Fix common PostgreSQL permission issues

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🔍 Troubleshooting

For common issues and solutions, please refer to the [TROUBLESHOOTING.md](TROUBLESHOOTING.md) file.
