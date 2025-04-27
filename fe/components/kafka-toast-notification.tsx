"use client"

import { useEffect } from "react"
import { useToast } from "@/components/ui/use-toast"
import { ToastAction } from "@/components/ui/toast"
import { CheckCircle, AlertCircle, MessageSquare } from "lucide-react"

interface KafkaToastNotificationProps {
  message: {
    jobId: string
    status: string
    timestamp: string
    metadata: {
      user: string
      jobId: string
      executionTime?: string
    }
    error?: string
  }
}

export function KafkaToastNotification({ message }: KafkaToastNotificationProps) {
  const { toast } = useToast()
  const isSuccess = message.status === "success"

  useEffect(() => {
    toast({
      title: (
        <div className="flex items-center gap-2">
          {isSuccess ? (
            <CheckCircle className="h-4 w-4 text-green-500" />
          ) : (
            <AlertCircle className="h-4 w-4 text-red-500" />
          )}
          <span>Kafka Message: {isSuccess ? "Success" : "Error"}</span>
        </div>
      ),
      description: isSuccess
        ? `Job ${message.jobId} completed successfully in ${message.metadata.executionTime}`
        : `Job ${message.jobId} failed: ${message.error}`,
      action: (
        <ToastAction altText="View Details">
          <MessageSquare className="h-3 w-3 mr-1" />
          Details
        </ToastAction>
      ),
    })
  }, [toast, message, isSuccess])

  return null
}
