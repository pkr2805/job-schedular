# Job Scheduler

[![License](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.1.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Next.js](https://img.shields.io/badge/Next.js-14.x-black.svg)](https://nextjs.org/)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![React](https://img.shields.io/badge/React-18.x-blue.svg)](https://reactjs.org/)
[![Tailwind CSS](https://img.shields.io/badge/Tailwind%20CSS-3.x-38B2AC.svg)](https://tailwindcss.com/)
[![GitHub last commit](https://img.shields.io/github/last-commit/pkr2805/job-schedular)](https://github.com/pkr2805/job-schedular/commits/main)

A modern web application for scheduling and managing executable JAR files with an intuitive user interface.

## 📋 Features

- **Job Scheduling**: Schedule executable JAR files to run at specific times.
- **Job History**: View history of scheduled and executed jobs.
- **Real-time Updates**: Get live updates on job status and execution.
- **Dark/Light Theme**: Switch between dark and light modes for comfortable viewing.
- **Responsive Design**: Works seamlessly on desktop and mobile devices.

## 🛠️ Tech Stack

### Backend
- **Java Spring Boot**: For robust server-side processing
- **Spring Data JPA**: For data persistence and ORM
- **H2 Database**: For local development
- **Lombok**: For reducing boilerplate code
- **RESTful API**: For communication with the frontend

### Frontend
- **Next.js**: For server-side rendering and optimal performance
- **React**: For building interactive user interfaces
- **Tailwind CSS**: For styling and responsive design
- **Shadcn UI**: For pre-built UI components
- **Axios**: For HTTP requests to the backend

## 🏗️ Project Structure

```
job-scheduler/
├── backend/                # Spring Boot application
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   └── resources/
│   │   └── test/
│   └── pom.xml
│
├── frontend/               # Next.js application
│   ├── app/
│   ├── components/
│   ├── lib/
│   ├── public/
│   └── package.json
│
└── .gitignore              # Git ignore file
```

## 🚀 Getting Started

### Prerequisites
- Java 17+
- Node.js 18+
- Maven 3.8+

### Setup and Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/pkr2805/job-schedular.git
   cd job-scheduler
   ```

2. **Start the backend**
   ```bash
   cd backend
   mvn spring-boot:run
   ```

3. **Start the frontend**
   ```bash
   cd frontend
   npm install
   npm run dev
   ```

4. Access the application at `http://localhost:3000`

## 📝 Usage

1. **Login or Register**: Create an account to manage your jobs
2. **Upload JAR Files**: Upload the executable JAR files you want to schedule
3. **Create a Job**: Set up a new job with timing and execution parameters
4. **Monitor Status**: View the status and logs of your running jobs
5. **Review History**: Check the execution history of your scheduled jobs

## 🔄 API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET    | /api/jobs | Get all jobs |
| GET    | /api/jobs/{id} | Get job by ID |
| POST   | /api/jobs | Create a new job |
| PUT    | /api/jobs/{id} | Update a job |
| DELETE | /api/jobs/{id} | Delete a job |
| GET    | /api/jar-files | Get all JAR files |
| POST   | /api/jar-files | Upload a new JAR file |

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 👥 Creators

- **pkr2805** - [GitHub Profile](https://github.com/pkr2805)
