package com.lemnisk.jobscheduler.service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.ListObjectsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.messages.Item;

@Service
public class MinioService {

    private static final Logger log = LoggerFactory.getLogger(MinioService.class);

    private final MinioClient minioClient;

    @Value("${minio.bucketName}")
    private String bucketName;

    public MinioService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    /**
     * Initialize MinIO bucket if it doesn't exist
     */
    public void initializeBucket() {
        try {
            log.info("Attempting to connect to MinIO bucket: {}", bucketName);

            boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build());

            if (!bucketExists) {
                log.info("Bucket '{}' does not exist, creating it", bucketName);
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build());
                log.info("Bucket '{}' created successfully", bucketName);
            } else {
                log.info("Bucket '{}' already exists", bucketName);
            }
        } catch (Exception e) {
            log.error("Error initializing MinIO bucket: {}", e.getMessage(), e);
            log.error("MinIO connection details: bucket={}", bucketName);
            // Don't throw exception, just log it and continue
            // This allows the application to start even if MinIO is not available
            // The application will try to reconnect when needed
        }
    }

    /**
     * List all JAR files in the bucket
     */
    public List<String> listJarFiles() {
        List<String> jarFiles = new ArrayList<>();
        try {
            log.info("Listing JAR files in bucket: {}", bucketName);
            log.info("MinIO endpoint: {}", minioClient.toString());

            // First check if the bucket exists
            log.debug("Checking if bucket {} exists", bucketName);
            boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build());
            log.info("Bucket {} exists: {}", bucketName, bucketExists);

            if (!bucketExists) {
                log.warn("Bucket {} does not exist, creating it", bucketName);
                try {
                    minioClient.makeBucket(MakeBucketArgs.builder()
                            .bucket(bucketName)
                            .build());
                    log.info("Bucket {} created successfully", bucketName);
                } catch (Exception e) {
                    log.error("Failed to create bucket {}: {}", bucketName, e.getMessage(), e);
                    return jarFiles; // Return empty list
                }
            }

            // List objects in the bucket, specifically in the "jars" folder
            log.info("Listing objects in bucket {} in the jars folder", bucketName);
            Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder()
                    .bucket(bucketName)
                    .prefix("jars/")
                    .recursive(true)
                    .build());

            int totalObjects = 0;
            for (Result<Item> result : results) {
                try {
                    totalObjects++;
                    Item item = result.get();
                    String objectName = item.objectName();
                    log.debug("Found object in MinIO: {}", objectName);

                    if (objectName.endsWith(".jar")) {
                        // Extract just the filename from the path
                        String jarName = objectName.substring(objectName.lastIndexOf('/') + 1);
                        jarFiles.add(jarName);
                        log.info("Found JAR file in MinIO: {} (path: {})", jarName, objectName);
                    } else {
                        log.debug("Skipping non-JAR file: {}", objectName);
                    }
                } catch (Exception e) {
                    log.error("Error processing MinIO object: {}", e.getMessage(), e);
                    // Continue with the next item
                }
            }
            log.info("Total objects in bucket {}: {}", bucketName, totalObjects);

            log.info("Found {} JAR files in MinIO bucket {}", jarFiles.size(), bucketName);
        } catch (Exception e) {
            log.error("Error listing JAR files: {}", e.getMessage(), e);
            // Don't throw exception, just log it and return empty list
            // This allows the application to continue even if MinIO is not available
        }
        return jarFiles;
    }

    /**
     * Get JAR file metadata
     */
    public StatObjectResponse getJarMetadata(String jarName) {
        try {
            String objectPath = "jars/" + jarName;
            log.info("Getting metadata for JAR file: {} (path: {}) in bucket: {}", jarName, objectPath, bucketName);
            StatObjectResponse response = minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectPath)
                    .build());
            log.info("Successfully retrieved metadata for JAR file: {}, size: {}", jarName, response.size());
            return response;
        } catch (Exception e) {
            log.error("Error getting JAR metadata for {}: {}", jarName, e.getMessage(), e);
            // Instead of creating a dummy response, throw a more specific exception
            throw new RuntimeException("Error getting JAR metadata for " + jarName, e);
        }
    }

    /**
     * Get JAR file as input stream
     */
    public InputStream getJarFile(String jarName) {
        try {
            String objectPath = "jars/" + jarName;
            log.info("Getting JAR file: {} (path: {}) from bucket: {}", jarName, objectPath, bucketName);
            InputStream inputStream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectPath)
                    .build());
            log.info("Successfully retrieved JAR file: {}", jarName);
            return inputStream;
        } catch (Exception e) {
            log.error("Error getting JAR file {}: {}", jarName, e.getMessage(), e);
            throw new RuntimeException("Error getting JAR file: " + jarName, e);
        }
    }

    /**
     * Check if a JAR file exists in MinIO
     */
    public boolean jarFileExists(String jarName) {
        try {
            String objectPath = "jars/" + jarName;
            log.info("Checking if JAR file {} (path: {}) exists in MinIO bucket {}", jarName, objectPath, bucketName);

            // First check if the bucket exists
            boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build());

            if (!bucketExists) {
                log.error("Bucket {} does not exist", bucketName);
                return false;
            }

            log.info("Bucket {} exists, checking for object {}", bucketName, objectPath);

            // Then check if the object exists
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectPath)
                    .build());

            log.info("JAR file {} exists in MinIO bucket {}", jarName, bucketName);
            return true;
        } catch (Exception e) {
            log.error("Error checking if JAR file {} exists in MinIO: {}", jarName, e.getMessage(), e);
            return false;
        }
    }
}
