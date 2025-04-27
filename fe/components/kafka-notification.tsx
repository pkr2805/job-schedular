"use client"

import { useEffect } from "react"
import { useToast } from "@/components/ui/use-toast"
import { ToastAction } from "@/components/ui/toast"
import { CheckCircle, AlertCircle } from "lucide-react"

interface KafkaNotificationProps {
  jobId: string
  status: "success" | "failure"
  message: string
}

export function KafkaNotification({ jobId, status, message }: KafkaNotificationProps) {
  const { toast } = useToast()

  useEffect(() => {
    toast({
      title: (
        <div className="flex items-center gap-2">
          {status === "success" ? (
            <CheckCircle className="h-4 w-4 text-green-500" />
          ) : (
            <AlertCircle className="h-4 w-4 text-red-500" />
          )}
          {status === "success" ? "Success" : "Error"}
        </div>
      ),
      description: message,
      variant: status === "success" ? "default" : "destructive",
      action: <ToastAction altText="View Details">View Details</ToastAction>,
    })
  }, [toast, jobId, status, message])

  return null
}
