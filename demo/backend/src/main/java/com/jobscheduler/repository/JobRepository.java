package com.jobscheduler.repository;

import com.jobscheduler.model.Job;
import com.jobscheduler.model.JobStatus;
import com.jobscheduler.model.JobType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface JobRepository extends JpaRepository<Job, UUID> {

    // Basic finders
    List<Job> findByStatus(JobStatus status);
    
    List<Job> findByType(JobType type);
    
    List<Job> findByTypeAndStatus(JobType type, JobStatus status);

    // Find jobs scheduled to run
    @Query("SELECT j FROM Job j WHERE j.nextRunAt <= :now AND j.status = :status ORDER BY j.priority DESC")
    List<Job> findJobsDueForExecution(@Param("now") LocalDateTime now, @Param("status") JobStatus status);
    
    // Find recurring jobs
    List<Job> findByTypeAndCronExpressionIsNotNull(JobType type);
    
    // Find recent jobs
    @Query("SELECT j FROM Job j WHERE j.status = :status ORDER BY j.createdAt DESC")
    List<Job> findRecentJobs(@Param("status") JobStatus status, Pageable pageable);
    
    // Count jobs by status
    long countByStatus(JobStatus status);
    
    // Count jobs by type
    long countByType(JobType type);
    
    // Find jobs by name (partial match)
    List<Job> findByNameContainingIgnoreCase(String name);
    
    // Update job status
    @Modifying
    @Query("UPDATE Job j SET j.status = :status, j.updatedAt = CURRENT_TIMESTAMP WHERE j.id = :id")
    int updateJobStatus(@Param("id") UUID id, @Param("status") JobStatus status);
    
    // Update job next run time
    @Modifying
    @Query("UPDATE Job j SET j.nextRunAt = :nextRunAt, j.updatedAt = CURRENT_TIMESTAMP WHERE j.id = :id")
    int updateNextRunTime(@Param("id") UUID id, @Param("nextRunAt") LocalDateTime nextRunAt);
    
    // Update execution count
    @Modifying
    @Query("UPDATE Job j SET j.executionCount = j.executionCount + 1, j.lastRunAt = CURRENT_TIMESTAMP, j.updatedAt = CURRENT_TIMESTAMP WHERE j.id = :id")
    int incrementExecutionCount(@Param("id") UUID id);
    
    // Find jobs to clean up (completed/failed jobs older than a certain time)
    @Query("SELECT j FROM Job j WHERE (j.status = 'COMPLETED' OR j.status = 'FAILED') AND j.updatedAt < :cutoffDate")
    List<Job> findJobsForCleanup(@Param("cutoffDate") LocalDateTime cutoffDate);

    // Find jobs by creation date and statuses
    List<Job> findByCreatedAtBeforeAndStatusIn(LocalDateTime createdBefore, List<JobStatus> statuses);

    List<Job> findAllByOrderByCreatedAtDesc();
    
    Page<Job> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    List<Job> findByJarFile(String jarFile);
} 