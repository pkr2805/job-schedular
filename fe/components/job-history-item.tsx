"use client"

import { useState } from "react"
import { TableCell, TableRow } from "@/components/ui/table"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog"
import { FileText, Trash2 } from "lucide-react"
import { JobLogs } from "@/components/job-logs"
import { KafkaMessageViewer } from "@/components/kafka-message-viewer"
import { useToast } from "@/components/ui/use-toast"

interface JobHistoryItemProps {
  job: {
    id: string
    jarName: string
    executionTime: string
    type: string
    status: string
    logs?: string
    response?: any
  }
  onCancelJob: (jobId: string) => void
}

export function JobHistoryItem({ job, onCancelJob }: JobHistoryItemProps) {
  const { toast } = useToast()
  const [showKafkaDetails, setShowKafkaDetails] = useState(false)

  const getStatusBadge = (status: string) => {
    switch (status.toLowerCase()) {
      case "completed":
        return (
          <Badge variant="default" className="bg-green-500">
            Completed
          </Badge>
        )
      case "running":
        return (
          <Badge variant="default" className="bg-blue-500">
            Running
          </Badge>
        )
      case "pending":
        return <Badge variant="outline">Pending</Badge>
      case "failed":
        return <Badge variant="destructive">Failed</Badge>
      case "cancelled":
        return (
          <Badge variant="default" className="bg-gray-500">
            Cancelled
          </Badge>
        )
      default:
        return <Badge variant="outline">{status}</Badge>
    }
  }

  return (
    <TableRow>
      <TableCell className="font-medium">{job.jarName}</TableCell>
      <TableCell>{job.executionTime}</TableCell>
      <TableCell>{job.type}</TableCell>
      <TableCell>{getStatusBadge(job.status)}</TableCell>
      <TableCell>
        {job.response ? (
          <KafkaMessageViewer response={job.response} />
        ) : (
          <span className="text-muted-foreground text-sm">No message yet</span>
        )}
      </TableCell>
      <TableCell className="text-right">
        <div className="flex justify-end gap-2">
          <Dialog>
            <DialogTrigger asChild>
              <Button variant="outline" size="sm" disabled={!job.logs}>
                <FileText className="h-4 w-4 mr-1" />
                Logs
              </Button>
            </DialogTrigger>
            <DialogContent className="max-w-3xl">
              <DialogHeader>
                <DialogTitle>Job Logs: {job.id}</DialogTitle>
              </DialogHeader>
              <JobLogs logs={job.logs || ''} response={job.response} />
            </DialogContent>
          </Dialog>

          {(job.status === "Pending" || job.status === "Running") && (
            <Button variant="destructive" size="sm" onClick={() => onCancelJob(job.id)}>
              <Trash2 className="h-4 w-4 mr-1" />
              Cancel
            </Button>
          )}
        </div>
      </TableCell>
    </TableRow>
  )
}
