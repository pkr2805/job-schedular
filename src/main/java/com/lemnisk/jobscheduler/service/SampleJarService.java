package com.lemnisk.jobscheduler.service;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lemnisk.jobscheduler.model.JarFile;
import com.lemnisk.jobscheduler.repository.JarFileRepository;

@Service
public class SampleJarService {

    private static final Logger log = LoggerFactory.getLogger(SampleJarService.class);
    private static final String SAMPLE_JARS_DIR = "jar_files-main";

    private final JarFileRepository jarFileRepository;

    public SampleJarService(JarFileRepository jarFileRepository) {
        this.jarFileRepository = jarFileRepository;
    }

    /**
     * Initialize JAR files from the jar_files-main directory
     */
    @Transactional
    public void initializeSampleJars() {
        try {
            log.info("Initializing JAR files from {}", SAMPLE_JARS_DIR);

            // Check if we already have JAR files
            if (jarFileRepository.count() > 0) {
                log.info("JAR files already exist, skipping JAR initialization");
                return;
            }

            // Get the jar_files-main directory
            File jarFilesDir = new File(SAMPLE_JARS_DIR);
            if (!jarFilesDir.exists() || !jarFilesDir.isDirectory()) {
                log.warn("JAR files directory not found: {}", SAMPLE_JARS_DIR);
                return;
            }

            // Get all JAR files in the directory
            File[] jarFiles = jarFilesDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".jar"));
            if (jarFiles == null || jarFiles.length == 0) {
                log.warn("No JAR files found in {}", SAMPLE_JARS_DIR);
                return;
            }

            List<JarFile> jarFileEntities = new ArrayList<>();

            // Create a JarFile entity for each JAR file
            for (File jarFile : jarFiles) {
                String jarName = jarFile.getName();
                log.info("Creating JAR file entity: {}", jarName);

                // Get the size of the JAR file
                long size = jarFile.length();

                // Create a new JarFile entity
                JarFile jarFileEntity = JarFile.builder()
                        .name(jarName)
                        .description("JAR file: " + jarName.replace(".jar", ""))
                        .path(jarFile.getPath())
                        .size(size > 0 ? size : 1024) // Use 1KB as default size if file is empty
                        .uploadedAt(LocalDateTime.now())
                        .build();

                jarFileEntities.add(jarFileEntity);
            }

            // Save all JAR files to the database
            jarFileRepository.saveAll(jarFileEntities);
            log.info("Initialized {} JAR files", jarFileEntities.size());

        } catch (Exception e) {
            log.error("Error initializing JAR files: {}", e.getMessage(), e);
        }
    }


}
