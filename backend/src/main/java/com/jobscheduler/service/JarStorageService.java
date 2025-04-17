package com.jobscheduler.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

public interface JarStorageService {
    
    List<String> listJarFiles() throws Exception;
    
    void uploadJar(String fileName, InputStream inputStream, long size, String contentType) throws Exception;
    
    void uploadJar(MultipartFile file) throws Exception;
    
    InputStream getJar(String fileName) throws Exception;
    
    void deleteJar(String fileName) throws Exception;
    
    boolean jarExists(String fileName) throws Exception;
} 