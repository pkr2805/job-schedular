# PowerShell script to register JAR files from MinIO to the database

# Configuration
$API_URL = "http://localhost:8080/api"
$MINIO_BUCKET = "data"
$MINIO_PATH = "jars"

# Get list of JAR files from MinIO
Write-Host "Getting list of JAR files from MinIO..."
$jarFiles = @(
    "instant-job.jar",
    "subscribe-channel-1.jar",
    "subscribe-channel-2.jar",
    "ten-minute-reminder.jar",
    "wake-up-reminder.jar"
)

# Register each JAR file
foreach ($jarFile in $jarFiles) {
    $jarPath = "$MINIO_PATH/$jarFile"
    $jarSize = 0
    
    # Get file size based on the file name
    switch ($jarFile) {
        "instant-job.jar" { $jarSize = 802 }
        "subscribe-channel-1.jar" { $jarSize = 851 }
        "subscribe-channel-2.jar" { $jarSize = 852 }
        "ten-minute-reminder.jar" { $jarSize = 828 }
        "wake-up-reminder.jar" { $jarSize = 828 }
        default { $jarSize = 1024 }
    }
    
    Write-Host "Registering JAR file: $jarFile (size: $jarSize bytes)"
    
    # Create JSON payload
    $payload = @{
        name = $jarFile
        description = "JAR file: $($jarFile -replace '\.jar$', '')"
        path = $jarPath
        size = $jarSize
    } | ConvertTo-Json
    
    # Register JAR file
    try {
        $response = Invoke-RestMethod -Method POST -Uri "$API_URL/jar-files" -ContentType "application/json" -Body $payload
        Write-Host "Successfully registered JAR file: $jarFile with ID: $($response.id)" -ForegroundColor Green
    } catch {
        Write-Host "Error registering JAR file: $jarFile - $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host "Done registering JAR files"

# Now let's check if the JAR files were registered
Write-Host "Checking registered JAR files..."
try {
    $jarFiles = Invoke-RestMethod -Method GET -Uri "$API_URL/jar-files"
    Write-Host "Found $($jarFiles.Count) registered JAR files:" -ForegroundColor Green
    foreach ($jar in $jarFiles) {
        Write-Host "  - $($jar.name) (ID: $($jar.id))"
    }
} catch {
    Write-Host "Error getting JAR files: $($_.Exception.Message)" -ForegroundColor Red
}
