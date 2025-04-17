package com.jobscheduler.service.impl;

import com.jobscheduler.service.JarStorageService;
import io.minio.*;
import io.minio.messages.Item;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
@Slf4j
public class MinioJarStorageServiceImpl implements JarStorageService {

    private final MinioClient minioClient;
    private boolean minioAvailable = false;

    @Value("${minio.bucket}")
    private String bucketName;
    
    @Value("${job.scheduler.job-directory}")
    private String jarDirectory;

    @PostConstruct
    public void init() {
        try {
            // Check if bucket exists
            boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!bucketExists) {
                // Create bucket if it doesn't exist
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                log.info("Created MinIO bucket: {}", bucketName);
            }
            minioAvailable = true;
        } catch (Exception e) {
            log.warn("MinIO is not available. Will use local jar directory instead. Error: {}", e.getMessage());
            minioAvailable = false;
        }
    }

    @Override
    public List<String> listJarFiles() {
        // Always use local jar directory
        log.info("Listing JAR files from local directory: {}", jarDirectory);
        List<String> jarFiles = new ArrayList<>();
        
        try {
            File directory = new File(jarDirectory.replace("file:", ""));
            if (directory.exists() && directory.isDirectory()) {
                File[] files = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".jar"));
                if (files != null) {
                    jarFiles = Arrays.stream(files)
                        .map(File::getName)
                        .collect(Collectors.toList());
                    log.info("Found {} JAR files in local directory", jarFiles.size());
                }
            } else {
                log.warn("JAR directory does not exist: {}", jarDirectory);
            }
        } catch (Exception e) {
            log.error("Error listing JAR files from local directory", e);
        }
        
        return jarFiles;
    }

    @Override
    public void uploadJar(String fileName, InputStream inputStream, long size, String contentType) throws Exception {
        // Save to local directory
        Path targetPath = Paths.get(jarDirectory.replace("file:", ""), fileName);
        try {
            Files.copy(inputStream, targetPath);
            log.info("Saved JAR file to local directory: {}", targetPath);
        } catch (IOException e) {
            log.error("Error saving JAR file to local directory", e);
            throw e;
        }
        
        // Try MinIO if available
        if (minioAvailable) {
            try {
                // Re-create input stream from the saved file
                InputStream fileStream = new FileInputStream(targetPath.toFile());
                minioClient.putObject(
                    PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fileName)
                        .stream(fileStream, size, -1)
                        .contentType(contentType)
                        .build()
                );
                fileStream.close();
                log.info("Uploaded JAR file: {} to MinIO bucket: {}", fileName, bucketName);
            } catch (Exception e) {
                log.error("Error uploading JAR file to MinIO, but file was saved locally", e);
            }
        }
    }

    @Override
    public void uploadJar(MultipartFile file) throws Exception {
        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            throw new IllegalArgumentException("Original filename is null");
        }
        
        uploadJar(fileName, file.getInputStream(), file.getSize(), file.getContentType());
    }

    @Override
    public InputStream getJar(String fileName) throws Exception {
        // Always get from local directory first
        Path filePath = Paths.get(jarDirectory.replace("file:", ""), fileName);
        if (Files.exists(filePath)) {
            log.info("Retrieved JAR file from local directory: {}", filePath);
            return new FileInputStream(filePath.toFile());
        }
        
        // Try MinIO if available and local file not found
        if (minioAvailable) {
            try {
                log.info("Retrieving JAR file from MinIO: {}", fileName);
                return minioClient.getObject(
                    GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fileName)
                        .build()
                );
            } catch (Exception e) {
                log.error("Error retrieving JAR file from MinIO", e);
                throw e;
            }
        }
        
        throw new FileNotFoundException("JAR file not found: " + fileName);
    }

    @Override
    public void deleteJar(String fileName) throws Exception {
        // Delete from local directory
        Path filePath = Paths.get(jarDirectory.replace("file:", ""), fileName);
        try {
            Files.deleteIfExists(filePath);
            log.info("Deleted JAR file from local directory: {}", filePath);
        } catch (IOException e) {
            log.error("Error deleting JAR file from local directory", e);
        }
        
        // Try MinIO if available
        if (minioAvailable) {
            try {
                minioClient.removeObject(
                    RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fileName)
                        .build()
                );
                log.info("Deleted JAR file: {} from MinIO bucket: {}", fileName, bucketName);
            } catch (Exception e) {
                log.error("Error deleting JAR file from MinIO", e);
            }
        }
    }

    @Override
    public boolean jarExists(String fileName) throws Exception {
        // Check local directory first
        Path filePath = Paths.get(jarDirectory.replace("file:", ""), fileName);
        if (Files.exists(filePath)) {
            return true;
        }
        
        // Try MinIO if available
        if (minioAvailable) {
            try {
                minioClient.statObject(
                    StatObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fileName)
                        .build()
                );
                return true;
            } catch (Exception e) {
                return false;
            }
        }
        
        return false;
    }
} 