package com.lemnisk.jobscheduler.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lemnisk.jobscheduler.dto.JobExecutionDTO;
import com.lemnisk.jobscheduler.service.JobExecutionService;

@RestController
@RequestMapping("/job-executions")
@CrossOrigin(origins = "${cors.allowed-origins}", allowedHeaders = "*")
public class JobExecutionController {

    private final JobExecutionService jobExecutionService;

    public JobExecutionController(JobExecutionService jobExecutionService) {
        this.jobExecutionService = jobExecutionService;
    }

    /**
     * Get job executions by job schedule ID
     */
    @GetMapping("/job-schedule/{jobScheduleId}")
    public ResponseEntity<List<JobExecutionDTO>> getJobExecutionsByJobScheduleId(@PathVariable UUID jobScheduleId) {
        return ResponseEntity.ok(jobExecutionService.getJobExecutionsByJobScheduleId(jobScheduleId));
    }

    /**
     * Get job execution by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<JobExecutionDTO> getJobExecutionById(@PathVariable UUID id) {
        JobExecutionDTO jobExecutionDTO = jobExecutionService.getJobExecutionById(id);

        if (jobExecutionDTO == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(jobExecutionDTO);
    }
}
