package com.lemnisk.jobscheduler.repository;

import com.lemnisk.jobscheduler.model.JobExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JobExecutionRepository extends JpaRepository<JobExecution, UUID> {
    
    List<JobExecution> findByJobScheduleId(UUID jobScheduleId);
    
    List<JobExecution> findByJobScheduleIdOrderByStartTimeDesc(UUID jobScheduleId);
}
