package com.lemnisk.jobscheduler.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class JarFileDTO {
    private UUID id;
    private String name;
    private String description;
    private long size;
    private LocalDateTime uploadedAt;

    public JarFileDTO() {
    }

    public JarFileDTO(UUID id, String name, String description, long size, LocalDateTime uploadedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.size = size;
        this.uploadedAt = uploadedAt;
    }

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

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private JarFileDTO dto = new JarFileDTO();

        public Builder id(UUID id) {
            dto.setId(id);
            return this;
        }

        public Builder name(String name) {
            dto.setName(name);
            return this;
        }

        public Builder description(String description) {
            dto.setDescription(description);
            return this;
        }

        public Builder size(long size) {
            dto.setSize(size);
            return this;
        }

        public Builder uploadedAt(LocalDateTime uploadedAt) {
            dto.setUploadedAt(uploadedAt);
            return this;
        }

        public JarFileDTO build() {
            return dto;
        }
    }
}
