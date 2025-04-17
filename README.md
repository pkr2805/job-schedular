# Job Scheduler

A modern job scheduling application with a Spring Boot backend and Next.js frontend.

## Demo

The complete demo application is available in the [demo](./demo) directory.

### What's Inside the Demo

- **[Backend](./demo/backend)**: Spring Boot application with REST APIs, job scheduling, and Kafka integration
- **[Frontend](./demo/frontend)**: Next.js application with modern UI components and real-time notifications

## Features

- Create and manage scheduled jobs
- Support for immediate and recurring job execution
- Various frequency options (hourly, daily, weekly)
- Real-time notifications via Kafka and WebSockets
- Job status tracking and history
- Execution logs and errors
- Dynamic JAR file selection

## Getting Started

See the [demo README](./demo/README.md) for detailed setup instructions.

## Technology Stack

### Backend
- Java 17
- Spring Boot 3.2.6
- Spring Data JPA
- H2 Database
- Kafka for messaging
- WebSockets for real-time updates

### Frontend
- Next.js 15.2
- React 19.1
- TypeScript
- Tailwind CSS
- Shadcn UI components

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
   cd frontend_new/frontend
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

## Usage

### Creating Jobs
1. Navigate to "Create Job" page
2. Select a JAR file
3. Choose execution type (immediate or scheduled)
4. For scheduled jobs, select date, time, and frequency
5. Click "Create Job"

### Managing Jobs
- View all jobs in the "Job History" page
- Check job status and execution details
- Cancel running or pending jobs
- View execution logs and Kafka messages

## Architecture

The application follows a layered architecture:
- **Controller Layer**: REST APIs for job management
- **Service Layer**: Business logic for job scheduling and execution
- **Repository Layer**: Data access using Spring Data JPA
- **Model Layer**: Entity classes representing the domain
- **Frontend**: React components and API services

Jobs are executed asynchronously using a thread pool, and status updates are sent via Kafka and WebSockets.
