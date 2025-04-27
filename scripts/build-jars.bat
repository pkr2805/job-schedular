@echo off
REM Script to build sample JAR files

echo Building sample JAR files...

REM Build hello-world.jar
echo Building hello-world.jar...
cd sample-jars\hello-world
call mvn clean package
if %ERRORLEVEL% NEQ 0 (
    echo Failed to build hello-world.jar
    exit /b %ERRORLEVEL%
)
cd ..\..

REM Build date-printer.jar
echo Building date-printer.jar...
cd sample-jars\date-printer
call mvn clean package
if %ERRORLEVEL% NEQ 0 (
    echo Failed to build date-printer.jar
    exit /b %ERRORLEVEL%
)
cd ..\..

REM Build data-processor.jar
echo Building data-processor.jar...
cd sample-jars\data-processor
call mvn clean package
if %ERRORLEVEL% NEQ 0 (
    echo Failed to build data-processor.jar
    exit /b %ERRORLEVEL%
)
cd ..\..

REM Build report-generator.jar
echo Building report-generator.jar...
cd sample-jars\report-generator
call mvn clean package
if %ERRORLEVEL% NEQ 0 (
    echo Failed to build report-generator.jar
    exit /b %ERRORLEVEL%
)
cd ..\..

echo All JAR files built successfully.
echo You can now upload them to MinIO manually or use the MinIO web interface.
