// API client for the Job Scheduler backend

const API_BASE_URL = 'http://localhost:8080/api';

// Types
export interface JarFile {
  id: string;
  name: string;
  description: string;
  size: number;
  uploadedAt: string;
}

export interface JobSchedule {
  id: string;
  jarFileId: string;
  jarName: string;
  executionType: string;
  scheduledTime: string;
  recurrenceType: string;
  status: string;
  createdAt: string;
  updatedAt: string;
}

export interface JobExecution {
  id: string;
  jobScheduleId: string;
  startTime: string;
  endTime: string;
  status: string;
  logs: string;
  errorMessage: string;
  executionTime: string;
  response: string;
}

export interface JobScheduleRequest {
  jarFileId: string;
  executionType: string;
  scheduledTime?: string | null;
  recurrenceType?: string | null;
}

// API functions
export async function fetchJarFiles(): Promise<JarFile[]> {
  console.log('Fetching JAR files from API...');
  try {
    console.log('Making API request to:', `${API_BASE_URL}/jar-files`);

    // Add a timeout to the fetch request
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), 30000); // 30 second timeout

    try {
      const response = await fetch(`${API_BASE_URL}/jar-files`, {
        signal: controller.signal
      });

      clearTimeout(timeoutId);

      console.log('API response status:', response.status, response.statusText);

      if (!response.ok) {
        const errorText = await response.text();
        console.error('Error response from server:', errorText);
        throw new Error(`Error fetching JAR files: ${errorText || response.statusText}`);
      }

      const data = await response.json();
      console.log('API response data:', data);
      return data;
    } finally {
      clearTimeout(timeoutId);
    }
  } catch (error: any) {
    console.error('Error fetching JAR files:', error);

    // Check if it's a network error
    if (error.name === 'TypeError' && error.message === 'Failed to fetch') {
      console.error('Network error: Failed to connect to the backend server. Make sure the backend is running.');
    }

    // Check if it's a timeout error
    if (error.name === 'AbortError') {
      console.error('Request timed out. The backend might not be running or is unresponsive.');
    }

    // Return empty array if API is not available
    // This ensures we only show JAR files that are actually in MinIO
    return [];
  }
}

export async function fetchJobSchedules(): Promise<JobSchedule[]> {
  console.log('Fetching job schedules from API...');
  try {
    console.log('Making API request to:', `${API_BASE_URL}/job-schedules`);

    // Add a timeout to the fetch request
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), 30000); // 30 second timeout

    try {
      const response = await fetch(`${API_BASE_URL}/job-schedules`, {
        signal: controller.signal
      });

      clearTimeout(timeoutId);

      console.log('API response status:', response.status, response.statusText);

      if (!response.ok) {
        const errorText = await response.text();
        console.error('Error response from server:', errorText);
        throw new Error(`Error fetching job schedules: ${errorText || response.statusText}`);
      }

      const data = await response.json();
      console.log('API response data:', data);
      return data;
    } finally {
      clearTimeout(timeoutId);
    }
  } catch (error: any) {
    console.error('Error fetching job schedules:', error);

    // Check if it's a network error
    if (error.name === 'TypeError' && error.message === 'Failed to fetch') {
      console.error('Network error: Failed to connect to the backend server. Make sure the backend is running.');
    }

    // Check if it's a timeout error
    if (error.name === 'AbortError') {
      console.error('Request timed out. The backend might not be running or is unresponsive.');
    }

    // Return empty array if API is not available
    return [];
  }
}

