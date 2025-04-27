package com.lemnisk.jobscheduler.controller;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lemnisk.jobscheduler.dto.JarFileDTO;
import com.lemnisk.jobscheduler.service.JarFileService;

@RestController
@RequestMapping("/jar-files")
@CrossOrigin(origins = "${cors.allowed-origins}", allowedHeaders = "*")
public class JarFileController {

    private static final Logger log = LoggerFactory.getLogger(JarFileController.class);

    private final JarFileService jarFileService;

    public JarFileController(JarFileService jarFileService) {
        this.jarFileService = jarFileService;
    }

    /**
     * Get all JAR files
     */
    @GetMapping
    public ResponseEntity<List<JarFileDTO>> getAllJarFiles() {
        try {
            List<JarFileDTO> jarFiles = jarFileService.getAllJarFiles();
            return ResponseEntity.ok(jarFiles);
        } catch (Exception e) {
            log.error("Error getting all JAR files: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get JAR file by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<JarFileDTO> getJarFileById(@PathVariable UUID id) {
        try {
            log.info("Getting JAR file by ID: {}", id);
            JarFileDTO jarFileDTO = jarFileService.getJarFileById(id);

            if (jarFileDTO == null) {
                log.warn("JAR file with ID {} not found", id);
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(jarFileDTO);
        } catch (Exception e) {
            log.error("Error getting JAR file by ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
