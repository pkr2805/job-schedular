// API service for Jobs
const BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8082/api';

export interface Job {
  id: string;
  name: string;
  jarFile: string;
  cronExpression: string;
  status: 'SCHEDULED' | 'RUNNING' | 'COMPLETED' | 'FAILED' | 'CANCELLED';
  createdAt: string;
  lastRun?: string;
  nextRun?: string;
}

export async function getJobs(): Promise<Job[]> {
  try {
    const response = await fetch(`${BASE_URL}/jobs`);
    
    if (!response.ok) {
      throw new Error(`Failed to fetch jobs: ${response.statusText}`);
    }
    
    return await response.json();
  } catch (error) {
    console.warn('Error fetching jobs from API, returning mock data:', error);
    
    // Return mock data if API call fails
    return [
      {
        id: '1',
        name: 'Daily Report Generator',
        jarFile: '2',
        cronExpression: '0 0 12 * * ?',
        status: 'SCHEDULED',
        createdAt: new Date().toISOString(),
        lastRun: null,
        nextRun: new Date(Date.now() + 86400000).toISOString(),
      },
      {
        id: '2',
        name: 'Weekly Cleanup Task',
        jarFile: '1',
        cronExpression: '0 0 0 ? * SUN',
        status: 'COMPLETED',
        createdAt: new Date(Date.now() - 86400000 * 7).toISOString(),
        lastRun: new Date(Date.now() - 86400000).toISOString(),
        nextRun: new Date(Date.now() + 86400000 * 6).toISOString(),
      },
      {
        id: '3',
        name: 'Monthly Email Campaign',
        jarFile: '3',
        cronExpression: '0 0 9 1 * ?',
        status: 'FAILED',
        createdAt: new Date(Date.now() - 86400000 * 15).toISOString(),
        lastRun: new Date(Date.now() - 86400000 * 2).toISOString(),
        nextRun: null,
      },
    ];
  }
}

export async function createJob(job: Omit<Job, 'id' | 'status' | 'createdAt' | 'lastRun' | 'nextRun'>): Promise<Job> {
  try {
    const response = await fetch(`${BASE_URL}/jobs`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(job),
    });
    
    if (!response.ok) {
      throw new Error(`Failed to create job: ${response.statusText}`);
    }
    
    return await response.json();
  } catch (error) {
    console.warn('Error creating job via API, returning mock response:', error);
    
    // Return mock data if API call fails
    return {
      id: crypto.randomUUID(),
      status: 'SCHEDULED',
      createdAt: new Date().toISOString(),
      ...job,
    };
  }
}

export async function cancelJob(jobId: string): Promise<boolean> {
  try {
    const response = await fetch(`${BASE_URL}/jobs/${jobId}/cancel`, {
      method: 'POST',
    });
    
    if (!response.ok) {
      throw new Error(`Failed to cancel job: ${response.statusText}`);
    }
    
    return true;
  } catch (error) {
    console.warn('Error cancelling job via API:', error);
    return false;
  }
}

// Get job logs
export async function getJobLogs(jobId: string): Promise<{ output?: string; error?: string }> {
  try {
    const [outputResponse, errorResponse] = await Promise.all([
      fetch(`${BASE_URL}/jobs/${jobId}/output`),
      fetch(`${BASE_URL}/jobs/${jobId}/error`),
    ]);
    
    const output = outputResponse.ok ? await outputResponse.text() : undefined;
    const error = errorResponse.ok ? await errorResponse.text() : undefined;
    
    return { output, error };
  } catch (error) {
    console.error(`Error fetching logs for job ${jobId}:`, error);
    throw error;
  }
} 