package com.jobscheduler.dto;

import com.jobscheduler.model.Job;
import com.jobscheduler.model.JobStatus;
import com.jobscheduler.model.JobType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobDTO {
    private UUID id;
    private String name;
    private String jarFile;
    private String description;
    private JobType type;
    private JobStatus status;
    private JobStatus previousStatus;
    private LocalDateTime scheduledAt;
    private LocalDateTime nextRunAt;
    private LocalDateTime lastRunAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int priority;
    private int executionCount;
    private int maxExecutions;
    private String cronExpression;
    private String arguments;
    private String output;
    private String error;
    
    // Kafka message related fields
    private String kafkaMessageId;
    private LocalDateTime kafkaMessageSent;
    private String kafkaMessageStatus; // SUCCESS, FAILED, PENDING
    private String kafkaMessageResponse; // JSON response from Kafka
    
    public static JobDTO fromEntity(Job job) {
        return JobDTO.builder()
                .id(job.getId())
                .name(job.getName())
                .jarFile(job.getJarFile())
                .description(job.getDescription())
                .type(job.getType())
                .status(job.getStatus())
                .previousStatus(job.getPreviousStatus())
                .scheduledAt(job.getScheduledAt())
                .nextRunAt(job.getNextRunAt())
                .lastRunAt(job.getLastRunAt())
                .createdAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .priority(job.getPriority())
                .executionCount(job.getExecutionCount())
                .maxExecutions(job.getMaxExecutions())
                .cronExpression(job.getCronExpression())
                .arguments(job.getArguments())
                .output(job.getOutput())
                .error(job.getError())
                .kafkaMessageId(job.getKafkaMessageId())
                .kafkaMessageSent(job.getKafkaMessageSent())
                .kafkaMessageStatus(job.getKafkaMessageStatus())
                .kafkaMessageResponse(job.getKafkaMessageResponse())
                .build();
    }
    
    public Job toEntity() {
        return Job.builder()
                .id(this.id)
                .name(this.name)
                .jarFile(this.jarFile)
                .description(this.description)
                .type(this.type)
                .status(this.status)
                .previousStatus(this.previousStatus)
                .scheduledAt(this.scheduledAt)
                .nextRunAt(this.nextRunAt)
                .lastRunAt(this.lastRunAt)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .priority(this.priority)
                .executionCount(this.executionCount)
                .maxExecutions(this.maxExecutions)
                .cronExpression(this.cronExpression)
                .arguments(this.arguments)
                .output(this.output)
                .error(this.error)
                .kafkaMessageId(this.kafkaMessageId)
                .kafkaMessageSent(this.kafkaMessageSent)
                .kafkaMessageStatus(this.kafkaMessageStatus)
                .kafkaMessageResponse(this.kafkaMessageResponse)
                .build();
    }
} 