package com.jobscheduler.config;

import java.util.concurrent.Executor;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Application configuration class for the Job Scheduler.
 * Sets up beans for thread pool, web configuration, etc.
 */
@Configuration
@EnableAsync
public class AppConfig {
    
    /**
     * Configures CORS settings to allow connections from the frontend.
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("*")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .maxAge(3600);
            }
        };
    }
    
    /**
     * Creates a task executor for async job processing.
     */
    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(25);
        executor.setThreadNamePrefix("JobScheduler-");
        executor.initialize();
        return executor;
    }
    
    /**
     * Creates an executor for scheduled tasks.
     */
    @Bean
    public Executor scheduledTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(10);
        executor.setThreadNamePrefix("ScheduledTask-");
        executor.initialize();
        return executor;
    }
}

/**
 * Configuration properties for the job scheduler.
 */
@Configuration
@ConfigurationProperties(prefix = "job.scheduler")
class JobSchedulerProperties {
    private String jobDirectory;
    private ExecutorProperties executor;
    private int maxConcurrentJobs;
    private CleanupProperties cleanup;
    private RetryProperties retry;
    
    // Getters and setters
    public String getJobDirectory() {
        return jobDirectory;
    }
    
    public void setJobDirectory(String jobDirectory) {
        this.jobDirectory = jobDirectory;
    }
    
    public ExecutorProperties getExecutor() {
        return executor;
    }
    
    public void setExecutor(ExecutorProperties executor) {
        this.executor = executor;
    }
    
    public int getMaxConcurrentJobs() {
        return maxConcurrentJobs;
    }
    
    public void setMaxConcurrentJobs(int maxConcurrentJobs) {
        this.maxConcurrentJobs = maxConcurrentJobs;
    }
    
    public CleanupProperties getCleanup() {
        return cleanup;
    }
    
    public void setCleanup(CleanupProperties cleanup) {
        this.cleanup = cleanup;
    }
    
    public RetryProperties getRetry() {
        return retry;
    }
    
    public void setRetry(RetryProperties retry) {
        this.retry = retry;
    }
    
    static class ExecutorProperties {
        private int poolSize;
        
        public int getPoolSize() {
            return poolSize;
        }
        
        public void setPoolSize(int poolSize) {
            this.poolSize = poolSize;
        }
    }
    
    static class CleanupProperties {
        private boolean enabled;
        private int daysToKeep;
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        public int getDaysToKeep() {
            return daysToKeep;
        }
        
        public void setDaysToKeep(int daysToKeep) {
            this.daysToKeep = daysToKeep;
        }
    }
    
    static class RetryProperties {
        private int maxAttempts;
        private long delay;
        
        public int getMaxAttempts() {
            return maxAttempts;
        }
        
        public void setMaxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
        }
        
        public long getDelay() {
            return delay;
        }
        
        public void setDelay(long delay) {
            this.delay = delay;
        }
    }
} 