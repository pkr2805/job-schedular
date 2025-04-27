#!/bin/bash

# Script to build sample JAR files and upload them to MinIO

# Configuration
MINIO_ENDPOINT="http://localhost:9000"
MINIO_ACCESS_KEY="minioadmin"
MINIO_SECRET_KEY="minioadmin"
MINIO_BUCKET="jars"

# Check if MinIO client (mc) is installed
if ! command -v mc &> /dev/null; then
    echo "MinIO client (mc) is not installed. Please install it first."
    echo "Visit https://min.io/docs/minio/linux/reference/minio-mc.html for installation instructions."
    exit 1
fi

# Configure MinIO client
echo "Configuring MinIO client..."
mc alias set local "$MINIO_ENDPOINT" "$MINIO_ACCESS_KEY" "$MINIO_SECRET_KEY"

# Create bucket if it doesn't exist
echo "Creating bucket if it doesn't exist..."
mc mb --ignore-existing "local/$MINIO_BUCKET"

# Build and upload each JAR file
build_and_upload() {
    local project_dir="$1"
    local jar_name="$2"
    local description="$3"
    
    echo "Building $jar_name..."
    cd "sample-jars/$project_dir" || exit 1
    mvn clean package
    
    if [ $? -eq 0 ]; then
        echo "Uploading $jar_name to MinIO..."
        mc cp "target/$project_dir-1.0-SNAPSHOT.jar" "local/$MINIO_BUCKET/$jar_name"
        
        # Set description as metadata
        mc tag set "local/$MINIO_BUCKET/$jar_name" "Description=$description"
        
        echo "$jar_name uploaded successfully."
    else
        echo "Failed to build $jar_name."
    fi
    
    cd ../..
}

# Build and upload all sample JAR files
build_and_upload "hello-world" "hello-world.jar" "Simple Hello World application"
build_and_upload "date-printer" "date-printer.jar" "Prints current date and time in various formats"
build_and_upload "data-processor" "data-processor.jar" "Processes data and calculates statistics"
build_and_upload "report-generator" "report-generator.jar" "Generates system performance reports"

echo "All JAR files have been built and uploaded to MinIO."
