package com.lemnisk.jobscheduler.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.lemnisk.jobscheduler.model.JarFile;

@Service
public class JarExecutorService {

    private static final Logger log = LoggerFactory.getLogger(JarExecutorService.class);

    private final MinioService minioService;

    public JarExecutorService(MinioService minioService) {
        this.minioService = minioService;
    }

    /**
     * Execute a JAR file and return the output
     */
    public ExecutionResult executeJar(JarFile jarFile, List<String> arguments) {
        log.info("Executing JAR file: {}", jarFile.getName());

        try {
            // Download JAR file from MinIO
            Path tempJarFile = downloadJarFile(jarFile.getName());

            // Prepare command
            List<String> command = new ArrayList<>();
            command.add("java");
            command.add("-jar");
            command.add(tempJarFile.toString());

            // Add arguments if provided
            if (arguments != null && !arguments.isEmpty()) {
                command.addAll(arguments);
            }

            // Start process
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true); // Merge stderr into stdout

            long startTime = System.currentTimeMillis();
            Process process = processBuilder.start();

            // Read output
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\\n");
                }
            }

            // Wait for process to complete (with timeout)
            boolean completed = process.waitFor(60, TimeUnit.SECONDS);
            long endTime = System.currentTimeMillis();

            // Clean up temp file
            Files.deleteIfExists(tempJarFile);

            if (!completed) {
                process.destroyForcibly();
                return new ExecutionResult(false, "Process timed out after 60 seconds",
                        output.toString(), (endTime - startTime) / 1000.0);
            }

            int exitCode = process.exitValue();
            boolean success = exitCode == 0;

            return new ExecutionResult(
                    success,
                    success ? "Process completed successfully" : "Process failed with exit code " + exitCode,
                    output.toString(),
                    (endTime - startTime) / 1000.0
            );

        } catch (Exception e) {
            log.error("Error executing JAR file: {}", e.getMessage(), e);
            return new ExecutionResult(false, "Error: " + e.getMessage(), e.toString(), 0);
        }
    }

    /**
     * Download JAR file from MinIO to a temporary file
     */
    private Path downloadJarFile(String jarName) throws IOException {
        log.info("Downloading JAR file from MinIO: {}", jarName);

        // Create temp file
        Path tempFile = Files.createTempFile("job-scheduler-", "-" + jarName);

        // Download from MinIO
        try (var inputStream = minioService.getJarFile(jarName)) {
            Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
        }

        // Make sure the file is executable
        File file = tempFile.toFile();
        file.setExecutable(true);

        return tempFile;
    }

    /**
     * Class to hold execution result
     */
    public static class ExecutionResult {
        private final boolean success;
        private final String message;
        private final String output;
        private final double executionTimeSeconds;

        public ExecutionResult(boolean success, String message, String output, double executionTimeSeconds) {
            this.success = success;
            this.message = message;
            this.output = output;
            this.executionTimeSeconds = executionTimeSeconds;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public String getOutput() {
            return output;
        }

        public double getExecutionTimeSeconds() {
            return executionTimeSeconds;
        }

        public String getExecutionTimeFormatted() {
            return String.format("%.2fs", executionTimeSeconds);
        }
    }
}
