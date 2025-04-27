package com.lemnisk.jobscheduler.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class JobExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "job_schedule_id")
    private JobSchedule jobSchedule;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    private ExecutionStatus status;

    @Column(columnDefinition = "TEXT")
    private String logs;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @Column(columnDefinition = "TEXT")
    private String response;

    private String executionTime; // in milliseconds or formatted string

    public enum ExecutionStatus {
        STARTED, COMPLETED, FAILED
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public JobSchedule getJobSchedule() {
        return jobSchedule;
    }

    public void setJobSchedule(JobSchedule jobSchedule) {
        this.jobSchedule = jobSchedule;
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

    public ExecutionStatus getStatus() {
        return status;
    }

    public void setStatus(ExecutionStatus status) {
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

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(String executionTime) {
        this.executionTime = executionTime;
    }

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private JobExecution jobExecution = new JobExecution();

        public Builder jobSchedule(JobSchedule jobSchedule) {
            jobExecution.setJobSchedule(jobSchedule);
            return this;
        }

        public Builder startTime(LocalDateTime startTime) {
            jobExecution.setStartTime(startTime);
            return this;
        }

        public Builder endTime(LocalDateTime endTime) {
            jobExecution.setEndTime(endTime);
            return this;
        }

        public Builder status(ExecutionStatus status) {
            jobExecution.setStatus(status);
            return this;
        }

        public Builder logs(String logs) {
            jobExecution.setLogs(logs);
            return this;
        }

        public Builder errorMessage(String errorMessage) {
            jobExecution.setErrorMessage(errorMessage);
            return this;
        }

        public Builder response(String response) {
            jobExecution.setResponse(response);
            return this;
        }

        public Builder executionTime(String executionTime) {
            jobExecution.setExecutionTime(executionTime);
            return this;
        }

        public JobExecution build() {
            return jobExecution;
        }
    }
}
