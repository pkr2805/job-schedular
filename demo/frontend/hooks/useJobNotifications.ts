import { useState, useEffect, useCallback } from 'react';
import { useToast } from '@/components/ui/use-toast';

interface JobStatusEvent {
  jobId: string;
  status: string;
  name?: string;
  jarFile?: string;
  timestamp: string;
  executionTime?: string;
  output?: string;
  error?: string;
}

// Simulate job updates that would normally come from Kafka
const mockJobStatusEvents: JobStatusEvent[] = [
  {
    jobId: 'kafka-job-1',
    status: 'RUNNING',
    name: 'Kafka Stream Processing',
    timestamp: new Date().toISOString(),
  },
  {
    jobId: 'kafka-job-2',
    status: 'COMPLETED',
    name: 'Data Pipeline ETL',
    timestamp: new Date().toISOString(),
    output: "Successfully processed 1500 records"
  },
  {
    jobId: 'kafka-job-3',
    status: 'FAILED',
    name: 'Log Aggregation Task',
    timestamp: new Date().toISOString(),
    error: "Connection timeout after 30s"
  },
  {
    jobId: 'kafka-job-4',
    status: 'SCHEDULED',
    name: 'Weekly Database Backup',
    timestamp: new Date().toISOString(),
  }
];

export default function useJobNotifications() {
  const [connected, setConnected] = useState(true); // Assume always connected
  const { toast } = useToast();
  
  // Function to generate a toast notification based on job status
  const showJobStatusToast = useCallback((jobStatus: JobStatusEvent) => {
    const isSuccess = jobStatus.status.toLowerCase() === 'completed';
    const isFailed = jobStatus.status.toLowerCase() === 'failed';
    const isScheduled = jobStatus.status.toLowerCase() === 'scheduled';
    const isRunning = jobStatus.status.toLowerCase() === 'running';
    
    if (isRunning) {
      toast({
        title: 'Job Started',
        description: `Job ${jobStatus.name || jobStatus.jobId} is now running`,
        variant: 'default',
      });
    } else if (isSuccess || isFailed) {
      toast({
        title: isSuccess ? 'Job Completed Successfully' : 'Job Execution Failed',
        description: `Job ${jobStatus.name || jobStatus.jobId} ${isSuccess ? 'completed successfully' : 'failed'} at ${new Date(jobStatus.timestamp).toLocaleString()}`,
        variant: isSuccess ? 'default' : 'destructive',
      });
    } else if (isScheduled) {
      toast({
        title: 'Job Scheduled',
        description: `Job ${jobStatus.name || jobStatus.jobId} has been scheduled`,
        variant: 'default',
      });
    }
  }, [toast]);
  
  // Set up the simulated Kafka polling
  useEffect(() => {
    console.log('Setting up simulated Kafka job notifications');
    setConnected(true);
    
    // Mock the process of receiving job status updates from Kafka
    // In a real implementation, this would fetch from an API endpoint
    const pollInterval = setInterval(() => {
      // For demo purposes, we'll show one mock notification every minute
      const randomIndex = Math.floor(Math.random() * mockJobStatusEvents.length);
      const jobStatus = mockJobStatusEvents[randomIndex];
      jobStatus.timestamp = new Date().toISOString(); // Update timestamp
      
      // Show fake notifications
      showJobStatusToast(jobStatus);
      
    }, 10000); // Poll every 10 seconds instead of 60 seconds for quicker feedback
    
    // Handle real job updates via normal API polling
    const fetchJobUpdates = async () => {
      try {
        // In production, uncomment this to fetch from real API
        // const response = await fetch('http://localhost:8082/api/jobs/updates');
        // const updates = await response.json();
        // updates.forEach(showJobStatusToast);
      } catch (error) {
        console.error('Error fetching job updates', error);
      }
    };
    
    // Initial fetch
    fetchJobUpdates();
    
    // Setup periodic polling
    const updateInterval = setInterval(fetchJobUpdates, 10000);
    
    // Clean up intervals on unmount
    return () => {
      clearInterval(pollInterval);
      clearInterval(updateInterval);
    };
  }, [showJobStatusToast]);
  
  return { connected };
} 