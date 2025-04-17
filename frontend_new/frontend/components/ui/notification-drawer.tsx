"use client";

import React, { useEffect, useState } from 'react'
import { Bell } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { ScrollArea } from '@/components/ui/scroll-area'
import {
  Sheet,
  SheetContent,
  SheetHeader,
  SheetTitle,
  SheetTrigger,
} from '@/components/ui/sheet'
import { useJobNotifications } from '@/components/job-notification-provider'
import { format } from 'date-fns'

export function NotificationDrawer() {
  const [open, setOpen] = useState(false)
  const { notifications, unreadCount, markAsRead, removeNotification, refreshNotifications } = useJobNotifications()

  useEffect(() => {
    if (open) {
      refreshNotifications()
    }
  }, [open, refreshNotifications])

  const handleMarkAsRead = async (id: string) => {
    await markAsRead(id)
  }

  const handleDelete = async (id: string) => {
    await removeNotification(id)
  }

  const getColorClass = (type: string) => {
    switch (type) {
      case 'SUCCESS':
        return 'border-green-500 bg-green-50 dark:bg-green-950/30'
      case 'ERROR':
        return 'border-red-500 bg-red-50 dark:bg-red-950/30'
      case 'WARNING':
        return 'border-yellow-500 bg-yellow-50 dark:bg-yellow-950/30'
      case 'INFO':
      default:
        return 'border-blue-500 bg-blue-50 dark:bg-blue-950/30'
    }
  }

  return (
    <Sheet open={open} onOpenChange={setOpen}>
      <SheetTrigger asChild>
        <Button variant="ghost" size="icon" className="relative">
          <Bell className="h-5 w-5" />
          {unreadCount > 0 && (
            <Badge
              variant="destructive"
              className="absolute -right-1 -top-1 flex h-5 w-5 items-center justify-center rounded-full p-0 text-xs"
            >
              {unreadCount}
            </Badge>
          )}
        </Button>
      </SheetTrigger>
      <SheetContent className="w-full sm:max-w-md">
        <SheetHeader>
          <SheetTitle>Notifications</SheetTitle>
        </SheetHeader>
        <div className="mt-4">
          {notifications.length === 0 ? (
            <div className="flex h-40 items-center justify-center text-muted-foreground">
              No notifications
            </div>
          ) : (
            <ScrollArea className="h-[calc(100vh-100px)]">
              <div className="space-y-3 pr-3">
                {notifications.map((notification) => (
                  <div
                    key={notification.id}
                    className={`relative rounded-md border p-4 ${
                      getColorClass(notification.type)
                    } ${!notification.read ? 'border-l-4' : ''}`}
                  >
                    <div className="flex items-start justify-between">
                      <div>
                        <h4 className="text-sm font-medium">
                          {notification.jobName}
                        </h4>
                        <p className="mt-1 text-sm text-muted-foreground">
                          {notification.message}
                        </p>
                        <p className="mt-2 text-xs text-muted-foreground">
                          {format(new Date(notification.timestamp), "MMM d, h:mm a")}
                        </p>
                      </div>
                      <div className="flex space-x-2">
                        {!notification.read && (
                          <Button
                            size="sm"
                            variant="ghost"
                            onClick={() => handleMarkAsRead(notification.id)}
                          >
                            Mark as read
                          </Button>
                        )}
                        <Button
                          size="sm"
                          variant="ghost"
                          onClick={() => handleDelete(notification.id)}
                        >
                          Delete
                        </Button>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </ScrollArea>
          )}
        </div>
      </SheetContent>
    </Sheet>
  )
} 