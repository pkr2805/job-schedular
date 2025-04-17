package com.jobscheduler.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

import com.jobscheduler.model.JobStatus;
import com.jobscheduler.model.JobType;
import com.jobscheduler.model.JobFrequency;

@Entity
@Table(name = "jobs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String jarFile;

    @Column
    private String description;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private JobType type;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private JobStatus status;

    @Column
    @Enumerated(EnumType.STRING)
    private JobStatus previousStatus;

    @Column
    @Enumerated(EnumType.STRING)
    private JobFrequency frequency;

    @Column
    private LocalDateTime scheduledAt;

    @Column
    private LocalDateTime nextRunAt;

    @Column
    private LocalDateTime lastRunAt;

    @Column
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private int priority;

    @Column(nullable = false)
    private int executionCount;

    @Column(nullable = false)
    private int maxExecutions;

    @Column(columnDefinition = "TEXT")
    private String cronExpression;

    @Column(columnDefinition = "TEXT")
    private String arguments;

    @Column(columnDefinition = "TEXT")
    private String output;

    @Column(columnDefinition = "TEXT")
    private String error;

    // Kafka message related fields
    @Column
    private String kafkaMessageId;
    
    @Column
    private LocalDateTime kafkaMessageSent;
    
    @Column
    private String kafkaMessageStatus; // SUCCESS, FAILED, PENDING
    
    @Column(columnDefinition = "TEXT")
    private String kafkaMessageResponse; // JSON response from Kafka

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
} 