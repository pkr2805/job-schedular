package com.lemnisk.jobscheduler.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lemnisk.jobscheduler.dto.JobScheduleDTO;
import com.lemnisk.jobscheduler.dto.JobScheduleRequest;
import com.lemnisk.jobscheduler.dto.kafka.JobExecutionMessage;
import com.lemnisk.jobscheduler.model.JarFile;
import com.lemnisk.jobscheduler.model.JobSchedule;
import com.lemnisk.jobscheduler.repository.JarFileRepository;
import com.lemnisk.jobscheduler.repository.JobScheduleRepository;

@Service
public class JobScheduleService {

    private static final Logger log = LoggerFactory.getLogger(JobScheduleService.class);

    private final JobScheduleRepository jobScheduleRepository;
    private final JarFileRepository jarFileRepository;
    private final KafkaProducerService kafkaProducerService;

    public JobScheduleService(JobScheduleRepository jobScheduleRepository, JarFileRepository jarFileRepository,
            KafkaProducerService kafkaProducerService) {
        this.jobScheduleRepository = jobScheduleRepository;
        this.jarFileRepository = jarFileRepository;
        this.kafkaProducerService = kafkaProducerService;
    }

    /**
     * Create a new job schedule
     */
    @Transactional
    public JobScheduleDTO createJobSchedule(JobScheduleRequest request) {
        // Find the JAR file
        Optional<JarFile> jarFileOptional = jarFileRepository.findById(request.getJarFileId());

        if (jarFileOptional.isEmpty()) {
            throw new IllegalArgumentException("JAR file not found: " + request.getJarFileId());
        }

        JarFile jarFile = jarFileOptional.get();

        // Create job schedule
        JobSchedule jobSchedule = JobSchedule.builder()
                .jarFile(jarFile)
                .executionType(JobSchedule.ExecutionType.valueOf(request.getExecutionType().toUpperCase()))
                .scheduledTime(request.getExecutionType().equalsIgnoreCase("immediate")
                        ? LocalDateTime.now()
                        : request.getScheduledTime())
                .recurrenceType(request.getRecurrenceType() != null
                        ? JobSchedule.RecurrenceType.valueOf(request.getRecurrenceType().toUpperCase())
                        : JobSchedule.RecurrenceType.ONE_TIME)
                .status(JobSchedule.JobStatus.SCHEDULED)
                .build();

        jobSchedule = jobScheduleRepository.save(jobSchedule);

        // If immediate execution, send to Kafka
        if (jobSchedule.getExecutionType() == JobSchedule.ExecutionType.IMMEDIATE) {
            sendJobExecutionMessage(jobSchedule);
        }

        return convertToDTO(jobSchedule);
    }

    /**
     * Create a job schedule (internal use)
     */
    @Transactional
    public JobSchedule createJobSchedule(JobSchedule jobSchedule) {
        return jobScheduleRepository.save(jobSchedule);
    }

    /**
     * Get all job schedules
     */
    public List<JobScheduleDTO> getAllJobSchedules() {
        List<JobSchedule> jobSchedules = jobScheduleRepository.findAll();
        List<JobScheduleDTO> jobScheduleDTOs = new ArrayList<>();

        for (JobSchedule jobSchedule : jobSchedules) {
            jobScheduleDTOs.add(convertToDTO(jobSchedule));
        }

        return jobScheduleDTOs;
    }

    /**
     * Get job schedule by ID
     */
    public JobSchedule getJobScheduleById(UUID id) {
        Optional<JobSchedule> jobScheduleOptional = jobScheduleRepository.findById(id);
        return jobScheduleOptional.orElse(null);
    }

    /**
     * Get job schedule DTO by ID
     */
    public JobScheduleDTO getJobScheduleDTOById(UUID id) {
        Optional<JobSchedule> jobScheduleOptional = jobScheduleRepository.findById(id);
        return jobScheduleOptional.map(this::convertToDTO).orElse(null);
    }

