package com.jobscheduler.dto;

import com.jobscheduler.model.JobType;
import com.jobscheduler.model.JobFrequency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobRequest {
    
    @NotBlank(message = "Job name is required")
    private String name;
    
    @NotBlank(message = "JAR file is required")
    private String jarFile;
    
    private String description;
    
    @NotNull(message = "Job type is required")
    private JobType type;
    
    private LocalDateTime scheduledAt;
    
    private String cronExpression;
    
    private JobFrequency frequency;
    
    private int maxExecutions = 1;
    
    private String arguments;
} 