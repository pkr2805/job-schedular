package com.lemnisk.jobscheduler.config;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.lemnisk.jobscheduler.service.JarFileService;
import com.lemnisk.jobscheduler.service.MinioService;
import com.lemnisk.jobscheduler.service.SampleJarService;

@Component
public class ApplicationInitializer {

    private static final Logger log = LoggerFactory.getLogger(ApplicationInitializer.class);

    private final MinioService minioService;
    private final JarFileService jarFileService;
    private final SampleJarService sampleJarService;

    public ApplicationInitializer(MinioService minioService, JarFileService jarFileService, SampleJarService sampleJarService) {
        this.minioService = minioService;
        this.jarFileService = jarFileService;
        this.sampleJarService = sampleJarService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initialize() {
        log.info("Initializing application...");

        try {
            // Initialize MinIO bucket
            log.info("Initializing MinIO bucket...");
            minioService.initializeBucket();
            log.info("MinIO bucket initialized successfully");

            // List JAR files directly from MinIO for debugging
            log.info("Listing JAR files directly from MinIO for debugging...");
            List<String> minioJarFiles = minioService.listJarFiles();
            log.info("Found {} JAR files directly in MinIO", minioJarFiles.size());
            for (String jarFile : minioJarFiles) {
                log.info("MinIO JAR file: {}", jarFile);
            }

            // Initialize JAR files from MinIO
            log.info("Initializing JAR files from MinIO...");
            jarFileService.initializeJarFiles();
            log.info("JAR files initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize MinIO and JAR files: {}", e.getMessage(), e);
            log.info("Falling back to sample JAR files from local directory");

            try {
                // Initialize sample JAR files from local directory
                sampleJarService.initializeSampleJars();
                log.info("Sample JAR files initialized successfully");
            } catch (Exception ex) {
                log.error("Failed to initialize sample JAR files: {}", ex.getMessage(), ex);
            }
        }

        log.info("Application initialization completed");
    }
}
