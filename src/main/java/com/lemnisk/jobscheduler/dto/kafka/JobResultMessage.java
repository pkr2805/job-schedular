package com.lemnisk.jobscheduler.dto.kafka;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public class JobResultMessage {
    private UUID jobId;
    private String status; // "success" or "failure"
    private LocalDateTime timestamp;
    private String error;
    private String logs;
    private String executionTime;
    private Map<String, Object> metadata;

    public JobResultMessage() {
    }

    public JobResultMessage(UUID jobId, String status, LocalDateTime timestamp, String error, String logs,
            String executionTime, Map<String, Object> metadata) {
        this.jobId = jobId;
        this.status = status;
        this.timestamp = timestamp;
        this.error = error;
        this.logs = logs;
        this.executionTime = executionTime;
        this.metadata = metadata;
    }

    public UUID getJobId() {
        return jobId;
    }

    public void setJobId(UUID jobId) {
        this.jobId = jobId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getLogs() {
        return logs;
    }

    public void setLogs(String logs) {
        this.logs = logs;
    }

    public String getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(String executionTime) {
        this.executionTime = executionTime;
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
        private JobResultMessage message = new JobResultMessage();

        public Builder jobId(UUID jobId) {
            message.setJobId(jobId);
            return this;
        }

        public Builder status(String status) {
            message.setStatus(status);
            return this;
        }

        public Builder timestamp(LocalDateTime timestamp) {
            message.setTimestamp(timestamp);
            return this;
        }

        public Builder error(String error) {
            message.setError(error);
            return this;
        }

        public Builder logs(String logs) {
            message.setLogs(logs);
            return this;
        }

        public Builder executionTime(String executionTime) {
            message.setExecutionTime(executionTime);
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            message.setMetadata(metadata);
            return this;
        }

        public JobResultMessage build() {
            return message;
        }
    }
}
