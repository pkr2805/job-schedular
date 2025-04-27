package com.lemnisk.jobscheduler.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lemnisk.jobscheduler.dto.kafka.JobExecutionMessage;
import com.lemnisk.jobscheduler.dto.kafka.JobResultMessage;
import com.lemnisk.jobscheduler.model.JobExecution;
import com.lemnisk.jobscheduler.model.JobSchedule;

@Service
public class KafkaConsumerService {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerService.class);

    private final JobScheduleService jobScheduleService;
    private final JobExecutionService jobExecutionService;
    private final KafkaProducerService kafkaProducerService;
    private final JarExecutorService jarExecutorService;
    private final ObjectMapper objectMapper;

    private final ExecutorService executorService = Executors.newFixedThreadPool(5);

    public KafkaConsumerService(JobScheduleService jobScheduleService, JobExecutionService jobExecutionService,
            KafkaProducerService kafkaProducerService, JarExecutorService jarExecutorService, ObjectMapper objectMapper) {
        this.jobScheduleService = jobScheduleService;
        this.jobExecutionService = jobExecutionService;
        this.kafkaProducerService = kafkaProducerService;
        this.jarExecutorService = jarExecutorService;
        this.objectMapper = objectMapper;
    }

    /**
     * Listen for job execution messages
     */
    @KafkaListener(topics = "${kafka.topic.job-execution}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeJobExecutionMessage(JobExecutionMessage message, Acknowledgment acknowledgment) {
        log.info("Received job execution message: {}", message);

        try {
            // Process the job execution in a separate thread
            executorService.submit(() -> processJobExecution(message));

            // Acknowledge the message
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error processing job execution message: {}", e.getMessage(), e);
        }
    }

    /**
     * Listen for job result messages
     */
    @KafkaListener(topics = "${kafka.topic.job-result}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeJobResultMessage(JobResultMessage message, Acknowledgment acknowledgment) {
        log.info("Received job result message: {}", message);

        try {
            // Update job execution with result
            updateJobExecutionWithResult(message);

            // Acknowledge the message
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error processing job result message: {}", e.getMessage(), e);
        }
    }

    /**
     * Process job execution
     */
    private void processJobExecution(JobExecutionMessage message) {
        try {
            // Find the job schedule
            JobSchedule jobSchedule = jobScheduleService.getJobScheduleById(message.getJobId());

            if (jobSchedule == null) {
                log.error("Job schedule not found: {}", message.getJobId());
                return;
            }

            // Update job status to RUNNING
            jobScheduleService.updateJobStatus(jobSchedule.getId(), JobSchedule.JobStatus.RUNNING);

            // Create job execution record
            JobExecution jobExecution = JobExecution.builder()
                    .jobSchedule(jobSchedule)
                    .startTime(LocalDateTime.now())
                    .status(JobExecution.ExecutionStatus.STARTED)
                    .logs("Starting job execution...")
                    .build();

            jobExecution = jobExecutionService.saveJobExecution(jobExecution);

            // Simulate job execution
            simulateJobExecution(jobSchedule, jobExecution);

        } catch (Exception e) {
            log.error("Error processing job execution: {}", e.getMessage(), e);

            // Send failure result
            JobResultMessage resultMessage = JobResultMessage.builder()
                    .jobId(message.getJobId())
                    .status("failure")
                    .timestamp(LocalDateTime.now())
                    .error(e.getMessage())
                    .logs("Error executing job: " + e.getMessage())
                    .build();

            kafkaProducerService.sendJobResultMessage(resultMessage);
        }
    }

    /**
     * Execute JAR file
     */
    private void simulateJobExecution(JobSchedule jobSchedule, JobExecution jobExecution) {
        try {
            // Prepare any arguments for the JAR file (could be extended in the future)
            List<String> arguments = new ArrayList<>();

            // Execute the JAR file
            JarExecutorService.ExecutionResult result = jarExecutorService.executeJar(jobSchedule.getJarFile(), arguments);

            LocalDateTime endTime = LocalDateTime.now();
            String executionTimeFormatted = result.getExecutionTimeFormatted();

            // Create result message
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("executionTime", executionTimeFormatted);
            metadata.put("jarName", jobSchedule.getJarFile().getName());

            if (result.isSuccess()) {
                // Update job execution
                jobExecution.setEndTime(endTime);
                jobExecution.setStatus(JobExecution.ExecutionStatus.COMPLETED);
                jobExecution.setLogs(result.getOutput());
                jobExecution.setExecutionTime(executionTimeFormatted);
                jobExecutionService.saveJobExecution(jobExecution);

                // Update job schedule status
                jobScheduleService.updateJobStatus(jobSchedule.getId(), JobSchedule.JobStatus.COMPLETED);

                // Send success result
                JobResultMessage resultMessage = JobResultMessage.builder()
                        .jobId(jobSchedule.getId())
                        .status("success")
                        .timestamp(endTime)
                        .logs(result.getOutput())
                        .executionTime(executionTimeFormatted)
                        .metadata(metadata)
                        .build();

                kafkaProducerService.sendJobResultMessage(resultMessage);

                // If job is recurring, schedule the next execution
                if (jobSchedule.getRecurrenceType() != null &&
                        jobSchedule.getRecurrenceType() != JobSchedule.RecurrenceType.ONE_TIME) {
                    scheduleNextExecution(jobSchedule);
                }
            } else {
                // Update job execution
                jobExecution.setEndTime(endTime);
                jobExecution.setStatus(JobExecution.ExecutionStatus.FAILED);
                jobExecution.setLogs(result.getOutput());
                jobExecution.setErrorMessage(result.getMessage());
                jobExecution.setExecutionTime(executionTimeFormatted);
                jobExecutionService.saveJobExecution(jobExecution);

                // Update job schedule status
                jobScheduleService.updateJobStatus(jobSchedule.getId(), JobSchedule.JobStatus.FAILED);

                // Send failure result
                JobResultMessage resultMessage = JobResultMessage.builder()
                        .jobId(jobSchedule.getId())
                        .status("failure")
                        .timestamp(endTime)
                        .error(result.getMessage())
                        .logs(result.getOutput())
                        .executionTime(executionTimeFormatted)
                        .metadata(metadata)
                        .build();

                kafkaProducerService.sendJobResultMessage(resultMessage);
            }
        } catch (Exception e) {
            log.error("Error simulating job execution: {}", e.getMessage(), e);

            // Update job execution
            jobExecution.setEndTime(LocalDateTime.now());
            jobExecution.setStatus(JobExecution.ExecutionStatus.FAILED);
            jobExecution.setLogs(jobExecution.getLogs() + "\\nError: " + e.getMessage());
            jobExecution.setErrorMessage(e.getMessage());
            jobExecutionService.saveJobExecution(jobExecution);

            // Update job schedule status
            jobScheduleService.updateJobStatus(jobSchedule.getId(), JobSchedule.JobStatus.FAILED);

            // Send failure result
            JobResultMessage resultMessage = JobResultMessage.builder()
                    .jobId(jobSchedule.getId())
                    .status("failure")
                    .timestamp(LocalDateTime.now())
                    .error(e.getMessage())
                    .logs(jobExecution.getLogs())
                    .build();

            kafkaProducerService.sendJobResultMessage(resultMessage);
        }
    }

    /**
     * Schedule next execution for recurring jobs
     */
    private void scheduleNextExecution(JobSchedule completedJob) {
        try {
            LocalDateTime lastScheduledTime = completedJob.getScheduledTime();
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime nextExecutionTime = calculateNextExecutionTime(completedJob);

            log.info("===== SCHEDULING NEXT RECURRING JOB =====");
            log.info("Job ID: {}", completedJob.getId());
            log.info("JAR file: {}", completedJob.getJarFile().getName());
            log.info("Recurrence type: {}", completedJob.getRecurrenceType());
            log.info("Previous scheduled time: {}", lastScheduledTime);
            log.info("Current time: {}", now);
            log.info("Next execution time: {}", nextExecutionTime);
            log.info("Time difference: {} minutes", ChronoUnit.MINUTES.between(lastScheduledTime, nextExecutionTime));

            // Validate that the next execution time is in the future
            if (nextExecutionTime.isBefore(now)) {
                log.error("Calculated next execution time is in the past! Adjusting to future time.");
                // Add one hour to current time as a fallback
                nextExecutionTime = now.plus(1, ChronoUnit.HOURS);
                log.info("Adjusted next execution time: {}", nextExecutionTime);
            }

            // Create a new job schedule for the next execution
            JobSchedule nextJob = JobSchedule.builder()
                    .jarFile(completedJob.getJarFile())
                    .executionType(JobSchedule.ExecutionType.SCHEDULED)
                    .scheduledTime(nextExecutionTime)
                    .recurrenceType(completedJob.getRecurrenceType())
                    .status(JobSchedule.JobStatus.SCHEDULED)
                    .build();

            JobSchedule savedJob = jobScheduleService.createJobSchedule(nextJob);
            log.info("Created new job schedule with ID: {}", savedJob.getId());
            log.info("Next execution scheduled successfully");
            log.info("=========================================");
        } catch (Exception e) {
            log.error("Error scheduling next execution: {}", e.getMessage(), e);
        }
    }

    /**
     * Calculate next execution time based on recurrence type
     */
    private LocalDateTime calculateNextExecutionTime(JobSchedule job) {
        LocalDateTime lastScheduledTime = job.getScheduledTime();
        LocalDateTime now = LocalDateTime.now();

        // Extract the original minute and second components to maintain the same time pattern
        int originalMinute = lastScheduledTime.getMinute();
        int originalSecond = lastScheduledTime.getSecond();
        int originalNano = lastScheduledTime.getNano();

        switch (job.getRecurrenceType()) {
            case HOURLY:
                // First try to calculate the next hour precisely from the original scheduled time
                LocalDateTime nextHour = lastScheduledTime.plus(1, ChronoUnit.HOURS);

                // If the calculated next hour is in the past (due to delays),
                // calculate the next hour that maintains the same minute/second pattern
                if (nextHour.isBefore(now)) {
                    // Get the current time and add hours until we're in the future
                    LocalDateTime baseTime = now.withMinute(originalMinute)
                                               .withSecond(originalSecond)
                                               .withNano(originalNano);

                    // If the adjusted time is still in the past, add one hour
                    if (baseTime.isBefore(now)) {
                        baseTime = baseTime.plus(1, ChronoUnit.HOURS);
                    }

                    log.info("Adjusted hourly schedule from {} to {} to maintain exact hour intervals",
                             nextHour, baseTime);
                    return baseTime;
                }

                return nextHour;
            case DAILY:
                // Similar logic for daily recurrence
                LocalDateTime nextDay = lastScheduledTime.plus(1, ChronoUnit.DAYS);
                if (nextDay.isBefore(now)) {
                    // Maintain the same hour, minute, second pattern
                    LocalDateTime baseTime = now.withHour(lastScheduledTime.getHour())
                                               .withMinute(originalMinute)
                                               .withSecond(originalSecond)
                                               .withNano(originalNano);

                    // If the adjusted time is still in the past, add one day
                    if (baseTime.isBefore(now)) {
                        baseTime = baseTime.plus(1, ChronoUnit.DAYS);
                    }

                    return baseTime;
                }
                return nextDay;
            case WEEKLY:
                // Similar logic for weekly recurrence
                LocalDateTime nextWeek = lastScheduledTime.plus(1, ChronoUnit.WEEKS);
                if (nextWeek.isBefore(now)) {
                    // Calculate how many weeks we need to add to get to the future
                    long weeksToAdd = ChronoUnit.WEEKS.between(lastScheduledTime, now) + 1;
                    return lastScheduledTime.plus(weeksToAdd, ChronoUnit.WEEKS);
                }
                return nextWeek;
            default:
                return lastScheduledTime;
        }
    }

    /**
     * Update job execution with result
     */
    private void updateJobExecutionWithResult(JobResultMessage message) {
        try {
            // Find the job schedule
            JobSchedule jobSchedule = jobScheduleService.getJobScheduleById(message.getJobId());

            if (jobSchedule == null) {
                log.error("Job schedule not found: {}", message.getJobId());
                return;
            }

            // Find the latest job execution
            JobExecution jobExecution = jobExecutionService.getLatestJobExecution(jobSchedule.getId());

            if (jobExecution == null) {
                log.error("Job execution not found for job: {}", message.getJobId());
                return;
            }

            // Update job execution
            jobExecution.setEndTime(message.getTimestamp());
            jobExecution.setStatus("success".equals(message.getStatus())
                    ? JobExecution.ExecutionStatus.COMPLETED
                    : JobExecution.ExecutionStatus.FAILED);
            jobExecution.setLogs(message.getLogs());
            jobExecution.setErrorMessage(message.getError());
            jobExecution.setExecutionTime(message.getExecutionTime());
            jobExecution.setResponse(objectMapper.writeValueAsString(message));

            jobExecutionService.saveJobExecution(jobExecution);

            // Update job schedule status
            JobSchedule.JobStatus newStatus = "success".equals(message.getStatus())
                    ? JobSchedule.JobStatus.COMPLETED
                    : JobSchedule.JobStatus.FAILED;

            jobScheduleService.updateJobStatus(jobSchedule.getId(), newStatus);

        } catch (Exception e) {
            log.error("Error updating job execution with result: {}", e.getMessage(), e);
        }
    }
}
