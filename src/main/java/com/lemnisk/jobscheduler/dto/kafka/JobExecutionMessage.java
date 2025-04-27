package com.lemnisk.jobscheduler.dto.kafka;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public class JobExecutionMessage {
    private UUID jobId;
    private String jarName;
    private String executionType;
    private LocalDateTime scheduledTime;
    private String recurrenceType;
    private Map<String, Object> metadata;

    public JobExecutionMessage() {
    }

    public JobExecutionMessage(UUID jobId, String jarName, String executionType, LocalDateTime scheduledTime,
            String recurrenceType, Map<String, Object> metadata) {
        this.jobId = jobId;
        this.jarName = jarName;
        this.executionType = executionType;
        this.scheduledTime = scheduledTime;
        this.recurrenceType = recurrenceType;
        this.metadata = metadata;
    }

    public UUID getJobId() {
        return jobId;
    }

    public void setJobId(UUID jobId) {
        this.jobId = jobId;
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

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private JobExecutionMessage message = new JobExecutionMessage();

        public Builder jobId(UUID jobId) {
            message.setJobId(jobId);
            return this;
        }

        public Builder jarName(String jarName) {
            message.setJarName(jarName);
            return this;
        }

        public Builder executionType(String executionType) {
            message.setExecutionType(executionType);
            return this;
        }

        public Builder scheduledTime(LocalDateTime scheduledTime) {
            message.setScheduledTime(scheduledTime);
            return this;
        }

        public Builder recurrenceType(String recurrenceType) {
            message.setRecurrenceType(recurrenceType);
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            message.setMetadata(metadata);
            return this;
        }

        public JobExecutionMessage build() {
            return message;
        }
    }
}
