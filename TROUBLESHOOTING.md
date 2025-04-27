# Troubleshooting Guide for Job Scheduler

This guide provides steps to troubleshoot common issues with the Job Scheduler application.

## 1. Check if PostgreSQL is Running

1. Open pgAdmin and try to connect to the PostgreSQL server
2. Or run the following command in a terminal:
   ```
   pg_isready -h localhost -p 5432
   ```
3. If PostgreSQL is not running, start it from the Services panel or using the PostgreSQL start command

## 2. Check PostgreSQL Database and User

1. Open pgAdmin and connect to the PostgreSQL server
2. Check if the `jobscheduler` database exists
3. Check if the `jobscheduler_user` user exists
4. If they don't exist, create them using the SQL script in `setup-postgres.sql`

## 3. Check if MinIO is Running

1. Open a web browser and navigate to http://localhost:9000
2. Try to log in with the credentials (minioadmin/minioadmin)
3. If MinIO is not running, start it using the MinIO executable

## 4. Check MinIO Bucket and Files

1. Log in to the MinIO web interface
2. Check if the `jars` bucket exists
3. If it doesn't exist, create it
4. Check if there are any JAR files in the bucket
5. If not, upload some JAR files to the bucket

## 5. Test MinIO Connection

Run the `test-minio.bat` script to test the connection to MinIO and list the JAR files in the bucket.

## 6. Reset the Database

If the application is not showing JAR files that are in MinIO, try resetting the database:

1. Drop the `jobscheduler` database in PostgreSQL
2. Create a new `jobscheduler` database
3. Grant privileges to the `jobscheduler_user`
4. Restart the application

## 7. Check Application Logs

1. Look for error messages in the application logs
2. Pay attention to MinIO connection errors
3. Pay attention to PostgreSQL connection errors

## 8. Manually Initialize JAR Files

If the application is not automatically detecting JAR files in MinIO, you can manually insert them into the database:

```sql
INSERT INTO jar_file (id, name, description, path, size, uploaded_at)
VALUES 
(gen_random_uuid(), 'example.jar', 'Example JAR file', 'example.jar', 1024, NOW());
```

## 9. Check API Endpoints

1. Test the API endpoint using Postman or curl:
   ```
   curl http://localhost:8080/api/jar-files
   ```
2. Check the response status code and body

## 10. Restart Everything

Sometimes a full restart of all components can resolve issues:

1. Stop the Job Scheduler application
2. Stop PostgreSQL
3. Stop MinIO
4. Start MinIO
5. Start PostgreSQL
6. Start the Job Scheduler application

## Common Error Messages and Solutions

### PostgreSQL Connection Errors

- **Connection refused**: PostgreSQL is not running or is not accessible
- **Authentication failed**: Incorrect username or password
- **Database does not exist**: The `jobscheduler` database has not been created

### MinIO Connection Errors

- **Connection refused**: MinIO is not running or is not accessible
- **Authentication failed**: Incorrect access key or secret key
- **Bucket not found**: The `jars` bucket has not been created

### Application Errors

- **Failed to initialize MinIO bucket**: MinIO is not running or is not accessible
- **Failed to initialize JAR files**: Error connecting to MinIO or PostgreSQL
- **No JAR files found**: No JAR files in MinIO or error listing JAR files
