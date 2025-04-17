package com.jobscheduler.service;

import com.jobscheduler.controller.JobNotificationController;
import com.jobscheduler.model.Job;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A direct logging implementation for job status updates
 */
@Service
@Slf4j
public class DirectNotificationService {

    /**
     * Send a job status update notification
     */
    public void sendJobStatusUpdate(Job job) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("jobId", job.getId().toString());
        notification.put("status", job.getStatus().toString());
        notification.put("name", job.getName());
        notification.put("jarFile", job.getJarFile());
        notification.put("timestamp", job.getUpdatedAt() != null ? job.getUpdatedAt().toString() 
                                                               : job.getCreatedAt().toString());
        notification.put("executionTime", job.getLastRunAt() != null ? job.getLastRunAt().toString() : null);
        notification.put("output", job.getOutput());
        notification.put("error", job.getError());
        
        log.info("DIRECT NOTIFICATION: Topic=job-status, Key={}, Value={}", job.getId(), notification);
        
        // Add notification to the controller
        String message = String.format("Job status changed to %s", job.getStatus().toString());
        if (job.getOutput() != null && !job.getOutput().isEmpty()) {
            message += ": " + job.getOutput();
        }
        if (job.getError() != null && !job.getError().isEmpty()) {
            message += ": " + job.getError();
        }
        
        String type = determineNotificationType(job.getStatus().toString());
        JobNotificationController.addNotification(
            job.getId().toString(),
            job.getName(),
            message,
            type
        );
    }
    
    /**
     * Send a job error notification
     */
    public void sendJobErrorNotification(UUID jobId, String errorMessage) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("jobId", jobId.toString());
        notification.put("status", "FAILED");
        notification.put("timestamp", LocalDateTime.now().toString());
        notification.put("error", errorMessage);
        
        log.info("DIRECT NOTIFICATION: Topic=job-status, Key={}, Value={}", jobId, notification);
        
        // Add notification to the controller
        JobNotificationController.addNotification(
            jobId.toString(),
            "Job " + jobId.toString(), // We don't have the name here
            "Job failed: " + errorMessage,
            "ERROR"
        );
    }
    
    /**
     * Helper method to determine notification type based on job status
     */
    private String determineNotificationType(String status) {
        switch (status) {
            case "COMPLETED":
                return "SUCCESS";
            case "FAILED":
                return "ERROR";
            case "SCHEDULED":
            case "RUNNING":
                return "INFO";
            case "CANCELLED":
            case "PAUSED":
                return "WARNING";
            default:
                return "INFO";
        }
    }
} 