    /**
     * Update job status
     */
    @Transactional
    public void updateJobStatus(UUID id, JobSchedule.JobStatus status) {
        Optional<JobSchedule> jobScheduleOptional = jobScheduleRepository.findById(id);

        if (jobScheduleOptional.isPresent()) {
            JobSchedule jobSchedule = jobScheduleOptional.get();
            jobSchedule.setStatus(status);
            jobScheduleRepository.save(jobSchedule);
        }
    }

    /**
     * Cancel job
     */
    @Transactional
    public boolean cancelJob(UUID id) {
        Optional<JobSchedule> jobScheduleOptional = jobScheduleRepository.findById(id);

        if (jobScheduleOptional.isPresent()) {
            JobSchedule jobSchedule = jobScheduleOptional.get();

            // Only cancel if job is scheduled or running
            if (jobSchedule.getStatus() == JobSchedule.JobStatus.SCHEDULED ||
                    jobSchedule.getStatus() == JobSchedule.JobStatus.RUNNING) {
                jobSchedule.setStatus(JobSchedule.JobStatus.CANCELLED);
                jobScheduleRepository.save(jobSchedule);
                return true;
            }
        }

        return false;
    }

    /**
     * Check for scheduled jobs that are due for execution
     */
    @Scheduled(fixedRate = 30000) // Run every 30 seconds for more precise scheduling
    @Transactional
    public void checkScheduledJobs() {
        LocalDateTime now = LocalDateTime.now();
        log.info("Checking for scheduled jobs due for execution at {}", now);

        try {
            List<JobSchedule> dueJobs = jobScheduleRepository.findJobsDueForExecution(now, JobSchedule.JobStatus.SCHEDULED);
            log.info("Found {} jobs due for execution", dueJobs.size());

            for (JobSchedule job : dueJobs) {
                log.info("Processing due job: ID={}, JAR={}, ScheduledTime={}, RecurrenceType={}",
                        job.getId(), job.getJarFile().getName(), job.getScheduledTime(), job.getRecurrenceType());

                // Calculate how many minutes past the scheduled time
                long minutesLate = java.time.temporal.ChronoUnit.MINUTES.between(job.getScheduledTime(), now);
                if (minutesLate > 1) {
                    log.warn("Job is {} minutes past its scheduled execution time", minutesLate);
                }

                sendJobExecutionMessage(job);
                log.info("Sent execution message for job: {}", job.getId());
            }
        } catch (Exception e) {
            log.error("Error checking for scheduled jobs: {}", e.getMessage(), e);
        }
    }

    /**
     * Send job execution message to Kafka
     */
    private void sendJobExecutionMessage(JobSchedule jobSchedule) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("jarName", jobSchedule.getJarFile().getName());

        JobExecutionMessage message = JobExecutionMessage.builder()
                .jobId(jobSchedule.getId())
                .jarName(jobSchedule.getJarFile().getName())
                .executionType(jobSchedule.getExecutionType().toString())
                .scheduledTime(jobSchedule.getScheduledTime())
                .recurrenceType(jobSchedule.getRecurrenceType().toString())
                .metadata(metadata)
                .build();

        kafkaProducerService.sendJobExecutionMessage(message);
    }

    /**
     * Convert JobSchedule to JobScheduleDTO
     */
    private JobScheduleDTO convertToDTO(JobSchedule jobSchedule) {
        return JobScheduleDTO.builder()
                .id(jobSchedule.getId())
                .jarFileId(jobSchedule.getJarFile().getId())
                .jarName(jobSchedule.getJarFile().getName())
                .executionType(jobSchedule.getExecutionType().toString())
                .scheduledTime(jobSchedule.getScheduledTime())
                .recurrenceType(jobSchedule.getRecurrenceType().toString())
                .status(jobSchedule.getStatus().toString())
                .createdAt(jobSchedule.getCreatedAt())
                .updatedAt(jobSchedule.getUpdatedAt())
                .build();
    }
}
