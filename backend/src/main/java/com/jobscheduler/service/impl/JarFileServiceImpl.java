package com.jobscheduler.service.impl;

import com.jobscheduler.model.JarFile;
import com.jobscheduler.service.JarFileService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class JarFileServiceImpl implements JarFileService {

    @Value("${job.scheduler.job-directory}")
    private String jobDirectory;

    private final Map<UUID, JarFile> jarFiles = new HashMap<>();
    private Path baseDir;

    @PostConstruct
    public void init() throws IOException {
        // Remove the 'file:' prefix if it exists
        String dirPath = jobDirectory.startsWith("file:") ? jobDirectory.substring(5) : jobDirectory;
        baseDir = Paths.get(dirPath);
        
        if (!Files.exists(baseDir)) {
            Files.createDirectories(baseDir);
        }
        
        // Load existing jar files
        loadJarFiles();
    }

    private void loadJarFiles() throws IOException {
        try (Stream<Path> paths = Files.walk(baseDir)) {
            List<Path> jarPaths = paths
                    .filter(path -> {
                        String fileName = path.getFileName().toString().toLowerCase();
                        return fileName.endsWith(".jar") || fileName.endsWith(".txt");
                    })
                    .collect(Collectors.toList());
            
            for (Path jarPath : jarPaths) {
                try {
                    // Skip description files, they will be processed with their jar files
                    if (jarPath.toString().toLowerCase().endsWith(".txt") &&
                        !jarPath.getFileName().toString().equalsIgnoreCase("immediate-execution.txt")) {
                        continue;
                    }
                    
                    JarFile jarFile = createJarFileFromPath(jarPath);
                    jarFiles.put(jarFile.getId(), jarFile);
                } catch (Exception e) {
                    log.error("Failed to load jar file: {}", jarPath, e);
                }
            }
        }
    }

    private JarFile createJarFileFromPath(Path jarPath) throws IOException {
        String fileName = jarPath.getFileName().toString();
        String name = fileName.substring(0, fileName.lastIndexOf('.'));
        long size = Files.size(jarPath);
        
        String mainClass = null;
        String description = null;
        String type = jarPath.toString().toLowerCase().endsWith(".jar") ? "jar" : "txt";
        
        // For jar files, try to extract main class from manifest
        if (type.equals("jar")) {
            try (java.util.jar.JarFile jar = new java.util.jar.JarFile(jarPath.toFile())) {
                Manifest manifest = jar.getManifest();
                if (manifest != null) {
                    mainClass = manifest.getMainAttributes().getValue("Main-Class");
                }
            }
        }
        
        // Look for a description file with the same name
        Path descPath = baseDir.resolve(name + ".txt");
        if (!jarPath.equals(descPath) && Files.exists(descPath)) {
            description = Files.readString(descPath);
        } else if (type.equals("txt")) {
            // If it's a text file, use first line or part as description
            try {
                String content = Files.readString(jarPath);
                description = content.length() > 100 
                    ? content.substring(0, 100) + "..." 
                    : content;
            } catch (Exception e) {
                description = name + " (text file)";
            }
        }
        
        return new JarFile(
                UUID.randomUUID(),
                name,
                description == null ? name + " job execution" : description,
                jarPath,
                mainClass,
                size,
                type
        );
    }

    @Override
    public List<JarFile> getAllJarFiles() {
        return new ArrayList<>(jarFiles.values());
    }

    @Override
    public JarFile getJarFileById(UUID id) {
        return jarFiles.get(id);
    }

    @Override
    public JarFile uploadJarFile(MultipartFile file, String description) throws IOException {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            originalFilename = "uploaded-file-" + UUID.randomUUID() + ".jar";
        }
        
        Path targetPath = baseDir.resolve(originalFilename);
        
        // If file exists, generate a unique name
        if (Files.exists(targetPath)) {
            String baseName = originalFilename.substring(0, originalFilename.lastIndexOf('.'));
            String extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
            originalFilename = baseName + "-" + UUID.randomUUID().toString().substring(0, 8) + extension;
            targetPath = baseDir.resolve(originalFilename);
        }
        
        // Copy the file
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        
        // Create description file if description is provided
        if (description != null && !description.isEmpty()) {
            String name = originalFilename.substring(0, originalFilename.lastIndexOf('.'));
            Path descPath = baseDir.resolve(name + ".txt");
            Files.writeString(descPath, description);
        }
        
        JarFile jarFile = createJarFileFromPath(targetPath);
        jarFiles.put(jarFile.getId(), jarFile);
        
        return jarFile;
    }

    @Override
    public void deleteJarFile(UUID id) throws IOException {
        JarFile jarFile = jarFiles.get(id);
        if (jarFile != null) {
            Files.deleteIfExists(jarFile.getPath());
            
            // Delete description file if it exists
            String name = jarFile.getName();
            Path descPath = baseDir.resolve(name + ".txt");
            Files.deleteIfExists(descPath);
            
            jarFiles.remove(id);
        }
    }

    @Override
    public void validateJarFile(String path) throws IOException {
        Path jarPath = Paths.get(path);
        if (!Files.exists(jarPath)) {
            throw new IOException("JAR file does not exist: " + path);
        }
        
        // If it's a text file, no need to validate as JAR
        if (path.toLowerCase().endsWith(".txt")) {
            return;
        }
        
        try (java.util.jar.JarFile jar = new java.util.jar.JarFile(jarPath.toFile())) {
            Manifest manifest = jar.getManifest();
            if (manifest == null) {
                throw new IOException("JAR file does not contain a manifest: " + path);
            }
            
            String mainClass = manifest.getMainAttributes().getValue("Main-Class");
            if (mainClass == null) {
                throw new IOException("JAR file does not specify a Main-Class: " + path);
            }
            
            // Check if the main class exists in the JAR
            String mainClassPath = mainClass.replace('.', '/') + ".class";
            JarEntry entry = jar.getJarEntry(mainClassPath);
            if (entry == null) {
                throw new IOException("Main class not found in JAR: " + mainClass);
            }
        }
    }
    
    @Override
    public void validateJarFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IOException("File is empty");
        }
        
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IOException("File name is null");
        }
        
        // If it's a text file, no need to validate as JAR
        if (originalFilename.toLowerCase().endsWith(".txt")) {
            return;
        }
        
        // Create a temporary file to validate
        File tempFile = File.createTempFile("temp-", "-" + originalFilename);
        try {
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(file.getBytes());
            }
            
            try (java.util.jar.JarFile jar = new java.util.jar.JarFile(tempFile)) {
                Manifest manifest = jar.getManifest();
                if (manifest == null) {
                    throw new IOException("JAR file does not contain a manifest");
                }
                
                String mainClass = manifest.getMainAttributes().getValue("Main-Class");
                if (mainClass == null) {
                    throw new IOException("JAR file does not specify a Main-Class");
                }
                
                // Check if the main class exists in the JAR
                String mainClassPath = mainClass.replace('.', '/') + ".class";
                JarEntry entry = jar.getJarEntry(mainClassPath);
                if (entry == null) {
                    throw new IOException("Main class not found in JAR: " + mainClass);
                }
            }
        } finally {
            tempFile.delete();
        }
    }
    
    @Override
    public void validateJarFileById(UUID id) throws IOException {
        JarFile jarFile = getJarFileById(id);
        if (jarFile == null) {
            throw new IOException("JAR file not found with id: " + id);
        }
        
        validateJarFile(jarFile.getPath().toString());
    }
    
    @Override
    public Resource loadJarFileAsResource(UUID id) throws IOException {
        JarFile jarFile = getJarFileById(id);
        if (jarFile == null) {
            throw new IOException("JAR file not found with id: " + id);
        }
        
        try {
            Path filePath = jarFile.getPath();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new IOException("File not found: " + jarFile.getName());
            }
        } catch (MalformedURLException e) {
            throw new IOException("Error reading file: " + jarFile.getName(), e);
        }
    }
    
    @Override
    public String getJarManifestContents(UUID id) throws IOException {
        JarFile jarFile = getJarFileById(id);
        if (jarFile == null) {
            throw new IOException("JAR file not found with id: " + id);
        }
        
        // If it's a text file, return its content
        if (jarFile.getType().equals("txt")) {
            return Files.readString(jarFile.getPath());
        }
        
        try (java.util.jar.JarFile jar = new java.util.jar.JarFile(jarFile.getPath().toFile())) {
            Manifest manifest = jar.getManifest();
            if (manifest == null) {
                return "No manifest found in JAR file.";
            }
            
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                manifest.write(baos);
                return baos.toString();
            }
        }
    }
    
    @Override
    public JarFile updateJarFileDescription(UUID id, String description) throws IOException {
        JarFile jarFile = getJarFileById(id);
        if (jarFile == null) {
            throw new IOException("JAR file not found with id: " + id);
        }
        
        // Update description file
        String name = jarFile.getName();
        Path descPath = baseDir.resolve(name + ".txt");
        Files.writeString(descPath, description);
        
        // Update in-memory object
        jarFile.setDescription(description);
        
        return jarFile;
    }
} 