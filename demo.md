# Job Scheduler - Demo

This is a demonstration of a job scheduler application with the following features:

## Architecture
- **Backend**: Spring Boot application with RESTful APIs
- **Frontend**: Next.js application with modern UI components

## Features
- Job scheduling with immediate or delayed execution
- Recurring jobs (hourly, daily, weekly, monthly)
- Real-time notifications via Kafka or direct WebSocket
- Job history tracking and management
- Dynamic JAR file loading and execution

## Running the Demo

### Backend
1. Navigate to the backend directory
2. Run `mvn spring-boot:run`
3. The server will start on port 8083

### Frontend  
1. Navigate to the frontend directory
2. Run `npm install`
3. Run `npm run dev`
4. Access the application at http://localhost:3000 