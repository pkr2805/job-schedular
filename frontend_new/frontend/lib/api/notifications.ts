// API service for job notifications
const BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8083/api';

export interface JobNotification {
  id: string;
  jobId: string;
  jobName: string;
  message: string;
  type: 'SUCCESS' | 'WARNING' | 'ERROR' | 'INFO';
  timestamp: string;
  read: boolean;
}

export async function getNotifications(): Promise<JobNotification[]> {
  try {
    const response = await fetch(`${BASE_URL}/notifications`);
    
    if (!response.ok) {
      throw new Error(`Failed to fetch notifications: ${response.statusText}`);
    }
    
    return await response.json();
  } catch (error) {
    console.warn('Error fetching notifications from API:', error);
    return []; // Return empty array instead of mock data
  }
}

export async function markNotificationAsRead(notificationId: string): Promise<boolean> {
  try {
    const response = await fetch(`${BASE_URL}/notifications/${notificationId}/read`, {
      method: 'POST',
    });
    
    if (!response.ok) {
      throw new Error(`Failed to mark notification as read: ${response.statusText}`);
    }
    
    return true;
  } catch (error) {
    console.warn('Error marking notification as read via API:', error);
    return false;
  }
}

export async function deleteNotification(notificationId: string): Promise<boolean> {
  try {
    const response = await fetch(`${BASE_URL}/notifications/${notificationId}`, {
      method: 'DELETE',
    });
    
    if (!response.ok) {
      throw new Error(`Failed to delete notification: ${response.statusText}`);
    }
    
    return true;
  } catch (error) {
    console.warn('Error deleting notification via API:', error);
    return false;
  }
} 