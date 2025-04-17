# Job Scheduler Application

A full-stack job scheduler application that allows users to schedule jobs, run binaries, and set up asynchronous messaging using Kafka.

## Project Structure

```
job-scheduler/
├── frontend/           # Next.js frontend application
└── backend/            # Spring Boot backend application (to be created)
    └── binaries/       # Sample job binaries
```

## Features

- Schedule jobs to run at specific times with timezone support
- Run jobs immediately
- Schedule recurring jobs (hourly, daily, weekly, monthly)
- Schedule delayed Kafka messages
- Upload and manage binary files (JAR, NPM packages)
- Track job execution status and history

## Backend Tech Stack

- Java 17+
- Spring Boot 3.x (Web, JPA, Kafka, Scheduling)
- H2 Database for job persistence
- Local file system for binary storage
- Kafka for messaging
- Maven for build management

## Frontend Tech Stack

- Next.js 14 with App Router
- React 18+
- Tailwind CSS
- Shadcn UI components
- TypeScript

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven
- Node.js 18 or higher
- Kafka (optional, but required for messaging features)

### Running the Backend

1. Navigate to the backend directory:

```bash
cd backend
```

2. Build and run the application:

```bash
mvn spring-boot:run
```

The backend server will start on http://localhost:8080 with the following configurations:
- H2 Database (file-based at ./data/jobscheduler)
- Local file system for binary storage (./binaries)
- In-memory Kafka if no external Kafka is detected

3. Access the H2 console at http://localhost:8080/h2-console
   - JDBC URL: jdbc:h2:file:./data/jobscheduler
   - Username: sa
   - Password: password

### Running the Frontend

1. Navigate to the frontend directory:

```bash
cd frontend
```

2. Install dependencies:

```bash
npm install
```

3. Start the development server:

```bash
npm run dev
```

The frontend application will start on http://localhost:3000.

## Sample Jobs

The application should include sample job binaries in the `backend/binaries` directory:

1. **Channel Subscription Jobs** (Scheduled Job Examples):
   - Simulates subscribing to Channel 1 at a specific time
   - Simulates subscribing to Channel 2 at a specific time

2. **Recurring Jobs**:
   - Daily wake-up reminder (recurring job example)
   - 10-minute break reminder (recurring job example)

3. **Immediate Execution**:
   - Sample text file for testing immediate execution

## API Endpoints

- `POST /api/jobs`: Create a new job
- `GET /api/jobs`: Get all jobs
- `GET /api/jobs/{id}`: Get job by ID
- `POST /api/jobs/{id}/run`: Run a job immediately
- `POST /api/jobs/{id}/cancel`: Cancel a job
- `POST /api/jobs/upload-binary`: Upload a binary file
- `GET /api/jobs/binaries`: List all available binaries

## License

MIT 