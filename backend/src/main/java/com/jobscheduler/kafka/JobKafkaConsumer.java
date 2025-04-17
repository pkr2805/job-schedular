package com.jobscheduler.kafka;

import com.jobscheduler.model.Job;
import com.jobscheduler.model.JobNotification;
import com.jobscheduler.repository.JobNotificationRepository;
import com.jobscheduler.repository.JobRepository;
import com.jobscheduler.service.JobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobKafkaConsumer {

    private final JobRepository jobRepository;
    private final JobNotificationRepository notificationRepository;
    private final JobService jobService;

    /**
     * Listen for job status updates
     */
    @KafkaListener(
        topics = "${spring.kafka.template.default-topic}",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void listen(@Payload Object payload) {
        try {
            log.info("Received message from Kafka: {}", payload);
            
            // Handle job status updates
            if (payload instanceof Job) {
                Job job = (Job) payload;
                log.info("Processing job status update: {}", job.getId());
                handleJobStatusUpdate(job);
            }
            
            // Handle job notifications
            else if (payload instanceof JobNotification) {
                JobNotification notification = (JobNotification) payload;
                log.info("Processing job notification: {}", notification.getId());
                saveNotification(notification);
            }
            
            // Handle unknown payload types
            else {
                log.warn("Unknown payload type received: {}", payload.getClass().getName());
            }
        } catch (Exception e) {
            log.error("Error processing Kafka message", e);
        }
    }

    /**
     * Handle job status updates
     */
    private void handleJobStatusUpdate(Job job) {
        try {
            // Find existing job in DB
            Optional<Job> existingJobOpt = jobRepository.findById(job.getId());
            
            if (existingJobOpt.isPresent()) {
                Job existingJob = existingJobOpt.get();
                
                // Update job status
                existingJob.setStatus(job.getStatus());
                
                // Update other job fields as needed
                if (job.getLastRunAt() != null) {
                    existingJob.setLastRunAt(job.getLastRunAt());
                }
                
                if (job.getNextRunAt() != null) {
                    existingJob.setNextRunAt(job.getNextRunAt());
                }
                
                existingJob.setExecutionCount(job.getExecutionCount());
                
                // Save updated job
                jobRepository.save(existingJob);
                log.info("Job status updated in database: {}", existingJob.getId());
            } else {
                log.warn("Job not found in database: {}", job.getId());
                // Save new job
                jobRepository.save(job);
                log.info("Created new job in database: {}", job.getId());
            }
        } catch (Exception e) {
            log.error("Error handling job status update", e);
        }
    }

    /**
     * Save job notification
     */
    private void saveNotification(JobNotification notification) {
        try {
            // Save notification to database
            notificationRepository.save(notification);
            log.info("Notification saved to database: {}", notification.getId());
        } catch (Exception e) {
            log.error("Error saving notification", e);
        }
    }
} 