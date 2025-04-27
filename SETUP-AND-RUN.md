# Job Scheduler Setup and Run Guide

This guide will walk you through setting up and running the Job Scheduler application with PostgreSQL.

## Prerequisites

1. Java 17 or higher installed
2. PostgreSQL installed and running
3. Maven installed (optional, if you want to build from source)

## Step 1: Set Up PostgreSQL

Follow the instructions in the `POSTGRES-SETUP-GUIDE.md` file to set up PostgreSQL for the application.

Quick summary:
1. Create a database named `jobscheduler`
2. Create a user named `jobscheduler_user` with password `jobscheduler_password`
3. Grant necessary privileges to the user

## Step 2: Configure the Application

The application is already configured to use PostgreSQL with the following settings:
- Database URL: `jdbc:postgresql://localhost:5432/jobscheduler`
- Username: `jobscheduler_user`
- Password: `jobscheduler_password`

If you need to change these settings, edit the `src/main/resources/application.properties` file.

## Step 3: Run the Application

### Option 1: Run the JAR file

```
java -jar target/job-scheduler-0.0.1-SNAPSHOT.jar
```

### Option 2: Run with Maven

```
mvn spring-boot:run
```

## Step 4: Verify the Application is Running

1. Open a web browser and navigate to `http://localhost:8080/api/jar-files`
2. You should see a JSON response (empty array if no JAR files have been uploaded yet)

## Step 5: Upload JAR Files to MinIO

1. Make sure MinIO is running
2. Use the MinIO web interface or the provided scripts to upload JAR files
3. The application will automatically detect and register the JAR files

## Step 6: Use the Frontend

1. Make sure the frontend is running (typically on `http://localhost:3000`)
2. Navigate to the frontend in your web browser
3. You should now be able to see and schedule jobs using the JAR files uploaded to MinIO

## Troubleshooting

### Database Connection Issues

If the application fails to connect to PostgreSQL:
1. Make sure PostgreSQL is running
2. Verify the connection details in `application.properties`
3. Check that the database and user exist with the correct privileges

### MinIO Connection Issues

If the application fails to connect to MinIO:
1. Make sure MinIO is running
2. Verify the MinIO connection details in `application.properties`
3. Check that the MinIO bucket exists

### Application Startup Issues

If the application fails to start:
1. Check the application logs for error messages
2. Make sure all required services (PostgreSQL, MinIO, Kafka) are running
3. Verify that the required ports are available (8080 for the API, etc.)

## Maintenance

### Changing Database Schema

The application is configured to create the database schema on first run (`spring.jpa.hibernate.ddl-auto=create`). For subsequent runs, you should change this to `update` to preserve your data:

```properties
spring.jpa.hibernate.ddl-auto=update
```

### Backing Up the Database

To back up the PostgreSQL database:
1. Use pgAdmin's backup feature
2. Or use the `pg_dump` command-line tool:
   ```
   pg_dump -U postgres -F c -b -v -f jobscheduler_backup.dump jobscheduler
   ```

### Restoring the Database

To restore the PostgreSQL database from a backup:
1. Use pgAdmin's restore feature
2. Or use the `pg_restore` command-line tool:
   ```
   pg_restore -U postgres -d jobscheduler -v jobscheduler_backup.dump
   ```
