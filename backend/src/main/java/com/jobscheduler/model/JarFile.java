package com.jobscheduler.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.file.Path;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JarFile {
    private UUID id;
    private String name;
    private String description;
    private Path path;
    private String mainClass;
    private long size;
    private String type;
} 