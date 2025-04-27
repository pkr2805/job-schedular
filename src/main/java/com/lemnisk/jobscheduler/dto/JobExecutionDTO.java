package com.lemnisk.jobscheduler.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class JobExecutionDTO {
    private UUID id;
    private UUID jobScheduleId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private String logs;
    private String errorMessage;
    private String executionTime;
    private String response;

    public JobExecutionDTO() {
    }

    public JobExecutionDTO(UUID id, UUID jobScheduleId, LocalDateTime startTime, LocalDateTime endTime,
            String status, String logs, String errorMessage, String executionTime, String response) {
        this.id = id;
        this.jobScheduleId = jobScheduleId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.logs = logs;
        this.errorMessage = errorMessage;
        this.executionTime = executionTime;
        this.response = response;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getJobScheduleId() {
        return jobScheduleId;
    }

    public void setJobScheduleId(UUID jobScheduleId) {
        this.jobScheduleId = jobScheduleId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLogs() {
        return logs;
    }

    public void setLogs(String logs) {
        this.logs = logs;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(String executionTime) {
        this.executionTime = executionTime;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private JobExecutionDTO dto = new JobExecutionDTO();

        public Builder id(UUID id) {
            dto.setId(id);
            return this;
        }

        public Builder jobScheduleId(UUID jobScheduleId) {
            dto.setJobScheduleId(jobScheduleId);
            return this;
        }

        public Builder startTime(LocalDateTime startTime) {
            dto.setStartTime(startTime);
            return this;
        }

        public Builder endTime(LocalDateTime endTime) {
            dto.setEndTime(endTime);
            return this;
        }

        public Builder status(String status) {
            dto.setStatus(status);
            return this;
        }

        public Builder logs(String logs) {
            dto.setLogs(logs);
            return this;
        }

        public Builder errorMessage(String errorMessage) {
            dto.setErrorMessage(errorMessage);
            return this;
        }

        public Builder executionTime(String executionTime) {
            dto.setExecutionTime(executionTime);
            return this;
        }

        public Builder response(String response) {
            dto.setResponse(response);
            return this;
        }

        public JobExecutionDTO build() {
            return dto;
        }
    }
}
