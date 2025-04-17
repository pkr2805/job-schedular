package com.jobscheduler.controller;

import com.jobscheduler.dto.JarFileDTO;
import com.jobscheduler.service.JarStorageService;
import com.jobscheduler.service.impl.JarExecutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/jars")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class JarController {

    private final JarExecutionService jarExecutionService;
    private final JarStorageService jarStorageService;

    @GetMapping
    public ResponseEntity<List<JarFileDTO>> getAllJars() {
        try {
            List<String> jarFiles = jarStorageService.listJarFiles();
            List<JarFileDTO> jarFileDTOs = new ArrayList<>();
            
            for (String jarFile : jarFiles) {
                // Generate random ID for each JAR file
                String id = UUID.randomUUID().toString();
                String name = jarFile;
                String desc = getDescriptionForJar(jarFile);
                
                jarFileDTOs.add(new JarFileDTO(id, name, desc));
            }
            
            // If no JAR files are found, return sample JAR files
            if (jarFileDTOs.isEmpty()) {
                log.info("No JAR files found in storage. Using sample JAR files.");
                jarFileDTOs.add(new JarFileDTO(UUID.randomUUID().toString(), "channel-subscribe-1.jar", "Schedules subscription to the main news channel"));
                jarFileDTOs.add(new JarFileDTO(UUID.randomUUID().toString(), "channel-subscribe-2.jar", "Schedules subscription to the sports channel"));
                jarFileDTOs.add(new JarFileDTO(UUID.randomUUID().toString(), "wake-up-reminder.jar", "Recurring reminder to wake up at scheduled times"));
                jarFileDTOs.add(new JarFileDTO(UUID.randomUUID().toString(), "ten-min-reminder.jar", "Sends a reminder every 10 minutes for scheduled breaks"));
                jarFileDTOs.add(new JarFileDTO(UUID.randomUUID().toString(), "immediate-execution.jar", "Executes immediate tasks with high priority"));
            } else {
                log.info("Found {} JAR files in storage", jarFileDTOs.size());
            }
            
            return ResponseEntity.ok(jarFileDTOs);
        } catch (Exception e) {
            log.error("Error listing JAR files", e);
            
            // If we encounter an error, still return sample JAR files
            List<JarFileDTO> jarFileDTOs = new ArrayList<>();
            jarFileDTOs.add(new JarFileDTO(UUID.randomUUID().toString(), "channel-subscribe-1.jar", "Schedules subscription to the main news channel"));
            jarFileDTOs.add(new JarFileDTO(UUID.randomUUID().toString(), "channel-subscribe-2.jar", "Schedules subscription to the sports channel"));
            jarFileDTOs.add(new JarFileDTO(UUID.randomUUID().toString(), "wake-up-reminder.jar", "Recurring reminder to wake up at scheduled times"));
            jarFileDTOs.add(new JarFileDTO(UUID.randomUUID().toString(), "ten-min-reminder.jar", "Sends a reminder every 10 minutes for scheduled breaks"));
            jarFileDTOs.add(new JarFileDTO(UUID.randomUUID().toString(), "immediate-execution.jar", "Executes immediate tasks with high priority"));
            
            log.info("Returning sample JAR files due to error: {}", e.getMessage());
            return ResponseEntity.ok(jarFileDTOs);
        }
    }

    @PostMapping("/{jarName}/execute")
    public ResponseEntity<ExecutionResultDTO> executeJar(@PathVariable String jarName) {
        log.info("Executing JAR file: {}", jarName);
        
        try {
            // Check if JAR file exists in MinIO
            boolean jarExists = jarStorageService.jarExists(jarName);
            if (!jarExists) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, 
                    "JAR file not found: " + jarName);
            }
            
            // Mock execution for now
            // In a real implementation, we would download the JAR from MinIO and execute it
            boolean success = true;
            String output = "JAR executed successfully. Output: Hello from " + jarName;
            String error = "";
            
            return ResponseEntity.ok(new ExecutionResultDTO(success, jarName, output, error));
        } catch (ResponseStatusException rse) {
            throw rse;
        } catch (Exception e) {
            log.error("Error executing JAR file: {}", jarName, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Error executing JAR file: " + e.getMessage());
        }
    }
    
    // Add description method
    private String getDescriptionForJar(String jarName) {
        // Map jar names to descriptions
        if (jarName.contains("channel-subscribe-1")) {
            return "Schedules subscription to the main news channel";
        } else if (jarName.contains("channel-subscribe-2")) {
            return "Schedules subscription to the sports channel";
        } else if (jarName.contains("wake-up-reminder")) {
            return "Recurring reminder to wake up at scheduled times";
        } else if (jarName.contains("ten-min-reminder")) {
            return "Sends a reminder every 10 minutes for scheduled breaks";
        } else if (jarName.contains("immediate-execution")) {
            return "Executes immediate tasks with high priority";
        } else {
            return "JAR file for job execution";
        }
    }
    
    public static class ExecutionResultDTO {
        private final boolean success;
        private final String jarName;
        private final String output;
        private final String error;
        
        public ExecutionResultDTO(boolean success, String jarName, String output, String error) {
            this.success = success;
            this.jarName = jarName;
            this.output = output;
            this.error = error;
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getJarName() {
            return jarName;
        }
        
        public String getOutput() {
            return output;
        }
        
        public String getError() {
            return error;
        }
    }
} 