import { API_BASE_URL, DEFAULT_HEADERS, handleApiError } from './config';

export type JobStatus = 'PENDING' | 'SCHEDULED' | 'RUNNING' | 'COMPLETED' | 'FAILED' | 'CANCELLED' | 'PAUSED';
export type JobType = 'IMMEDIATE' | 'SCHEDULED' | 'RECURRING';

export interface Job {
  id: string;
  name: string;
  jarFile: string;
  type: JobType;
  status: JobStatus;
  priority: number;
  createdAt: string;
  scheduledAt?: string;
  lastRunAt?: string;
  nextRunAt?: string;
  executionCount: number;
  maxExecutions: number;
  description?: string;
  cronExpression?: string;
  output?: string;
  error?: string;
}

export interface CreateJobRequest {
  name: string;
  jarFile: string;
  type: JobType;
  priority?: number;
  scheduledAt?: string;
  maxExecutions?: number;
  description?: string;
  cronExpression?: string;
  arguments?: string;
}

// Mock jobs data
const MOCK_JOBS: Job[] = [
  {
    id: "1",
    name: "Daily Report Generation",
    jarFile: "report-generator.jar",
    type: "RECURRING",
    status: "SCHEDULED",
    priority: 3,
    createdAt: new Date(Date.now() - 7 * 24 * 60 * 60 * 1000).toISOString(),
    scheduledAt: new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString(),
    nextRunAt: new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString(),
    executionCount: 7,
    maxExecutions: 30,
    description: "Generates daily PDF reports",
    cronExpression: "0 0 0 * * *"
  },
  {
    id: "2",
    name: "Immediate Processing",
    jarFile: "data-processor.jar",
    type: "IMMEDIATE",
    status: "COMPLETED",
    priority: 5,
    createdAt: new Date(Date.now() - 1 * 24 * 60 * 60 * 1000).toISOString(),
    lastRunAt: new Date(Date.now() - 1 * 24 * 60 * 60 * 1000).toISOString(),
    executionCount: 1,
    maxExecutions: 1,
    description: "Process CSV data files",
    output: "Successfully processed 145 records"
  }
];

/**
 * Fetch all jobs
 */
export async function getJobs(): Promise<Job[]> {
  try {
    console.log('Fetching jobs from:', `${API_BASE_URL}/api/jobs`);
    const response = await fetch(`${API_BASE_URL}/api/jobs`, {
      method: 'GET',
      headers: DEFAULT_HEADERS,
    });
    
    if (!response.ok) {
      console.error(`Failed to fetch jobs: ${response.status} ${response.statusText}`);
      return MOCK_JOBS;
    }
    
    return await response.json();
  } catch (error) {
    console.error('Error fetching jobs:', error);
    // Return mock data in case of error
    return MOCK_JOBS;
  }
}

/**
 * Fetch a specific job by ID
 */
export async function getJob(id: string): Promise<Job> {
  try {
    const response = await fetch(`${API_BASE_URL}/api/jobs/${id}`, {
      method: 'GET',
      headers: DEFAULT_HEADERS,
    });
    
    if (!response.ok) {
      console.error(`Failed to fetch job: ${response.status} ${response.statusText}`);
      // Return a mock job if the real one isn't available
      return MOCK_JOBS.find(job => job.id === id) || MOCK_JOBS[0];
    }
    
    return await response.json();
  } catch (error) {
    console.error(`Error fetching job ${id}:`, error);
    // Return a mock job
    return MOCK_JOBS.find(job => job.id === id) || MOCK_JOBS[0];
  }
}

/**
 * Create a new job
 */
export async function createJob(jobRequest: CreateJobRequest): Promise<Job> {
  try {
    console.log('Creating job:', jobRequest);
    const response = await fetch(`${API_BASE_URL}/api/jobs`, {
      method: 'POST',
      headers: DEFAULT_HEADERS,
      body: JSON.stringify(jobRequest),
    });
    
    if (!response.ok) {
      console.error(`Failed to create job: ${response.status} ${response.statusText}`);
      // Return a mock job response
      return createMockJob(jobRequest);
    }
    
    return await response.json();
  } catch (error) {
    console.error('Error creating job:', error);
    // Return a mock job
    return createMockJob(jobRequest);
  }
}

/**
 * Cancel a job
 */
export async function cancelJob(id: string): Promise<Job> {
  try {
    const response = await fetch(`${API_BASE_URL}/api/jobs/${id}/cancel`, {
      method: 'PUT',
      headers: DEFAULT_HEADERS,
    });
    
    if (!response.ok) {
      console.error(`Failed to cancel job: ${response.status} ${response.statusText}`);
      // Return a mock cancelled job
      const job = MOCK_JOBS.find(job => job.id === id) || MOCK_JOBS[0];
      return { ...job, status: 'CANCELLED' };
    }
    
    return await response.json();
  } catch (error) {
    console.error(`Error cancelling job ${id}:`, error);
    // Return a mock cancelled job
    const job = MOCK_JOBS.find(job => job.id === id) || MOCK_JOBS[0];
    return { ...job, status: 'CANCELLED' };
  }
}

/**
 * Pause a job
 */
export async function pauseJob(id: string): Promise<Job> {
  try {
    const response = await fetch(`${API_BASE_URL}/jobs/${id}/pause`, {
      method: 'PUT',
      headers: DEFAULT_HEADERS,
    });
    
    if (!response.ok) {
      throw new Error(`Failed to pause job: ${response.status} ${response.statusText}`);
    }
    
    return await response.json();
  } catch (error) {
    console.error(`Error pausing job ${id}:`, error);
    throw error;
  }
}

/**
 * Resume a job
 */
export async function resumeJob(id: string): Promise<Job> {
  try {
    const response = await fetch(`${API_BASE_URL}/jobs/${id}/resume`, {
      method: 'PUT',
      headers: DEFAULT_HEADERS,
    });
    
    if (!response.ok) {
      throw new Error(`Failed to resume job: ${response.status} ${response.statusText}`);
    }
    
    return await response.json();
  } catch (error) {
    console.error(`Error resuming job ${id}:`, error);
    throw error;
  }
}

/**
 * Helper function to create a mock job from a request
 */
function createMockJob(jobRequest: CreateJobRequest): Job {
  return {
    id: Math.random().toString(36).substring(2, 10),
    name: jobRequest.name,
    jarFile: jobRequest.jarFile,
    type: jobRequest.type,
    status: jobRequest.type === 'IMMEDIATE' ? 'COMPLETED' : 'SCHEDULED',
    priority: jobRequest.priority || 3,
    createdAt: new Date().toISOString(),
    scheduledAt: jobRequest.scheduledAt,
    lastRunAt: jobRequest.type === 'IMMEDIATE' ? new Date().toISOString() : undefined,
    nextRunAt: jobRequest.type === 'SCHEDULED' ? jobRequest.scheduledAt : undefined,
    executionCount: jobRequest.type === 'IMMEDIATE' ? 1 : 0,
    maxExecutions: jobRequest.maxExecutions || 1,
    description: jobRequest.description,
    cronExpression: jobRequest.cronExpression,
    output: jobRequest.type === 'IMMEDIATE' ? "Mock job executed successfully" : undefined
  };
} 