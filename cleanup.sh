#!/bin/bash

# Create a backup directory
mkdir -p backup

# Backup important files before removal
echo "Creating backups of important files..."
cp -r ./target/job-scheduler-0.0.1-SNAPSHOT.jar ./backup/
cp -r ./src ./backup/
cp -r ./fe/app ./backup/
cp -r ./fe/components ./backup/
cp -r ./fe/lib ./backup/
cp -r ./fe/public ./backup/
cp -r ./fe/hooks ./backup/
cp -r ./jar_files-main/*.jar ./backup/
cp ./pom.xml ./backup/
cp ./fe/package.json ./backup/
cp ./src/main/resources/application.properties ./backup/

# Remove build and cache files
echo "Removing build and cache files..."
rm -rf ./.next
rm -rf ./fe/.next
rm -rf ./target/classes
rm -rf ./target/generated-sources
rm -rf ./target/maven-archiver
rm -rf ./target/maven-status
# Keep the main JAR file
# rm -rf ./target/*.original

# Remove temporary and log files
echo "Removing temporary and log files..."
rm -f ./bash.exe.stackdump
rm -f ./spring-boot-log.txt
rm -f ./data/jobscheduler.trace.db

# Remove IDE settings
echo "Removing IDE settings..."
rm -rf ./.vscode

# Remove duplicate SQL scripts (keep only the main ones)
echo "Cleaning up SQL scripts..."
mkdir -p ./sql_scripts
mv ./setup-postgres.sql ./sql_scripts/
mv ./create-tables.sql ./sql_scripts/
rm -f ./fix-permissions.sql
rm -f ./fix-postgres.sql
rm -f ./fix-postgres-permissions.sql
rm -f ./fix-user.sql

echo "Cleanup complete!"
echo "Backups of important files are stored in the 'backup' directory."
