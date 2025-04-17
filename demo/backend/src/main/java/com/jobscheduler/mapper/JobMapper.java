package com.jobscheduler.mapper;

import com.jobscheduler.dto.JobRequest;
import com.jobscheduler.model.Job;
import com.jobscheduler.model.JobStatus;
import com.jobscheduler.model.JobType;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class JobMapper {

    public Job toEntity(JobRequest jobRequest) {
        Job job = new Job();
        job.setId(UUID.randomUUID());
        job.setName(jobRequest.getName());
        job.setDescription(jobRequest.getDescription());
        job.setJarFile(jobRequest.getJarFile());
        job.setType(jobRequest.getType());
        job.setStatus(JobStatus.SCHEDULED);
        job.setScheduledAt(jobRequest.getScheduledAt());
        job.setCronExpression(jobRequest.getCronExpression());
        job.setFrequency(jobRequest.getFrequency());
        job.setArguments(jobRequest.getArguments());
        job.setCreatedAt(LocalDateTime.now());
        job.setPriority(1);
        job.setExecutionCount(0);
        job.setMaxExecutions(jobRequest.getMaxExecutions() > 0 ? jobRequest.getMaxExecutions() : 1);
        
        // Set next run time based on scheduled time
        if (job.getScheduledAt() != null) {
            job.setNextRunAt(job.getScheduledAt());
        } else if (job.getType() != null && job.getType().equals(JobType.IMMEDIATE)) {
            job.setNextRunAt(LocalDateTime.now());
        }
        
        return job;
    }
    
    public void updateEntityFromRequest(Job job, JobRequest jobRequest) {
        job.setName(jobRequest.getName());
        job.setDescription(jobRequest.getDescription());
        job.setJarFile(jobRequest.getJarFile());
        job.setType(jobRequest.getType());
        job.setScheduledAt(jobRequest.getScheduledAt());
        job.setCronExpression(jobRequest.getCronExpression());
        job.setArguments(jobRequest.getArguments());
        
        // Update next run time if scheduled time changed
        if (jobRequest.getScheduledAt() != null) {
            job.setNextRunAt(jobRequest.getScheduledAt());
        }
    }
} 