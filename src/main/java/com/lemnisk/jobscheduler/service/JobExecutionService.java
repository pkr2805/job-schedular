package com.lemnisk.jobscheduler.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lemnisk.jobscheduler.dto.JobExecutionDTO;
import com.lemnisk.jobscheduler.model.JobExecution;
import com.lemnisk.jobscheduler.repository.JobExecutionRepository;

@Service
public class JobExecutionService {

    private static final Logger log = LoggerFactory.getLogger(JobExecutionService.class);

    private final JobExecutionRepository jobExecutionRepository;

    public JobExecutionService(JobExecutionRepository jobExecutionRepository) {
        this.jobExecutionRepository = jobExecutionRepository;
    }

    /**
     * Save job execution
     */
    @Transactional
    public JobExecution saveJobExecution(JobExecution jobExecution) {
        return jobExecutionRepository.save(jobExecution);
    }

    /**
     * Get job executions by job schedule ID
     */
    public List<JobExecutionDTO> getJobExecutionsByJobScheduleId(UUID jobScheduleId) {
        List<JobExecution> jobExecutions = jobExecutionRepository.findByJobScheduleIdOrderByStartTimeDesc(jobScheduleId);
        List<JobExecutionDTO> jobExecutionDTOs = new ArrayList<>();

        for (JobExecution jobExecution : jobExecutions) {
            jobExecutionDTOs.add(convertToDTO(jobExecution));
        }

        return jobExecutionDTOs;
    }

    /**
     * Get job execution by ID
     */
    public JobExecutionDTO getJobExecutionById(UUID id) {
        Optional<JobExecution> jobExecutionOptional = jobExecutionRepository.findById(id);
        return jobExecutionOptional.map(this::convertToDTO).orElse(null);
    }

    /**
     * Get latest job execution for a job schedule
     */
    public JobExecution getLatestJobExecution(UUID jobScheduleId) {
        List<JobExecution> jobExecutions = jobExecutionRepository.findByJobScheduleIdOrderByStartTimeDesc(jobScheduleId);
        return jobExecutions.isEmpty() ? null : jobExecutions.get(0);
    }

    /**
     * Convert JobExecution to JobExecutionDTO
     */
    private JobExecutionDTO convertToDTO(JobExecution jobExecution) {
        return JobExecutionDTO.builder()
                .id(jobExecution.getId())
                .jobScheduleId(jobExecution.getJobSchedule().getId())
                .startTime(jobExecution.getStartTime())
                .endTime(jobExecution.getEndTime())
                .status(jobExecution.getStatus().toString())
                .logs(jobExecution.getLogs())
                .errorMessage(jobExecution.getErrorMessage())
                .executionTime(jobExecution.getExecutionTime())
                .response(jobExecution.getResponse())
                .build();
    }
}
