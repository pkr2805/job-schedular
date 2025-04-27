package com.lemnisk.jobscheduler.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public class JobScheduleRequest {

    @NotNull(message = "JAR file ID is required")
    private UUID jarFileId;

    @NotNull(message = "Execution type is required")
    private String executionType; // "immediate" or "scheduled"

    private LocalDateTime scheduledTime; // Required if executionType is "scheduled"

    private String recurrenceType; // "one-time", "hourly", "daily", "weekly"

    public JobScheduleRequest() {
    }

    public JobScheduleRequest(UUID jarFileId, String executionType, LocalDateTime scheduledTime, String recurrenceType) {
        this.jarFileId = jarFileId;
        this.executionType = executionType;
        this.scheduledTime = scheduledTime;
        this.recurrenceType = recurrenceType;
    }

    public UUID getJarFileId() {
        return jarFileId;
    }

    public void setJarFileId(UUID jarFileId) {
        this.jarFileId = jarFileId;
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

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private JobScheduleRequest request = new JobScheduleRequest();

        public Builder jarFileId(UUID jarFileId) {
            request.setJarFileId(jarFileId);
            return this;
        }

        public Builder executionType(String executionType) {
            request.setExecutionType(executionType);
            return this;
        }

        public Builder scheduledTime(LocalDateTime scheduledTime) {
            request.setScheduledTime(scheduledTime);
            return this;
        }

        public Builder recurrenceType(String recurrenceType) {
            request.setRecurrenceType(recurrenceType);
            return this;
        }

        public JobScheduleRequest build() {
            return request;
        }
    }
}
