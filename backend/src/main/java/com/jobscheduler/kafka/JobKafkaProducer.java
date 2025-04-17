package com.jobscheduler.kafka;

import com.jobscheduler.model.Job;
import com.jobscheduler.model.JobNotification;
import com.jobscheduler.model.JobStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobKafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.kafka.template.default-topic}")
    private String topicName;

    /**
     * Send a job status update to Kafka
     */
    public void sendJobStatusUpdate(Job job) {
        String key = job.getId().toString();
        try {
            log.info("Sending job status update to Kafka: {}", job);
            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topicName, key, job);
            
            future.whenComplete((result, exception) -> {
                if (exception == null) {
                    log.info("Job status update sent successfully: {}", job.getId());
                } else {
                    log.error("Failed to send job status update to Kafka: {}", exception.getMessage());
                }
            });
        } catch (Exception e) {
            log.error("Error sending job status update to Kafka", e);
        }
    }

    /**
     * Send a job notification to Kafka
     */
    public void sendJobNotification(Job job, String message) {
        JobNotification notification = createNotificationFromJob(job, message);
        String key = notification.getId();
        
        try {
            log.info("Sending job notification to Kafka: {}", notification);
            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topicName, key, notification);
            
            future.whenComplete((result, exception) -> {
                if (exception == null) {
                    log.info("Job notification sent successfully: {}", notification.getId());
                } else {
                    log.error("Failed to send job notification to Kafka: {}", exception.getMessage());
                }
            });
        } catch (Exception e) {
            log.error("Error sending job notification to Kafka", e);
        }
    }

    /**
     * Create a notification from a job
     */
    private JobNotification createNotificationFromJob(Job job, String message) {
        JobNotification notification = new JobNotification();
        notification.setId(UUID.randomUUID().toString());
        notification.setJobId(job.getId().toString());
        
        // Set notification type based on job status
        if (job.getStatus() == JobStatus.COMPLETED) {
            notification.setTitle("Job Completed");
            notification.setType("SUCCESS");
        } else if (job.getStatus() == JobStatus.FAILED) {
            notification.setTitle("Job Failed");
            notification.setType("ERROR");
        } else if (job.getStatus() == JobStatus.SCHEDULED) {
            notification.setTitle("Job Scheduled");
            notification.setType("INFO");
        } else {
            notification.setTitle("Job Status Update");
            notification.setType("INFO");
        }
        
        notification.setMessage(message);
        notification.setTimestamp(java.time.Instant.now().toString());
        notification.setRead(false);
        
        return notification;
    }
} 