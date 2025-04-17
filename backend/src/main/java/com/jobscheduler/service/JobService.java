package com.jobscheduler.service;

import com.jobscheduler.model.Job;
import com.jobscheduler.model.JobStatus;
import com.jobscheduler.model.JobType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface JobService {
    
    // CRUD operations
    Job createJob(Job job);
    
    Job updateJob(UUID id, Job job);
    
    void deleteJob(UUID id);
    
    Job getJobById(UUID id);
    
    List<Job> getAllJobs();
    
    Page<Job> getAllJobs(Pageable pageable);
    
    // Status-based operations
    List<Job> getJobsByStatus(JobStatus status);
    
    List<Job> getJobsByType(JobType type);
    
    List<Job> getJobsByJarFile(String jarFile);
    
    // Job lifecycle management
    Job scheduleJob(UUID id);
    
    Job executeJob(UUID id);
    
    Job pauseJob(UUID id);
    
    Job resumeJob(UUID id);
    
    Job cancelJob(UUID id);
    
    Job restartJob(UUID id);
    
    // Batch operations
    List<Job> getJobsDueForExecution();
    
    void executeAllPendingJobs();
    
    // Scheduled operations
    void checkAndExecuteScheduledJobs();
    
    void updateRecurringJobs();
    
    // Job output management
    String getJobOutput(UUID id);
    
    String getJobError(UUID id);
    
    // Job search and filtering
    List<Job> searchJobs(String keyword);
    
    List<Job> getJobsCreatedBetween(LocalDateTime start, LocalDateTime end);
    
    // Statistics
    long countJobsByStatus(JobStatus status);
    
    long countJobsByType(JobType type);
    
    // Maintenance
    void cleanupOldJobs();
    
    void cleanupOldJobs(int daysToKeep);
} 