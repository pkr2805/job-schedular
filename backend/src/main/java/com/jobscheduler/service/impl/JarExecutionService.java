package com.jobscheduler.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Slf4j
@RequiredArgsConstructor
public class JarExecutionService {

    @Value("${job.scheduler.job-directory}")
    private String jarDirectory;

    private final ExecutorService executorService = Executors.newFixedThreadPool(5);

    /**
     * Execute a JAR file and return the execution result asynchronously
     * 
     * @param jarFileName Name of the JAR file
     * @return CompletableFuture with execution result
     */
    public CompletableFuture<ExecutionResult> executeJar(String jarFileName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("Executing JAR file: {}", jarFileName);
                
                // Build the full path to the JAR file
                String jarPath = jarDirectory.replace("file:", "") + File.separator + jarFileName;
                
                // Validate the JAR file exists
                File jarFile = new File(jarPath);
                if (!jarFile.exists()) {
                    log.error("JAR file does not exist: {}", jarPath);
                    return new ExecutionResult(false, "JAR file not found: " + jarFileName, "");
                }
                
                // Create the process to execute the JAR
                ProcessBuilder processBuilder = new ProcessBuilder("java", "-jar", jarPath);
                processBuilder.redirectErrorStream(true);
                
                // Start the process
                Process process = processBuilder.start();
                
                // Capture the output
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                StringBuilder output = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
                
                // Wait for the process to complete
                int exitCode = process.waitFor();
                log.info("JAR execution completed with exit code: {}", exitCode);
                
                if (exitCode == 0) {
                    return new ExecutionResult(true, "", output.toString());
                } else {
                    return new ExecutionResult(false, "Execution failed with exit code: " + exitCode, output.toString());
                }
                
            } catch (IOException | InterruptedException e) {
                log.error("Error executing JAR file: {}", jarFileName, e);
                return new ExecutionResult(false, "Error executing JAR file: " + e.getMessage(), "");
            }
        }, executorService);
    }
    
    /**
     * List all available JAR files
     * 
     * @return List of JAR file names
     */
    public List<String> listAvailableJars() {
        List<String> jarFiles = new ArrayList<>();
        
        try {
            File directory = new File(jarDirectory.replace("file:", ""));
            if (directory.exists() && directory.isDirectory()) {
                File[] files = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".jar"));
                if (files != null) {
                    for (File file : files) {
                        jarFiles.add(file.getName());
                    }
                }
            } else {
                log.warn("JAR directory does not exist: {}", jarDirectory);
            }
        } catch (Exception e) {
            log.error("Error listing JAR files", e);
        }
        
        return jarFiles;
    }
    
    /**
     * Class to represent the result of a JAR execution
     */
    public static class ExecutionResult {
        private final boolean success;
        private final String error;
        private final String output;
        
        public ExecutionResult(boolean success, String error, String output) {
            this.success = success;
            this.error = error;
            this.output = output;
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getError() {
            return error;
        }
        
        public String getOutput() {
            return output;
        }
    }
} 