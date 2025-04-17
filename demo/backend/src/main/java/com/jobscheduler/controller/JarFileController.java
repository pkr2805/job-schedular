package com.jobscheduler.controller;

import com.jobscheduler.model.JarFile;
import com.jobscheduler.service.JarStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/jar-files")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class JarFileController {

    private final JarStorageService jarStorageService;
    
    @GetMapping
    public ResponseEntity<List<JarFile>> getAllJarFiles() {
        try {
            List<String> jarFileNames = jarStorageService.listJarFiles();
            List<JarFile> jarFiles = new ArrayList<>();
            
            Map<String, String> descriptions = getJarDescriptions();
            
            for (String fileName : jarFileNames) {
                JarFile jarFile = new JarFile();
                jarFile.setId(UUID.randomUUID());
                jarFile.setName(fileName);
                jarFile.setDescription(descriptions.getOrDefault(fileName, "No description available"));
                jarFiles.add(jarFile);
            }
            
            return ResponseEntity.ok(jarFiles);
        } catch (Exception e) {
            log.error("Error retrieving JAR files", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    private Map<String, String> getJarDescriptions() {
        // Return empty map - all JARs will use the default description
        return new HashMap<>();
    }
} 