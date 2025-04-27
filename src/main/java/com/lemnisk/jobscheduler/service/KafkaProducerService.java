package com.lemnisk.jobscheduler.service;

import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import com.lemnisk.jobscheduler.dto.kafka.JobExecutionMessage;
import com.lemnisk.jobscheduler.dto.kafka.JobResultMessage;

@Service
public class KafkaProducerService {

    private static final Logger log = LoggerFactory.getLogger(KafkaProducerService.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topic.job-execution}")
    private String jobExecutionTopic;

    @Value("${kafka.topic.job-result}")
    private String jobResultTopic;

    public KafkaProducerService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Send job execution message to Kafka
     */
    public void sendJobExecutionMessage(JobExecutionMessage message) {
        log.info("Sending job execution message to Kafka: {}", message);
        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(jobExecutionTopic, message.getJobId().toString(), message);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Job execution message sent successfully: {}", message.getJobId());
            } else {
                log.error("Failed to send job execution message: {}", ex.getMessage(), ex);
            }
        });
    }

    /**
     * Send job result message to Kafka
     */
    public void sendJobResultMessage(JobResultMessage message) {
        log.info("Sending job result message to Kafka: {}", message);
        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(jobResultTopic, message.getJobId().toString(), message);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Job result message sent successfully: {}", message.getJobId());
            } else {
                log.error("Failed to send job result message: {}", ex.getMessage(), ex);
            }
        });
    }
}
