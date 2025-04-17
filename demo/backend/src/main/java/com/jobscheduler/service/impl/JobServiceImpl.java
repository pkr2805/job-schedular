package com.jobscheduler.service.impl;

import com.jobscheduler.model.Job;
import com.jobscheduler.model.JobFrequency;
import com.jobscheduler.model.JobStatus;
import com.jobscheduler.model.JobType;
import com.jobscheduler.repository.JobRepository;
import com.jobscheduler.service.JobService;
import com.jobscheduler.service.NotificationService;
import com.jobscheduler.service.DirectNotificationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.time.ZoneId;
import java.util.Date;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobServiceImpl implements JobService {

    private final JobRepository jobRepository;
    private final JarExecutionService jarExecutionService;
    private final NotificationService notificationService;
    private final DirectNotificationService directNotificationService;
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    // CRUD operations
    @Override
    @Transactional
    public Job createJob(Job job) {
        // Set default values if not provided
        if (job.getStatus() == null) {
            job.setStatus(JobStatus.PENDING);
        }
        
        // For immediate jobs, set next run time to now
        if (job.getType() == JobType.IMMEDIATE) {
            job.setNextRunAt(LocalDateTime.now());
        }
        
        // For scheduled jobs, set next run time to scheduled time
        if (job.getType() == JobType.SCHEDULED && job.getScheduledAt() != null) {
            job.setNextRunAt(job.getScheduledAt());
        }
        
        return jobRepository.save(job);
    }

    @Override
    @Transactional
    public Job updateJob(UUID id, Job job) {
        if (!jobRepository.existsById(id)) {
            throw new EntityNotFoundException("Job not found with id: " + id);
        }
        job.setId(id);
        return jobRepository.save(job);
    }

    @Override
    @Transactional
    public void deleteJob(UUID id) {
        if (!jobRepository.existsById(id)) {
            throw new EntityNotFoundException("Job not found with id: " + id);
        }
        jobRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Job getJobById(UUID id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Job not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Job> getAllJobs() {
        return jobRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Job> getAllJobs(Pageable pageable) {
        return jobRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    // Status-based operations
    @Override
    @Transactional(readOnly = true)
    public List<Job> getJobsByStatus(JobStatus status) {
        return jobRepository.findByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Job> getJobsByType(JobType type) {
        return jobRepository.findByType(type);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Job> getJobsByJarFile(String jarFile) {
        return jobRepository.findByJarFile(jarFile);
    }

    // Job lifecycle management
    @Override
    @Transactional
    public Job scheduleJob(UUID id) {
        Job job = getJobById(id);
        
        if (job.getStatus() == JobStatus.RUNNING) {
            throw new IllegalStateException("Cannot schedule a job that is already running");
        }
        
        job.setStatus(JobStatus.SCHEDULED);
        
        // If scheduled time is not set, schedule for immediate execution
        if (job.getScheduledAt() == null) {
            job.setScheduledAt(LocalDateTime.now());
        }
        
        job.setNextRunAt(job.getScheduledAt());
        
        return jobRepository.save(job);
    }

    @Override
    @Transactional
    public Job executeJob(UUID id) {
        Job job = getJobById(id);
        
        if (job.getStatus() == JobStatus.RUNNING) {
            throw new IllegalStateException("Job is already running");
        }
        
        job.setStatus(JobStatus.RUNNING);
        job.setLastRunAt(LocalDateTime.now());
        job.setUpdatedAt(LocalDateTime.now());
        job = jobRepository.save(job);
        
        // Send notification that job is running (using Kafka)
        try {
            // Send the notification via Kafka
            notificationService.sendJobStatusUpdate(job);
            log.info("Sent job running notification via Kafka for job: {}", job.getId());
        } catch (Exception e) {
            // Fallback to direct notification if Kafka fails
            log.warn("Failed to send Kafka notification, using direct notification: {}", e.getMessage());
            directNotificationService.sendJobStatusUpdate(job);
        }
        
        // Add a notification via controller for UI display
        com.jobscheduler.controller.JobNotificationController.addNotification(
            job.getId().toString(),
            job.getName(),
            "Job execution started",
            "INFO"
        );
        
        final Job savedJob = job;
        
        // Execute job asynchronously with reduced delay
        CompletableFuture.runAsync(() -> processJob(savedJob), executorService);
        
        return job;
    }

    @Override
    @Transactional
    public Job pauseJob(UUID id) {
        Job job = getJobById(id);
        
        if (job.getStatus() != JobStatus.RUNNING && 
            job.getStatus() != JobStatus.SCHEDULED) {
            throw new IllegalStateException("Only running or scheduled jobs can be paused");
        }
        
        // Store the previous status
        job.setPreviousStatus(job.getStatus());
        job.setStatus(JobStatus.PAUSED);
        
        return jobRepository.save(job);
    }

    @Override
    @Transactional
    public Job resumeJob(UUID id) {
        Job job = getJobById(id);
        
        if (job.getStatus() != JobStatus.PAUSED) {
            throw new IllegalStateException("Only paused jobs can be resumed");
        }
        
        // Restore the previous status or default to SCHEDULED
        JobStatus previousStatus = job.getPreviousStatus();
        job.setStatus(previousStatus != null ? previousStatus : JobStatus.SCHEDULED);
        job.setPreviousStatus(null);
        
        return jobRepository.save(job);
    }

    @Override
    @Transactional
    public Job cancelJob(UUID id) {
        Job job = getJobById(id);
        
        // Only running, scheduled, or paused jobs can be cancelled
        if (job.getStatus() != JobStatus.RUNNING && 
            job.getStatus() != JobStatus.SCHEDULED && 
            job.getStatus() != JobStatus.PAUSED) {
            throw new IllegalStateException("Job cannot be cancelled in its current state");
        }
        
        job.setStatus(JobStatus.CANCELLED);
        
        return jobRepository.save(job);
    }

    @Override
    @Transactional
    public Job restartJob(UUID id) {
        Job job = getJobById(id);
        
        // Only failed, completed, or cancelled jobs can be restarted
        if (job.getStatus() != JobStatus.FAILED && 
            job.getStatus() != JobStatus.COMPLETED && 
            job.getStatus() != JobStatus.CANCELLED) {
            throw new IllegalStateException("Job cannot be restarted in its current state");
        }
        
        job.setStatus(JobStatus.SCHEDULED);
        job.setNextRunAt(LocalDateTime.now());
        job.setOutput(null);
        job.setError(null);
        
        return jobRepository.save(job);
    }

    // Batch operations
    @Override
    @Transactional(readOnly = true)
    public List<Job> getJobsDueForExecution() {
        return jobRepository.findJobsDueForExecution(LocalDateTime.now(), JobStatus.SCHEDULED);
    }

    @Override
    @Transactional
    public void executeAllPendingJobs() {
        List<Job> pendingJobs = getJobsDueForExecution();
        
        for (Job job : pendingJobs) {
            try {
                executeJob(job.getId());
            } catch (Exception e) {
                log.error("Error executing job: {}", job.getId(), e);
            }
        }
    }

    // Scheduled operations
    @Override
    @Scheduled(fixedRate = 60000) // Check every minute
    @Transactional
    public void checkAndExecuteScheduledJobs() {
        log.debug("Checking for scheduled jobs...");
        
        List<Job> dueJobs = getJobsDueForExecution();
        
        log.debug("Found {} jobs due for execution", dueJobs.size());
        
        for (Job job : dueJobs) {
            try {
                executeJob(job.getId());
            } catch (Exception e) {
                log.error("Error executing scheduled job: {}", job.getId(), e);
            }
        }
    }

    @Override
    @Scheduled(cron = "0 0 * * * *") // Run every hour
    @Transactional
    public void updateRecurringJobs() {
        log.debug("Updating recurring jobs...");
        
        List<Job> recurringJobs = jobRepository.findByTypeAndCronExpressionIsNotNull(JobType.RECURRING);
        
        for (Job job : recurringJobs) {
            // Skip jobs that are still running or paused
            if (job.getStatus() == JobStatus.RUNNING || job.getStatus() == JobStatus.PAUSED) {
                continue;
            }
            
            // Skip jobs that have reached their max executions
            if (job.getMaxExecutions() > 0 && job.getExecutionCount() >= job.getMaxExecutions()) {
                continue;
            }
            
            // If job is completed or failed, reschedule it for the next run
            if (job.getStatus() == JobStatus.COMPLETED || job.getStatus() == JobStatus.FAILED) {
                job.setStatus(JobStatus.SCHEDULED);
                
                // For simplicity, just schedule for next hour
                // In a real implementation, this would use a cron parser
                job.setNextRunAt(LocalDateTime.now().plusHours(1));
                
                jobRepository.save(job);
            }
        }
    }

    // Job output management
    @Override
    @Transactional(readOnly = true)
    public String getJobOutput(UUID id) {
        Job job = getJobById(id);
        return job.getOutput();
    }

    @Override
    @Transactional(readOnly = true)
    public String getJobError(UUID id) {
        Job job = getJobById(id);
        return job.getError();
    }

    // Job search and filtering
    @Override
    @Transactional(readOnly = true)
    public List<Job> searchJobs(String keyword) {
        return jobRepository.findByNameContainingIgnoreCase(keyword);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Job> getJobsCreatedBetween(LocalDateTime start, LocalDateTime end) {
        // This would typically be implemented with a custom query
        // For now, we'll filter all jobs in the application
        List<Job> allJobs = jobRepository.findAll();
        List<Job> filteredJobs = new ArrayList<>();
        
        for (Job job : allJobs) {
            if (job.getCreatedAt() != null && 
                !job.getCreatedAt().isBefore(start) && 
                !job.getCreatedAt().isAfter(end)) {
                filteredJobs.add(job);
            }
        }
        
        return filteredJobs;
    }

    // Statistics
    @Override
    @Transactional(readOnly = true)
    public long countJobsByStatus(JobStatus status) {
        return jobRepository.countByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public long countJobsByType(JobType type) {
        return jobRepository.countByType(type);
    }

    // Maintenance
    @Override
    @Scheduled(cron = "0 0 0 * * *") // Run daily at midnight
    @Transactional
    public void cleanupOldJobs() {
        int daysToKeep = 30; // Default value
        cleanupOldJobs(daysToKeep);
    }
    
    @Override
    @Transactional
    public void cleanupOldJobs(int daysToKeep) {
        log.info("Running cleanup job for jobs older than {} days", daysToKeep);
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
        
        List<Job> oldJobs = jobRepository.findByCreatedAtBeforeAndStatusIn(
                cutoffDate, 
                List.of(JobStatus.COMPLETED, JobStatus.FAILED, JobStatus.CANCELLED));
        
        log.info("Found {} old jobs to clean up", oldJobs.size());
        jobRepository.deleteAll(oldJobs);
    }

    // Helper methods
    private void processJob(Job job) {
        log.info("Processing job: {} ({})", job.getName(), job.getId());
        
        String jarFile = job.getJarFile();
        boolean success = false;
        String output = "";
        String error = "";
        
        try {
            // Simulate faster processing with a minimal delay
            long executionTime = 200; // 200ms - extremely fast for demo
            Thread.sleep(executionTime);
            
            // Higher success rate for demo
            success = new Random().nextDouble() > 0.05; // 95% success rate
            
            if (success) {
                log.info("Job {} executed successfully", job.getId());
                output = "Job executed successfully in " + executionTime + "ms";
            } else {
                log.warn("Job {} failed execution", job.getId());
                error = "Job execution failed: Simulated failure";
            }
            
        } catch (Exception e) {
            log.error("Error executing job {}: {}", job.getId(), e.getMessage(), e);
            error = "Error: " + e.getMessage();
        }
        
        // Update job status and send notification immediately
        updateJobAfterExecution(job.getId(), success, output, error);
    }

    @Transactional
    public void updateJobAfterExecution(UUID jobId, boolean success, String output, String error) {
        Job job = getJobById(jobId);
        
        // Update execution count and job detail regardless of type
        job.setOutput(output);
        job.setError(error);
        job.setExecutionCount(job.getExecutionCount() + 1);
        job.setLastRunAt(LocalDateTime.now());
        job.setUpdatedAt(LocalDateTime.now());
        
        // Handle different job types differently
        if (job.getType() == JobType.RECURRING || 
            (job.getType() == JobType.SCHEDULED && job.getFrequency() != null && !job.getFrequency().equals(JobFrequency.ONE_TIME))) {
            
            // For recurring jobs, set status to SCHEDULED for next run
            job.setStatus(JobStatus.SCHEDULED);
            
            // Update next run time based on frequency
            if (job.getFrequency() == JobFrequency.HOURLY) {
                job.setNextRunAt(LocalDateTime.now().plusHours(1));
                log.info("Scheduled hourly job {} for next execution at {}", job.getId(), job.getNextRunAt());
            } else if (job.getFrequency() == JobFrequency.DAILY) {
                job.setNextRunAt(LocalDateTime.now().plusDays(1));
                log.info("Scheduled daily job {} for next execution at {}", job.getId(), job.getNextRunAt());
            } else if (job.getFrequency() == JobFrequency.WEEKLY) {
                job.setNextRunAt(LocalDateTime.now().plusWeeks(1));
                log.info("Scheduled weekly job {} for next execution at {}", job.getId(), job.getNextRunAt());
            } else if (job.getFrequency() == JobFrequency.MONTHLY) {
                job.setNextRunAt(LocalDateTime.now().plusMonths(1));
                log.info("Scheduled monthly job {} for next execution at {}", job.getId(), job.getNextRunAt());
            } else if (job.getCronExpression() != null) {
                // Use cron expression if specified (this is a more advanced option)
                // For simplicity, just add one hour
                job.setNextRunAt(LocalDateTime.now().plusHours(1));
                log.info("Scheduled job with cron expression for next execution at {}", job.getNextRunAt());
            }
            
            // Check if job has reached max executions
            if (job.getMaxExecutions() > 0 && job.getExecutionCount() >= job.getMaxExecutions()) {
                job.setStatus(success ? JobStatus.COMPLETED : JobStatus.FAILED);
                job.setNextRunAt(null);
                log.info("Job {} reached max executions ({}). Marking as {}", 
                        job.getId(), job.getMaxExecutions(), job.getStatus());
            }
        } else {
            // For one-time jobs, update status to COMPLETED or FAILED
            job.setStatus(success ? JobStatus.COMPLETED : JobStatus.FAILED);
            job.setNextRunAt(null);
        }
        
        // Save the job with updated status
        job = jobRepository.save(job);
        
        // Send notification about job completion via Kafka
        try {
            notificationService.sendJobStatusUpdate(job);
            log.info("Sent job completion notification via Kafka for job: {}", job.getId());
        } catch (Exception e) {
            log.warn("Failed to send Kafka notification, using direct notification: {}", e.getMessage());
            directNotificationService.sendJobStatusUpdate(job);
        }
        
        // Add notification to the controller for UI display
        String type = success ? "SUCCESS" : "ERROR";
        String message;
        if (job.getStatus() == JobStatus.SCHEDULED) {
            message = success ? 
                "Job executed successfully and scheduled for next run at " + job.getNextRunAt() : 
                "Job execution failed but scheduled for retry at " + job.getNextRunAt();
        } else {
            message = success ? "Job completed successfully: " + output : "Job failed: " + error;
        }
        
        com.jobscheduler.controller.JobNotificationController.addNotification(
            job.getId().toString(),
            job.getName(),
            message,
            type
        );
    }
} 