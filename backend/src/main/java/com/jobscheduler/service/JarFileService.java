package com.jobscheduler.service;

import com.jobscheduler.model.JarFile;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public interface JarFileService {
    
    List<JarFile> getAllJarFiles();
    
    JarFile getJarFileById(UUID id);
    
    JarFile uploadJarFile(MultipartFile file, String description) throws IOException;
    
    void deleteJarFile(UUID id) throws IOException;
    
    void validateJarFile(String path) throws IOException;
    
    void validateJarFile(MultipartFile file) throws IOException;
    
    void validateJarFileById(UUID id) throws IOException;
    
    Resource loadJarFileAsResource(UUID id) throws IOException;
    
    String getJarManifestContents(UUID id) throws IOException;
    
    JarFile updateJarFileDescription(UUID id, String description) throws IOException;
} 