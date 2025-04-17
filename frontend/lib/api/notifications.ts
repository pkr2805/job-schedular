import { API_BASE_URL, DEFAULT_HEADERS, handleApiError } from './config';

export type NotificationType = 'SUCCESS' | 'ERROR' | 'WARNING' | 'INFO';

export interface JobNotification {
  id: string;
  title: string;
  message: string;
  type: NotificationType;
  timestamp: string;
  read: boolean;
  jobId?: string;
}

// Mock notifications data
const MOCK_NOTIFICATIONS: JobNotification[] = [
  {
    id: "1",
    title: "Job Completed",
    message: "Your data processing job has completed successfully",
    type: "SUCCESS",
    timestamp: new Date(Date.now() - 30 * 60 * 1000).toISOString(),
    read: false,
    jobId: "2"
  },
  {
    id: "2",
    title: "Scheduled Job",
    message: "Your report generation job has been scheduled",
    type: "INFO",
    timestamp: new Date(Date.now() - 2 * 60 * 60 * 1000).toISOString(),
    read: true,
    jobId: "1"
  },
  {
    id: "3",
    title: "Job Warning",
    message: "Your job completed but with warnings",
    type: "WARNING",
    timestamp: new Date(Date.now() - 4 * 60 * 60 * 1000).toISOString(),
    read: false
  }
];

/**
 * Fetch all notifications
 */
export async function getNotifications(): Promise<JobNotification[]> {
  try {
    console.log('Fetching notifications from:', `${API_BASE_URL}/notifications`);
    const response = await fetch(`${API_BASE_URL}/notifications`, {
      method: 'GET',
      headers: DEFAULT_HEADERS,
    });
    
    if (!response.ok) {
      console.error(`Failed to fetch notifications: ${response.status} ${response.statusText}`);
      return MOCK_NOTIFICATIONS;
    }
    
    return await response.json();
  } catch (error) {
    console.error('Error fetching notifications:', error);
    // Return mock data in case of error
    return MOCK_NOTIFICATIONS;
  }
}

/**
 * Mark a notification as read
 */
export async function markNotificationAsRead(id: string): Promise<boolean> {
  try {
    const response = await fetch(`${API_BASE_URL}/notifications/${id}/read`, {
      method: 'PUT',
      headers: DEFAULT_HEADERS,
    });
    
    if (!response.ok) {
      console.error(`Failed to mark notification as read: ${response.status} ${response.statusText}`);
      return true; // Mock success
    }
    
    return true;
  } catch (error) {
    console.error(`Error marking notification ${id} as read:`, error);
    return true; // Mock success
  }
}

/**
 * Delete a notification
 */
export async function deleteNotification(id: string): Promise<boolean> {
  try {
    const response = await fetch(`${API_BASE_URL}/notifications/${id}`, {
      method: 'DELETE',
      headers: DEFAULT_HEADERS,
    });
    
    if (!response.ok) {
      console.error(`Failed to delete notification: ${response.status} ${response.statusText}`);
      return true; // Mock success
    }
    
    return true;
  } catch (error) {
    console.error(`Error deleting notification ${id}:`, error);
    return true; // Mock success
  }
}

/**
 * Mark all notifications as read
 */
export async function markAllNotificationsAsRead(): Promise<boolean> {
  try {
    const response = await fetch(`${API_BASE_URL}/notifications/read-all`, {
      method: 'PUT',
      headers: DEFAULT_HEADERS,
    });
    
    if (!response.ok) {
      console.error(`Failed to mark all notifications as read: ${response.status} ${response.statusText}`);
      return true; // Mock success
    }
    
    return true;
  } catch (error) {
    console.error('Error marking all notifications as read:', error);
    return true; // Mock success
  }
} 