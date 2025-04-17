package com.jobscheduler.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/notifications")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class JobNotificationController {

    // In-memory notification storage for demo purposes
    // In a production environment, these would be stored in a database
    private static final List<JobNotification> notifications = Collections.synchronizedList(new ArrayList<>());
    
    static {
        // Add some sample notifications
        notifications.add(new JobNotification(
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString(),
            "Sample Scheduled Job",
            "Job has been scheduled successfully",
            "SUCCESS",
            LocalDateTime.now().minusHours(1).toString(),
            false
        ));
        
        notifications.add(new JobNotification(
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString(),
            "Sample Failed Job",
            "Job execution failed: Connection timeout",
            "ERROR",
            LocalDateTime.now().minusMinutes(30).toString(),
            false
        ));
    }

    @GetMapping
    public ResponseEntity<List<JobNotification>> getAllNotifications() {
        log.info("Fetching all notifications");
        return ResponseEntity.ok(new ArrayList<>(notifications));
    }
    
    @GetMapping("/job/{jobId}")
    public ResponseEntity<List<JobNotification>> getNotificationsForJob(@PathVariable String jobId) {
        log.info("Fetching notifications for job: {}", jobId);
        List<JobNotification> jobNotifications = notifications.stream()
            .filter(n -> n.getJobId().equals(jobId))
            .collect(Collectors.toList());
        return ResponseEntity.ok(jobNotifications);
    }
    
    @PostMapping("/{id}/read")
    public ResponseEntity<Map<String, Boolean>> markAsRead(@PathVariable String id) {
        log.info("Marking notification as read: {}", id);
        boolean found = false;
        
        for (JobNotification notification : notifications) {
            if (notification.getId().equals(id)) {
                notification.setRead(true);
                found = true;
                break;
            }
        }
        
        Map<String, Boolean> response = new HashMap<>();
        response.put("success", found);
        
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Boolean>> deleteNotification(@PathVariable String id) {
        log.info("Deleting notification: {}", id);
        boolean removed = notifications.removeIf(notification -> notification.getId().equals(id));
        
        Map<String, Boolean> response = new HashMap<>();
        response.put("success", removed);
        
        return ResponseEntity.ok(response);
    }
    
    // Add a notification (typically called from other services)
    public static void addNotification(String jobId, String jobName, String message, String type) {
        JobNotification notification = new JobNotification(
            UUID.randomUUID().toString(),
            jobId,
            jobName,
            message,
            type,
            LocalDateTime.now().toString(),
            false
        );
        
        notifications.add(notification);
    }
    
    // Static method to get all current notifications
    public static List<JobNotification> getCurrentNotifications() {
        return new ArrayList<>(notifications);
    }
    
    // Simple notification model
    public static class JobNotification {
        private String id;
        private String jobId;
        private String jobName;
        private String message;
        private String type;
        private String timestamp;
        private boolean read;
        
        public JobNotification(String id, String jobId, String jobName, String message, 
                              String type, String timestamp, boolean read) {
            this.id = id;
            this.jobId = jobId;
            this.jobName = jobName;
            this.message = message;
            this.type = type;
            this.timestamp = timestamp;
            this.read = read;
        }
        
        public String getId() {
            return id;
        }
        
        public String getJobId() {
            return jobId;
        }
        
        public String getJobName() {
            return jobName;
        }
        
        public String getMessage() {
            return message;
        }
        
        public String getType() {
            return type;
        }
        
        public String getTimestamp() {
            return timestamp;
        }
        
        public boolean isRead() {
            return read;
        }
        
        public void setRead(boolean read) {
            this.read = read;
        }
    }
} 