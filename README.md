# Job Scheduler Application

A full-stack job scheduling system that allows users to schedule execution of JAR files at specific times with support for immediate execution, future scheduling, and recurring jobs (hourly, daily, weekly).

## 📸 Screenshots

### Create Job Page
![Create Job Empty](![WhatsApp Image 2025-04-28 at 04 15 28_e0c96a56](https://github.com/user-attachments/assets/6fab34a8-496a-4576-9d72-a0e3db27b614)
)
*Create Job page with no JAR file selected*

![Create Job Selected](/fe/public/images/create-job-selected.png)
*Create Job page with a JAR file selected*

![Create Job Dropdown](/fe/public/images/create-job-dropdown.png)
*Create Job page showing available JAR files*

### Job History Page
![Job History](/fe/public/images/job-history.png)
*Job History page showing executed jobs with their status*

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
4. Run the SQL scripts to create necessary tables (you can use pgAdmin or psql)

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
2. Manually upload these JAR files to the `jars` folder in the `data` bucket using the MinIO web interface or curl:
   ```
   curl -X PUT "http://localhost:9000/jars/jar-file-name.jar" --upload-file "path/to/jar-file.jar" -u minioadmin:minioadmin
   ```

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
└── jar_files-main/             # Sample JAR files
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

To clean up unnecessary build artifacts and temporary files, you can:

1. Delete the `target` directory in the backend project
2. Delete the `.next` and `node_modules` directories in the frontend project

### Database Maintenance

For database maintenance, use pgAdmin or psql to manage your PostgreSQL database.

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 📘 How to Use the Application

### Creating a New Job

1. Navigate to the "Create Job" page from the navigation menu
2. Click on the JAR file dropdown and select one of the available JAR files
3. Choose the execution type:
   - **Run immediately**: The job will execute as soon as you submit it
   - **Schedule for later**: You can specify a date and time for the job to run
4. For scheduled jobs, you can also set recurrence options:
   - **One-time**: The job will run only once at the scheduled time
   - **Hourly**: The job will run every hour starting from the scheduled time
   - **Daily**: The job will run every day at the scheduled time
   - **Weekly**: The job will run every week on the same day and time
5. Click the "Create Job" button to submit the job

### Viewing Job History

1. Navigate to the "Job History" page from the navigation menu
2. View all jobs with their execution details:
   - JAR Name: The name of the JAR file that was executed
   - Execution Time: When the job was executed
   - Type: Whether the job was one-time or recurring
   - Status: Current status of the job (Completed, Pending, Failed)
   - Kafka Message: Success or failure message from the job execution
3. Use the search bar to filter jobs by JAR name or job ID
4. Use the status filter to view jobs with specific statuses
5. For pending jobs, you can cancel them using the "Cancel" button
6. View detailed logs by clicking the "Logs" button
