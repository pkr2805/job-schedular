package com.jobscheduler.controller;

import com.jobscheduler.dto.JobDTO;
import com.jobscheduler.dto.JobRequest;
import com.jobscheduler.mapper.JobMapper;
import com.jobscheduler.model.Job;
import com.jobscheduler.model.JobStatus;
import com.jobscheduler.model.JobType;
import com.jobscheduler.service.JobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/jobs")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class JobController {

    private final JobService jobService;
    private final JobMapper jobMapper;

    // Basic CRUD Operations
    
    @GetMapping
    public ResponseEntity<List<JobDTO>> getAllJobs() {
        try {
            List<Job> jobs = jobService.getAllJobs();
            List<JobDTO> jobDTOs = jobs.stream()
                    .map(JobDTO::fromEntity)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(jobDTOs);
        } catch (Exception e) {
            log.error("Error getting all jobs", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error getting jobs");
        }
    }
    
    @GetMapping("/paged")
    public ResponseEntity<Page<JobDTO>> getPagedJobs(Pageable pageable) {
        try {
            Page<Job> jobsPage = jobService.getAllJobs(pageable);
            List<JobDTO> jobDTOs = jobsPage.getContent().stream()
                    .map(JobDTO::fromEntity)
                    .collect(Collectors.toList());
            
            Page<JobDTO> jobDTOPage = new PageImpl<>(
                    jobDTOs,
                    pageable,
                    jobsPage.getTotalElements()
            );
            
            return ResponseEntity.ok(jobDTOPage);
        } catch (Exception e) {
            log.error("Error getting paged jobs", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error getting paged jobs");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobDTO> getJobById(@PathVariable UUID id) {
        try {
            Job job = jobService.getJobById(id);
            return ResponseEntity.ok(JobDTO.fromEntity(job));
        } catch (Exception e) {
            log.error("Error getting job: {}", id, e);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found with id: " + id);
        }
    }

    @PostMapping
    public ResponseEntity<JobDTO> createJob(@RequestBody JobRequest jobRequest) {
        log.info("Creating new job: {}", jobRequest);
        
        Job newJob = jobMapper.toEntity(jobRequest);
        Job savedJob = jobService.createJob(newJob);
        
        // Automatically execute immediate jobs
        if (savedJob.getType() == JobType.IMMEDIATE) {
            try {
                log.info("Auto-executing immediate job: {}", savedJob.getId());
                jobService.executeJob(savedJob.getId());
            } catch (Exception e) {
                log.error("Error auto-executing job: {}", savedJob.getId(), e);
                // Continue with job creation response even if execution fails
            }
        }
        
        return ResponseEntity.status(HttpStatus.CREATED).body(JobDTO.fromEntity(savedJob));
    }

    @PutMapping("/{id}")
    public ResponseEntity<JobDTO> updateJob(@PathVariable UUID id, @RequestBody JobDTO jobDTO) {
        try {
            Job job = jobDTO.toEntity();
            Job updatedJob = jobService.updateJob(id, job);
            return ResponseEntity.ok(JobDTO.fromEntity(updatedJob));
        } catch (Exception e) {
            log.error("Error updating job: {}", id, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error updating job: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJob(@PathVariable UUID id) {
        try {
            jobService.deleteJob(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting job: {}", id, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error deleting job: " + e.getMessage());
        }
    }
    
    // Job Lifecycle Management

    @PostMapping("/{id}/schedule")
    public ResponseEntity<JobDTO> scheduleJob(@PathVariable UUID id) {
        try {
            Job job = jobService.scheduleJob(id);
            return ResponseEntity.ok(JobDTO.fromEntity(job));
        } catch (Exception e) {
            log.error("Error scheduling job: {}", id, e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error scheduling job: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/execute")
    public ResponseEntity<JobDTO> executeJob(@PathVariable UUID id) {
        log.info("Executing job: {}", id);
        Job job = jobService.executeJob(id);
        return ResponseEntity.ok(JobDTO.fromEntity(job));
    }

    @PostMapping("/{id}/pause")
    public ResponseEntity<JobDTO> pauseJob(@PathVariable UUID id) {
        try {
            Job job = jobService.pauseJob(id);
            return ResponseEntity.ok(JobDTO.fromEntity(job));
        } catch (Exception e) {
            log.error("Error pausing job: {}", id, e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error pausing job: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/resume")
    public ResponseEntity<JobDTO> resumeJob(@PathVariable UUID id) {
        try {
            Job job = jobService.resumeJob(id);
            return ResponseEntity.ok(JobDTO.fromEntity(job));
        } catch (Exception e) {
            log.error("Error resuming job: {}", id, e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error resuming job: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<JobDTO> cancelJob(@PathVariable UUID id) {
        try {
            Job job = jobService.cancelJob(id);
            return ResponseEntity.ok(JobDTO.fromEntity(job));
        } catch (Exception e) {
            log.error("Error cancelling job: {}", id, e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error cancelling job: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/restart")
    public ResponseEntity<JobDTO> restartJob(@PathVariable UUID id) {
        try {
            Job job = jobService.restartJob(id);
            return ResponseEntity.ok(JobDTO.fromEntity(job));
        } catch (Exception e) {
            log.error("Error restarting job: {}", id, e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error restarting job: " + e.getMessage());
        }
    }
    
    // Filtering and Search

    @GetMapping("/status/{status}")
    public ResponseEntity<List<JobDTO>> getJobsByStatus(@PathVariable JobStatus status) {
        try {
            List<Job> jobs = jobService.getJobsByStatus(status);
            List<JobDTO> jobDTOs = jobs.stream()
                    .map(JobDTO::fromEntity)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(jobDTOs);
        } catch (Exception e) {
            log.error("Error getting jobs by status: {}", status, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error getting jobs by status");
        }
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<JobDTO>> getJobsByType(@PathVariable JobType type) {
        try {
            List<Job> jobs = jobService.getJobsByType(type);
            List<JobDTO> jobDTOs = jobs.stream()
                    .map(JobDTO::fromEntity)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(jobDTOs);
        } catch (Exception e) {
            log.error("Error getting jobs by type: {}", type, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error getting jobs by type");
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<JobDTO>> searchJobs(@RequestParam String keyword) {
        List<Job> jobs = jobService.searchJobs(keyword);
        List<JobDTO> jobDTOs = jobs.stream()
                .map(JobDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(jobDTOs);
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<JobDTO>> getJobsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        
        List<Job> jobs = jobService.getJobsCreatedBetween(start, end);
        List<JobDTO> jobDTOs = jobs.stream()
                .map(JobDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(jobDTOs);
    }
    
    // Job Output Management

    @GetMapping("/{id}/output")
    public ResponseEntity<String> getJobOutput(@PathVariable UUID id) {
        try {
            String output = jobService.getJobOutput(id);
            if (output == null || output.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(output);
        } catch (Exception e) {
            log.error("Error getting job output: {}", id, e);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found with id: " + id);
        }
    }

    @GetMapping("/{id}/error")
    public ResponseEntity<String> getJobError(@PathVariable UUID id) {
        try {
            String error = jobService.getJobError(id);
            if (error == null || error.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(error);
        } catch (Exception e) {
            log.error("Error getting job error: {}", id, e);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found with id: " + id);
        }
    }
    
    // Get Kafka message details
    @GetMapping("/{id}/kafka")
    public ResponseEntity<Object> getJobKafkaDetails(@PathVariable UUID id) {
        try {
            Job job = jobService.getJobById(id);
            if (job.getKafkaMessageResponse() == null || job.getKafkaMessageResponse().isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            
            return ResponseEntity.ok(Map.of(
                "messageId", job.getKafkaMessageId() != null ? job.getKafkaMessageId() : "",
                "sentAt", job.getKafkaMessageSent() != null ? job.getKafkaMessageSent().toString() : "",
                "status", job.getKafkaMessageStatus() != null ? job.getKafkaMessageStatus() : "NOT_SENT",
                "response", job.getKafkaMessageResponse()
            ));
        } catch (Exception e) {
            log.error("Error getting job Kafka details: {}", id, e);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found with id: " + id);
        }
    }
    
    // Statistics

    @GetMapping("/count/status/{status}")
    public ResponseEntity<Long> countJobsByStatus(@PathVariable String status) {
        try {
            JobStatus jobStatus = JobStatus.valueOf(status.toUpperCase());
            return ResponseEntity.ok(jobService.countJobsByStatus(jobStatus));
        } catch (IllegalArgumentException e) {
            log.error("Invalid job status: {}", status, e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid job status: " + status);
        }
    }

    @GetMapping("/count/type/{type}")
    public ResponseEntity<Long> countJobsByType(@PathVariable String type) {
        try {
            JobType jobType = JobType.valueOf(type.toUpperCase());
            return ResponseEntity.ok(jobService.countJobsByType(jobType));
        } catch (IllegalArgumentException e) {
            log.error("Invalid job type: {}", type, e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid job type: " + type);
        }
    }
} 