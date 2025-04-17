# Job Scheduler Demo

A modern job scheduling application with a Spring Boot backend and Next.js frontend.

## Features

- Create and manage scheduled jobs
- Support for immediate and recurring job execution
- Various frequency options (hourly, daily, weekly)
- Real-time notifications via Kafka and WebSockets
- Job status tracking and history
- Execution logs and errors
- Dynamic JAR file selection

## Setup Instructions

### Prerequisites
- Java 17 or higher
- Maven
- Node.js and npm
- Kafka (optional, will fallback to direct notifications)

### Backend Setup
1. Navigate to the backend directory:
   ```
   cd backend
   ```
2. Build the project:
   ```
   mvn clean package
   ```
3. Run the Spring Boot application:
   ```
   mvn spring-boot:run
   ```
   The backend will be available at http://localhost:8083/api

### Frontend Setup
1. Navigate to the frontend directory:
   ```
   cd frontend
   ```
2. Install dependencies:
   ```
   npm install
   ```
3. Run the development server:
   ```
   npm run dev
   ```
   The frontend will be available at http://localhost:3000 