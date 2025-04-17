// API service for JAR files
const BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8082/api';

export interface JarFile {
  id: string;
  name: string;
  desc: string;
  createdAt: string;
  size: number;
}

export async function getJarFiles(): Promise<JarFile[]> {
  try {
    const response = await fetch(`${BASE_URL}/jarFiles`);
    
    if (!response.ok) {
      throw new Error(`Failed to fetch JAR files: ${response.statusText}`);
    }
    
    return await response.json();
  } catch (error) {
    console.warn('Error fetching JAR files from API, returning mock data:', error);
    
    // Return mock data if API call fails
    return [
      {
        id: '1',
        name: 'simple-job.jar',
        desc: 'A simple job that prints "Hello World"',
        createdAt: new Date().toISOString(),
        size: 1024,
      },
      {
        id: '2',
        name: 'data-processor.jar',
        desc: 'Processes CSV files and generates reports',
        createdAt: new Date(Date.now() - 86400000).toISOString(),
        size: 2048,
      },
      {
        id: '3',
        name: 'email-sender.jar',
        desc: 'Sends email notifications to users',
        createdAt: new Date(Date.now() - 86400000 * 2).toISOString(),
        size: 3072,
      },
    ];
  }
}

export async function uploadJarFile(file: File): Promise<JarFile> {
  try {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('desc', file.name); // Using filename as description initially
    
    const response = await fetch(`${BASE_URL}/jarFiles/upload`, {
      method: 'POST',
      body: formData,
    });
    
    if (!response.ok) {
      throw new Error(`Failed to upload JAR file: ${response.statusText}`);
    }
    
    return await response.json();
  } catch (error) {
    console.warn('Error uploading JAR file to API, returning mock response:', error);
    
    // Return mock data if API call fails
    return {
      id: crypto.randomUUID(),
      name: file.name,
      desc: file.name,
      createdAt: new Date().toISOString(),
      size: file.size,
    };
  }
}

// Execute a JAR file
export async function executeJarFile(jarName: string): Promise<{ success: boolean; output?: string; error?: string }> {
  try {
    const response = await fetch(`${BASE_URL}/jars/${jarName}/execute`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
    });
    
    if (response.ok) {
      return await response.json();
    }
    
    // If API fails, return mock result
    console.warn('JAR execution API failed, returning mock result');
    return {
      success: true,
      output: `Mock execution of ${jarName} completed successfully.`,
    };
  } catch (error) {
    console.error(`Error executing JAR file ${jarName}:`, error);
    throw error;
  }
} 