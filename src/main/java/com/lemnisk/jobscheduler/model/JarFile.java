package com.lemnisk.jobscheduler.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class JarFile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;
    private String description;
    private String path;
    private long size;
    private LocalDateTime uploadedAt;

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private JarFile jarFile = new JarFile();

        public Builder name(String name) {
            jarFile.setName(name);
            return this;
        }

        public Builder description(String description) {
            jarFile.setDescription(description);
            return this;
        }

        public Builder path(String path) {
            jarFile.setPath(path);
            return this;
        }

        public Builder size(long size) {
            jarFile.setSize(size);
            return this;
        }

        public Builder uploadedAt(LocalDateTime uploadedAt) {
            jarFile.setUploadedAt(uploadedAt);
            return this;
        }

        public JarFile build() {
            return jarFile;
        }
    }
}
