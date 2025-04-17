package com.jobscheduler.repository;

import com.jobscheduler.model.JobNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobNotificationRepository extends JpaRepository<JobNotification, String> {
    // Custom repository methods can be added here
} 