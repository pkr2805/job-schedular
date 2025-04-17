package com.jobscheduler.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobscheduler.model.Job;
import com.jobscheduler.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final JobRepository jobRepository;
    
    private static final String TOPIC_JOB_NOTIFICATIONS = "job-notifications";

    /**
     * Send a job status update notification via Kafka
     */
    @Transactional
    public void sendJobStatusUpdate(Job job) {
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("jobId", job.getId().toString());
            notification.put("status", job.getStatus().toString());
            notification.put("name", job.getName());
            notification.put("jarFile", job.getJarFile());
            notification.put("timestamp", LocalDateTime.now().toString());
            notification.put("executionTime", job.getLastRunAt() != null ? job.getLastRunAt().toString() : null);
            notification.put("output", job.getOutput());
            notification.put("error", job.getError());
            
            String message = objectMapper.writeValueAsString(notification);
            
            // Update job with pending kafka message status
            job.setKafkaMessageStatus("PENDING");
            job.setKafkaMessageSent(LocalDateTime.now());
            job.setKafkaMessageResponse(message);
            jobRepository.save(job);
            
            // Send message to Kafka
            CompletableFuture<SendResult<String, String>> future = 
                kafkaTemplate.send(TOPIC_JOB_NOTIFICATIONS, job.getId().toString(), message);
            
            // Handle async response
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    // Success
                    job.setKafkaMessageId(result.getRecordMetadata().toString());
                    job.setKafkaMessageStatus("SUCCESS");
                    jobRepository.save(job);
                    log.info("Sent job status notification for job ID: {}, status: {}", job.getId(), job.getStatus());
                } else {
                    // Error
                    job.setKafkaMessageStatus("FAILED");
                    job.setError(job.getError() + "\nKafka Error: " + ex.getMessage());
                    jobRepository.save(job);
                    log.error("Error sending job notification: {}", ex.getMessage(), ex);
                }
            });
        } catch (JsonProcessingException e) {
            job.setKafkaMessageStatus("FAILED");
            job.setError(job.getError() + "\nSerialization Error: " + e.getMessage());
            jobRepository.save(job);
            log.error("Error serializing job notification: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Send a job error notification via Kafka
     */
    @Transactional
    public void sendJobErrorNotification(UUID jobId, String errorMessage) {
        Job job = jobRepository.findById(jobId).orElse(null);
        if (job == null) {
            log.error("Cannot send error notification for non-existent job with ID: {}", jobId);
            return;
        }
        
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("jobId", jobId.toString());
            notification.put("status", "FAILED");
            notification.put("timestamp", LocalDateTime.now().toString());
            notification.put("error", errorMessage);
            
            String message = objectMapper.writeValueAsString(notification);
            
            // Update job with pending kafka message status
            job.setKafkaMessageStatus("PENDING");
            job.setKafkaMessageSent(LocalDateTime.now());
            job.setKafkaMessageResponse(message);
            job.setError(errorMessage);
            jobRepository.save(job);
            
            // Send message to Kafka
            CompletableFuture<SendResult<String, String>> future = 
                kafkaTemplate.send(TOPIC_JOB_NOTIFICATIONS, jobId.toString(), message);
            
            // Handle async response
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    // Success
                    job.setKafkaMessageId(result.getRecordMetadata().toString());
                    job.setKafkaMessageStatus("SUCCESS");
                    jobRepository.save(job);
                    log.info("Sent job error notification for job ID: {}", jobId);
                } else {
                    // Error
                    job.setKafkaMessageStatus("FAILED");
                    job.setError(job.getError() + "\nKafka Error: " + ex.getMessage());
                    jobRepository.save(job);
                    log.error("Error sending job error notification: {}", ex.getMessage(), ex);
                }
            });
        } catch (JsonProcessingException e) {
            job.setKafkaMessageStatus("FAILED");
            job.setError(job.getError() + "\nSerialization Error: " + e.getMessage());
            jobRepository.save(job);
            log.error("Error serializing job error notification: {}", e.getMessage(), e);
        }
    }
} 