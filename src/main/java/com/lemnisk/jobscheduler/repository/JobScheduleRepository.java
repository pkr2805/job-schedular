package com.lemnisk.jobscheduler.repository;

import com.lemnisk.jobscheduler.model.JobSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface JobScheduleRepository extends JpaRepository<JobSchedule, UUID> {
    
    List<JobSchedule> findByStatus(JobSchedule.JobStatus status);
    
    @Query("SELECT j FROM JobSchedule j WHERE j.scheduledTime <= ?1 AND j.status = ?2")
    List<JobSchedule> findJobsDueForExecution(LocalDateTime now, JobSchedule.JobStatus status);
    
    List<JobSchedule> findByJarFileId(UUID jarFileId);
}
