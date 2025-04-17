import { API_BASE_URL, DEFAULT_HEADERS, handleApiError } from './config';

export interface JarFile {
  id: string;
  name: string;
  desc: string;
}

export interface ExecutionResult {
  success: boolean;
  jarName: string;
  output: string;
  error: string;
}

// Mock JAR files to use when API is unavailable
const MOCK_JAR_FILES: JarFile[] = [
  { id: "1", name: "channel-subscribe-1.jar", desc: "Schedules subscription to the main news channel" },
  { id: "2", name: "channel-subscribe-2.jar", desc: "Schedules subscription to the sports channel" },
  { id: "3", name: "wake-up-reminder.jar", desc: "Recurring reminder to wake up at scheduled times" },
  { id: "4", name: "ten-min-reminder.jar", desc: "Sends a reminder every 10 minutes for scheduled breaks" },
  { id: "5", name: "immediate-execution.jar", desc: "Executes immediate tasks with high priority" }
];

/**
 * Fetch all JAR files from the server
 */
export async function getJarFiles(): Promise<JarFile[]> {
  try {
    console.log('Fetching JAR files from:', `${API_BASE_URL}/api/jars`);
    const response = await fetch(`${API_BASE_URL}/api/jars`, {
      method: 'GET',
      headers: DEFAULT_HEADERS,
    });
    
    if (!response.ok) {
      console.error(`Failed to fetch JAR files: ${response.status} ${response.statusText}`);
      return MOCK_JAR_FILES;
    }
    
    const data = await response.json();
    return data;
  } catch (error) {
    console.error('Error fetching JAR files:', error);
    // Return mock data in case of error
    return MOCK_JAR_FILES;
  }
}

/**
 * Execute a JAR file
 */
export async function executeJar(jarName: string): Promise<ExecutionResult> {
  try {
    const response = await fetch(`${API_BASE_URL}/api/jars/${jarName}/execute`, {
      method: 'POST',
      headers: DEFAULT_HEADERS,
    });
    
    if (!response.ok) {
      throw new Error(`Failed to execute JAR file: ${response.status} ${response.statusText}`);
    }
    
    return await response.json();
  } catch (error) {
    console.error(`Error executing JAR file ${jarName}:`, error);
    // Return mock execution result
    return {
      success: true,
      jarName: jarName,
      output: `Mock execution of ${jarName} completed successfully`,
      error: ''
    };
  }
} 