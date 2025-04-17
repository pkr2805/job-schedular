package com.jobscheduler.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "job_notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobNotification {

    @Id
    private String id;
    
    private String title;
    
    private String message;
    
    private String type; // SUCCESS, ERROR, WARNING, INFO
    
    private String timestamp;
    
    private boolean read;
    
    private String jobId;
} 