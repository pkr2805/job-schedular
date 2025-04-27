package com.lemnisk.jobscheduler.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.lemnisk.jobscheduler.dto.JobScheduleDTO;
import com.lemnisk.jobscheduler.dto.JobScheduleRequest;
import com.lemnisk.jobscheduler.service.JobScheduleService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/job-schedules")
@CrossOrigin(origins = "${cors.allowed-origins}", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class JobScheduleController {

    private final JobScheduleService jobScheduleService;

    public JobScheduleController(JobScheduleService jobScheduleService) {
        this.jobScheduleService = jobScheduleService;
    }

    /**
     * Create a new job schedule
     */
    @PostMapping
    public ResponseEntity<JobScheduleDTO> createJobSchedule(@Valid @RequestBody JobScheduleRequest request) {
        return ResponseEntity.ok(jobScheduleService.createJobSchedule(request));
    }

    /**
     * Get all job schedules
     */
    @GetMapping
    public ResponseEntity<List<JobScheduleDTO>> getAllJobSchedules() {
        return ResponseEntity.ok(jobScheduleService.getAllJobSchedules());
    }

    /**
     * Get job schedule by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<JobScheduleDTO> getJobScheduleById(@PathVariable UUID id) {
        JobScheduleDTO jobScheduleDTO = jobScheduleService.getJobScheduleDTOById(id);

        if (jobScheduleDTO == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(jobScheduleDTO);
    }

    /**
     * Cancel job
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelJob(@PathVariable UUID id) {
        boolean cancelled = jobScheduleService.cancelJob(id);

        if (!cancelled) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok().build();
    }
}
