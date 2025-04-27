package com.lemnisk.jobscheduler.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class JobScheduleDTO {
    private UUID id;
    private UUID jarFileId;
    private String jarName;
    private String executionType;
    private LocalDateTime scheduledTime;
    private String recurrenceType;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public JobScheduleDTO() {
    }

    public JobScheduleDTO(UUID id, UUID jarFileId, String jarName, String executionType, LocalDateTime scheduledTime,
            String recurrenceType, String status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.jarFileId = jarFileId;
        this.jarName = jarName;
        this.executionType = executionType;
        this.scheduledTime = scheduledTime;
        this.recurrenceType = recurrenceType;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getJarFileId() {
        return jarFileId;
    }

    public void setJarFileId(UUID jarFileId) {
        this.jarFileId = jarFileId;
    }

    public String getJarName() {
        return jarName;
    }

    public void setJarName(String jarName) {
        this.jarName = jarName;
    }

    public String getExecutionType() {
        return executionType;
    }

    public void setExecutionType(String executionType) {
        this.executionType = executionType;
    }

    public LocalDateTime getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(LocalDateTime scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public String getRecurrenceType() {
        return recurrenceType;
    }

    public void setRecurrenceType(String recurrenceType) {
        this.recurrenceType = recurrenceType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private JobScheduleDTO dto = new JobScheduleDTO();

        public Builder id(UUID id) {
            dto.setId(id);
            return this;
        }

        public Builder jarFileId(UUID jarFileId) {
            dto.setJarFileId(jarFileId);
            return this;
        }

        public Builder jarName(String jarName) {
            dto.setJarName(jarName);
            return this;
        }

        public Builder executionType(String executionType) {
            dto.setExecutionType(executionType);
            return this;
        }

        public Builder scheduledTime(LocalDateTime scheduledTime) {
            dto.setScheduledTime(scheduledTime);
            return this;
        }

        public Builder recurrenceType(String recurrenceType) {
            dto.setRecurrenceType(recurrenceType);
            return this;
        }

        public Builder status(String status) {
            dto.setStatus(status);
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            dto.setCreatedAt(createdAt);
            return this;
        }

        public Builder updatedAt(LocalDateTime updatedAt) {
            dto.setUpdatedAt(updatedAt);
            return this;
        }

        public JobScheduleDTO build() {
            return dto;
        }
    }
}
