package com.lemnisk.jobscheduler.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lemnisk.jobscheduler.dto.JarFileDTO;
import com.lemnisk.jobscheduler.model.JarFile;
import com.lemnisk.jobscheduler.repository.JarFileRepository;

@Service
public class JarFileService {

    private static final Logger log = LoggerFactory.getLogger(JarFileService.class);

    private final JarFileRepository jarFileRepository;
    private final MinioService minioService;

    public JarFileService(JarFileRepository jarFileRepository, MinioService minioService) {
        this.jarFileRepository = jarFileRepository;
        this.minioService = minioService;
    }

    /**
     * Initialize JAR files from MinIO
     * This method also removes JAR files from the database that no longer exist in MinIO
     */
    @Transactional
    public void initializeJarFiles() {
        try {
            log.info("Initializing JAR files from MinIO");

            // Get all JAR files from MinIO
            List<String> jarFiles = minioService.listJarFiles();
            log.info("Found {} JAR files in MinIO", jarFiles.size());

            // Get all JAR files from database
            List<JarFile> dbJarFiles = jarFileRepository.findAll();
            log.info("Found {} JAR files in database", dbJarFiles.size());

            // Remove JAR files from database that no longer exist in MinIO
            int removedCount = 0;
            for (JarFile dbJarFile : new ArrayList<>(dbJarFiles)) {
                try {
                    if (!jarFiles.contains(dbJarFile.getName())) {
                        log.info("Removing JAR file {} from database as it no longer exists in MinIO", dbJarFile.getName());
                        jarFileRepository.delete(dbJarFile);
                        dbJarFiles.remove(dbJarFile);
                        removedCount++;
                    }
                } catch (Exception e) {
                    log.error("Error removing JAR file {} from database: {}", dbJarFile.getName(), e.getMessage(), e);
                }
            }
            log.info("Removed {} JAR files from database that no longer exist in MinIO", removedCount);

            // Add new JAR files from MinIO to database
            int addedCount = 0;
            for (String jarName : jarFiles) {
                try {
                    // Check if JAR file already exists in database
                    if (!jarFileRepository.existsByName(jarName)) {
                        log.info("Adding JAR file {} to database", jarName);

                        // Get JAR file metadata
                        var metadata = minioService.getJarMetadata(jarName);
                        log.info("Retrieved metadata for JAR file {}: size={}", jarName, metadata.size());

                        // Create new JAR file record
                        JarFile jarFile = JarFile.builder()
                                .name(jarName)
                                .description("JAR file: " + jarName)
                                .path(jarName)
                                .size(metadata.size())
                                .uploadedAt(LocalDateTime.now())
                                .build();

                        jarFileRepository.save(jarFile);
                        log.info("Added JAR file to database: {}", jarName);
                        addedCount++;
                    } else {
                        log.info("JAR file {} already exists in database", jarName);
                    }
                } catch (Exception e) {
                    log.error("Error adding JAR file {} to database: {}", jarName, e.getMessage(), e);
                }
            }
            log.info("Added {} new JAR files to database", addedCount);

            log.info("JAR file initialization complete. Database now has {} JAR files", jarFileRepository.count());
        } catch (Exception e) {
            log.error("Error initializing JAR files: {}", e.getMessage(), e);
            throw new RuntimeException("Error initializing JAR files", e);
        }
    }

    /**
     * Get all JAR files that are actually stored in MinIO
     */
    public List<JarFileDTO> getAllJarFiles() {
        try {
            log.info("Starting getAllJarFiles method");

            // Get all JAR files from database
            List<JarFile> dbJarFiles = jarFileRepository.findAll();
            log.info("Found {} JAR files in database", dbJarFiles.size());

            // Log each JAR file in the database
            for (JarFile jarFile : dbJarFiles) {
                log.info("Database JAR file: id={}, name={}, size={}",
                         jarFile.getId(), jarFile.getName(), jarFile.getSize());
            }

            // Filter JAR files that are actually in MinIO
            List<JarFileDTO> jarFileDTOs = new ArrayList<>();
            int minioJarCount = 0;

            for (JarFile jarFile : dbJarFiles) {
                try {
                    log.info("Checking if JAR file {} exists in MinIO", jarFile.getName());
                    boolean exists = minioService.jarFileExists(jarFile.getName());
                    log.info("JAR file {} exists in MinIO: {}", jarFile.getName(), exists);

                    if (exists) {
                        jarFileDTOs.add(convertToDTO(jarFile));
                        minioJarCount++;
                        log.info("Added JAR file {} to result list", jarFile.getName());
                    } else {
                        log.warn("JAR file {} exists in database but not in MinIO", jarFile.getName());
                    }
                } catch (Exception e) {
                    log.error("Error checking if JAR file {} exists in MinIO: {}", jarFile.getName(), e.getMessage(), e);
                }
            }

            log.info("Found {} JAR files in MinIO out of {} in database", minioJarCount, dbJarFiles.size());
            log.info("Returning {} JAR files that exist in both database and MinIO", jarFileDTOs.size());
            return jarFileDTOs;
        } catch (Exception e) {
            log.error("Error getting JAR files from MinIO: {}", e.getMessage(), e);
            // In case of error, return empty list
            return new ArrayList<>();
        }
    }

    /**
     * Get JAR file by ID, only if it exists in MinIO
     */
    public JarFileDTO getJarFileById(UUID id) {
        try {
            Optional<JarFile> jarFileOptional = jarFileRepository.findById(id);

            if (jarFileOptional.isPresent()) {
                JarFile jarFile = jarFileOptional.get();

                // Check if the JAR file exists in MinIO
                if (minioService.jarFileExists(jarFile.getName())) {
                    return convertToDTO(jarFile);
                } else {
                    log.warn("JAR file {} (ID: {}) exists in database but not in MinIO", jarFile.getName(), id);
                    return null;
                }
            }

            return null;
        } catch (Exception e) {
            log.error("Error getting JAR file by ID {}: {}", id, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Convert JarFile to JarFileDTO
     */
    private JarFileDTO convertToDTO(JarFile jarFile) {
        return JarFileDTO.builder()
                .id(jarFile.getId())
                .name(jarFile.getName())
                .description(jarFile.getDescription())
                .size(jarFile.getSize())
                .uploadedAt(jarFile.getUploadedAt())
                .build();
    }
}
