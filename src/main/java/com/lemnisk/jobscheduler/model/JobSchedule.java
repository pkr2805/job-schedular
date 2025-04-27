package com.lemnisk.jobscheduler.model;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

@Entity
public class JobSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "jar_file_id")
    private JarFile jarFile;

    @Enumerated(EnumType.STRING)
    private ExecutionType executionType;

    private LocalDateTime scheduledTime;

    @Enumerated(EnumType.STRING)
    private RecurrenceType recurrenceType;

    @Enumerated(EnumType.STRING)
    private JobStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum ExecutionType {
        IMMEDIATE, SCHEDULED
    }

    public enum RecurrenceType {
        ONE_TIME, HOURLY, DAILY, WEEKLY
    }

    public enum JobStatus {
        SCHEDULED, RUNNING, COMPLETED, FAILED, CANCELLED
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public JarFile getJarFile() {
        return jarFile;
    }

    public void setJarFile(JarFile jarFile) {
        this.jarFile = jarFile;
    }

    public ExecutionType getExecutionType() {
        return executionType;
    }

    public void setExecutionType(ExecutionType executionType) {
        this.executionType = executionType;
    }

    public LocalDateTime getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(LocalDateTime scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public RecurrenceType getRecurrenceType() {
        return recurrenceType;
    }

    public void setRecurrenceType(RecurrenceType recurrenceType) {
        this.recurrenceType = recurrenceType;
    }

    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
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

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private JobSchedule jobSchedule = new JobSchedule();

        public Builder jarFile(JarFile jarFile) {
            jobSchedule.setJarFile(jarFile);
            return this;
        }

        public Builder executionType(ExecutionType executionType) {
            jobSchedule.setExecutionType(executionType);
            return this;
        }

        public Builder scheduledTime(LocalDateTime scheduledTime) {
            jobSchedule.setScheduledTime(scheduledTime);
            return this;
        }

        public Builder recurrenceType(RecurrenceType recurrenceType) {
            jobSchedule.setRecurrenceType(recurrenceType);
            return this;
        }

        public Builder status(JobStatus status) {
            jobSchedule.setStatus(status);
            return this;
        }

        public JobSchedule build() {
            return jobSchedule;
        }
    }
}
