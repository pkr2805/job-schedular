# Job Scheduler

A professional job scheduling application built with Spring Boot and Next.js. This application allows users to schedule, manage, and monitor jobs with a modern, responsive UI.

## Features

- Create and schedule jobs with JAR file execution
- View job history and execution details
- Real-time status updates and notifications
- Dark/Light theme support
- Responsive design for all devices

## Tech Stack

### Backend
- Java Spring Boot
- Spring Data JPA
- RESTful API design
- Lombok for cleaner code
- Kafka for notifications (optional)

### Frontend
- Next.js 13
- React 18
- Tailwind CSS
- Shadcn UI components
- TypeScript

## Getting Started

### Prerequisites
- Java JDK 17+
- Node.js 18+
- npm or pnpm

### Installation

1. Clone the repository:
```bash
git clone https://github.com/pkr2805/job-schedular.git
cd job-scheduler
```

2. Start the backend:
```bash
cd backend
mvn spring-boot:run
```

3. Start the frontend:
```bash
cd frontend
npm install
npm run dev
```

4. Open your browser and navigate to `http://localhost:3000`

## Structure

- `/backend`: Spring Boot application with controllers, services, and models
- `/frontend`: Next.js application with pages, components, and utilities

## License

This project is open-source and available under the MIT License.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.
