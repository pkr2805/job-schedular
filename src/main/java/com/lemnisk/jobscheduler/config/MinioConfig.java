package com.lemnisk.jobscheduler.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.minio.MinioClient;

@Configuration
public class MinioConfig {

    private static final Logger log = LoggerFactory.getLogger(MinioConfig.class);

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.accessKey}")
    private String accessKey;

    @Value("${minio.secretKey}")
    private String secretKey;

    @Bean
    public MinioClient minioClient() {
        try {
            log.info("Initializing MinIO client with endpoint: {}, accessKey: {}", endpoint, accessKey);

            // Validate configuration
            if (endpoint == null || endpoint.isEmpty()) {
                log.error("MinIO endpoint is not configured");
                throw new IllegalArgumentException("MinIO endpoint is not configured");
            }

            if (accessKey == null || accessKey.isEmpty() || secretKey == null || secretKey.isEmpty()) {
                log.error("MinIO credentials are not configured");
                throw new IllegalArgumentException("MinIO credentials are not configured");
            }

            // Build the client
            MinioClient client = MinioClient.builder()
                    .endpoint(endpoint)
                    .credentials(accessKey, secretKey)
                    .build();

            // Test the connection
            log.info("Testing MinIO connection...");
            client.listBuckets();
            log.info("MinIO connection successful");

            return client;
        } catch (Exception e) {
            log.error("Failed to initialize MinIO client: {}", e.getMessage(), e);
            log.error("MinIO configuration error. Check endpoint and credentials.");

            // Return a client with the configured values anyway
            // This allows the application to start even if MinIO is not available
            // The application will handle exceptions when trying to use MinIO
            return MinioClient.builder()
                    .endpoint(endpoint)
                    .credentials(accessKey, secretKey)
                    .build();
        }
    }
}