export async function fetchJobExecutions(jobScheduleId: string): Promise<JobExecution[]> {
  console.log('Fetching job executions from API...');
  try {
    console.log('Making API request to:', `${API_BASE_URL}/job-executions/job-schedule/${jobScheduleId}`);

    // Add a timeout to the fetch request
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), 30000); // 30 second timeout

    try {
      const response = await fetch(`${API_BASE_URL}/job-executions/job-schedule/${jobScheduleId}`, {
        signal: controller.signal
      });

      clearTimeout(timeoutId);

      console.log('API response status:', response.status, response.statusText);

      if (!response.ok) {
        const errorText = await response.text();
        console.error('Error response from server:', errorText);
        throw new Error(`Error fetching job executions: ${errorText || response.statusText}`);
      }

      const data = await response.json();
      console.log('API response data:', data);
      return data;
    } finally {
      clearTimeout(timeoutId);
    }
  } catch (error: any) {
    console.error('Error fetching job executions:', error);

    // Check if it's a network error
    if (error.name === 'TypeError' && error.message === 'Failed to fetch') {
      console.error('Network error: Failed to connect to the backend server. Make sure the backend is running.');
    }

    // Check if it's a timeout error
    if (error.name === 'AbortError') {
      console.error('Request timed out. The backend might not be running or is unresponsive.');
    }

    // Return empty array if API is not available
    return [];
  }
}

// These functions were previously used for UUID conversion
// They are no longer needed as we're using the actual UUIDs from the database

export async function createJobSchedule(jobScheduleRequest: JobScheduleRequest): Promise<JobSchedule> {
  try {
    // The jarFileId is already a UUID from the database, so we don't need to convert it
    // Just use it directly
    console.log('Sending job schedule request:', jobScheduleRequest);

    // Add a timeout to the fetch request
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), 30000); // 30 second timeout

    try {
      console.log('Making API request to:', `${API_BASE_URL}/job-schedules`);
      console.log('Request body:', JSON.stringify(jobScheduleRequest));

      const response = await fetch(`${API_BASE_URL}/job-schedules`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(jobScheduleRequest),
        signal: controller.signal
      });

      clearTimeout(timeoutId);

      console.log('API response status:', response.status, response.statusText);

      if (!response.ok) {
        const errorText = await response.text();
        console.error('Error response from server:', errorText);
        throw new Error(`Error creating job schedule: ${errorText || response.statusText}`);
      }

      const data = await response.json();
      console.log('API response data:', data);
      return data;
    } finally {
      clearTimeout(timeoutId);
    }
  } catch (error: any) {
    console.error('Error creating job schedule:', error);

    // Check if it's a network error
    if (error.name === 'TypeError' && error.message === 'Failed to fetch') {
      console.error('Network error: Failed to connect to the backend server. Make sure the backend is running.');
      throw new Error('Failed to connect to the backend server. Make sure the backend is running.');
    }

    // Check if it's a timeout error
    if (error.name === 'AbortError') {
      console.error('Request timed out. The backend might not be running or is unresponsive.');
      throw new Error('Request timed out. The backend might not be running or is unresponsive.');
    }

    throw error;
  }
}

export async function cancelJob(jobScheduleId: string): Promise<void> {
  try {
    // The jobScheduleId is already a UUID from the database, so we don't need to convert it
    console.log('Cancelling job with ID:', jobScheduleId);

    // Add a timeout to the fetch request
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), 30000); // 30 second timeout

    try {
      console.log('Making API request to:', `${API_BASE_URL}/job-schedules/${jobScheduleId}/cancel`);

      const response = await fetch(`${API_BASE_URL}/job-schedules/${jobScheduleId}/cancel`, {
        method: 'POST',
        signal: controller.signal
      });

      clearTimeout(timeoutId);

      console.log('API response status:', response.status, response.statusText);

      if (!response.ok) {
        let errorText = '';
        try {
          // Try to get error text, but handle empty responses
          const text = await response.text();
          errorText = text || response.statusText;
        } catch (e) {
          errorText = response.statusText;
        }
        console.error('Error response from server:', errorText);
        throw new Error(`Error cancelling job: ${errorText}`);
      }
    } finally {
      clearTimeout(timeoutId);
    }
  } catch (error: any) {
    console.error('Error cancelling job:', error);

    // Check if it's a network error
    if (error.name === 'TypeError' && error.message === 'Failed to fetch') {
      console.error('Network error: Failed to connect to the backend server. Make sure the backend is running.');
      throw new Error('Failed to connect to the backend server. Make sure the backend is running.');
    }

    // Check if it's a timeout error
    if (error.name === 'AbortError') {
      console.error('Request timed out. The backend might not be running or is unresponsive.');
      throw new Error('Request timed out. The backend might not be running or is unresponsive.');
    }

    throw error;
  }
}
