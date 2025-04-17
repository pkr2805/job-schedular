"use client"

import React, { createContext, useContext, useEffect, useState } from "react";
import { getNotifications, JobNotification, markNotificationAsRead, deleteNotification } from "@/lib/api/notifications";
import { useToast } from "@/components/ui/use-toast";
import { Badge } from '@/components/ui/badge';

interface JobNotificationContextType {
  notifications: JobNotification[];
  unreadCount: number;
  markAsRead: (id: string) => Promise<void>;
  removeNotification: (id: string) => Promise<void>;
  refreshNotifications: () => Promise<void>;
}

const JobNotificationContext = createContext<JobNotificationContextType | undefined>(undefined);

export function useJobNotifications() {
  const context = useContext(JobNotificationContext);
  if (!context) {
    throw new Error("useJobNotifications must be used within a JobNotificationProvider");
  }
  return context;
}

export function JobNotificationProvider({ children }: { children: React.ReactNode }) {
  const [notifications, setNotifications] = useState<JobNotification[]>([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const { toast } = useToast();

  const refreshNotifications = async () => {
    try {
      const fetchedNotifications = await getNotifications();
      setNotifications(fetchedNotifications);
      setUnreadCount(fetchedNotifications.filter(n => !n.read).length);
    } catch (error) {
      console.error("Failed to fetch notifications:", error);
    }
  };

  const markAsRead = async (id: string) => {
    const success = await markNotificationAsRead(id);
    if (success) {
      setNotifications(prevNotifications =>
        prevNotifications.map(notification =>
          notification.id === id ? { ...notification, read: true } : notification
        )
      );
      setUnreadCount(prev => Math.max(0, prev - 1));
    } else {
      toast({
        title: "Error",
        description: "Failed to mark notification as read",
        variant: "destructive",
      });
    }
  };

  const removeNotification = async (id: string) => {
    const success = await deleteNotification(id);
    if (success) {
      const notification = notifications.find(n => n.id === id);
      setNotifications(prevNotifications =>
        prevNotifications.filter(notification => notification.id !== id)
      );
      if (notification && !notification.read) {
        setUnreadCount(prev => Math.max(0, prev - 1));
      }
    } else {
      toast({
        title: "Error",
        description: "Failed to delete notification",
        variant: "destructive",
      });
    }
  };

  useEffect(() => {
    refreshNotifications();
    
    // Set up polling to refresh notifications every minute
    const intervalId = setInterval(refreshNotifications, 60000);
    
    return () => clearInterval(intervalId);
  }, []);

  return (
    <JobNotificationContext.Provider
      value={{
        notifications,
        unreadCount,
        markAsRead,
        removeNotification,
        refreshNotifications
      }}
    >
      {/* Show Kafka connection status indicator in development mode */}
      <div className="fixed bottom-4 left-4 z-50">
        <Badge
          variant={notifications.length > 0 ? 'outline' : 'destructive'}
          className="flex items-center gap-1.5"
        >
          <span
            className={`h-2 w-2 rounded-full ${
              notifications.length > 0 ? 'bg-green-500' : 'bg-red-500'
            }`}
          />
          {notifications.length > 0 ? `New Notifications: ${unreadCount}` : 'No Notifications'}
        </Badge>
      </div>
      {children}
    </JobNotificationContext.Provider>
  );
} 