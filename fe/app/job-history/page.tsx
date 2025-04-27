"use client"

import { useState, useEffect } from "react"
import { Input } from "@/components/ui/input"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Search } from "lucide-react"
import { useToast } from "@/components/ui/use-toast"
import { JobHistoryItem } from "@/components/job-history-item"
import { KafkaToastNotification } from "@/components/kafka-toast-notification"
import { fetchJobSchedules, fetchJobExecutions, cancelJob, JobSchedule, JobExecution } from "@/lib/api"

// Extended job type that includes UI-specific properties
interface ExtendedJobSchedule extends JobSchedule {
  logs?: string;
  response?: any;
  executionTime?: string;
  type?: string;
}

export default function JobHistoryPage() {
  const [jobs, setJobs] = useState<ExtendedJobSchedule[]>([])
  const [searchTerm, setSearchTerm] = useState("")
  const [statusFilter, setStatusFilter] = useState("all")
  const { toast } = useToast()
  // We'll keep this for future Kafka integration
  const [latestKafkaMessage] = useState<any>(null)
  const [loading, setLoading] = useState(true)

  // Fetch job schedules and their executions
  useEffect(() => {
    const getJobs = async () => {
      try {
        setLoading(true)
        const jobSchedules = await fetchJobSchedules()

        // Create extended jobs with basic info
        const extendedJobs: ExtendedJobSchedule[] = jobSchedules.map(job => ({
          ...job,
          executionTime: job.scheduledTime,
          type: job.executionType === 'immediate' ? 'Immediate' :
                (job.recurrenceType === 'one-time' ? 'Scheduled' : `Recurring (${job.recurrenceType})`),
          logs: '',
          response: null
        }))

        // Fetch job executions for each job schedule
        const jobsWithExecutions = await Promise.all(
          extendedJobs.map(async (job) => {
            try {
              // Fetch job executions for this job schedule
              const executions = await fetchJobExecutions(job.id)

              // If there are executions, use the latest one's data
              if (executions && executions.length > 0) {
                const latestExecution = executions[0] // API returns in descending order

                return {
                  ...job,
                  logs: latestExecution.logs || '',
                  response: latestExecution.response ? JSON.parse(latestExecution.response) : null,
                  executionTime: latestExecution.startTime || job.scheduledTime
                }
              }

              return job
            } catch (error) {
              console.error(`Error fetching executions for job ${job.id}:`, error)
              return job
            }
          })
        )

        setJobs(jobsWithExecutions)
      } catch (error) {
        console.error('Error fetching job schedules:', error)
        toast({
          title: "Error",
          description: "Failed to fetch job schedules. Please try again later.",
          variant: "destructive",
        })
        setJobs([])
      } finally {
        setLoading(false)
      }
    }

    getJobs()

    // Refresh job schedules every 10 seconds
    const intervalId = setInterval(() => {
      getJobs()
    }, 10000)

    return () => clearInterval(intervalId)
  }, [toast])

  const filteredJobs = jobs.filter((job) => {
    const matchesSearch =
      job.jarName.toLowerCase().includes(searchTerm.toLowerCase()) ||
      job.id.toLowerCase().includes(searchTerm.toLowerCase())
    const matchesStatus = statusFilter === "all" || job.status.toLowerCase() === statusFilter.toLowerCase()
    return matchesSearch && matchesStatus
  })

  // Convert backend status to frontend display format
  const formatStatus = (status: string) => {
    switch (status) {
      case "SCHEDULED": return "Pending"
      case "RUNNING": return "Running"
      case "COMPLETED": return "Completed"
      case "FAILED": return "Failed"
      case "CANCELLED": return "Cancelled"
      default: return status
    }
  }

  const handleCancelJob = async (jobId: string) => {
    try {
      // Show confirmation toast
      toast({
        title: "Cancelling job...",
        description: `Cancelling job ${jobId}`,
      })

      // Call API to cancel job
      await cancelJob(jobId)

      // Update job status locally
      setJobs(jobs.map((job) => (job.id === jobId ? { ...job, status: "CANCELLED" } : job)))

      // Show success toast
      toast({
        title: "Job cancelled",
        description: `Job ${jobId} has been cancelled`,
        variant: "default",
      })
    } catch (error: any) {
      console.error('Error cancelling job:', error)

      // Extract error message
      const errorMessage = error.message || "Failed to cancel job. Please try again.";

      toast({
        title: "Error",
        description: errorMessage,
        variant: "destructive",
      })
    }
  }

  return (
    <div className="container mx-auto py-10">
      <h1 className="text-3xl font-bold mb-6">Job History</h1>

      <div className="flex flex-col md:flex-row gap-4 mb-6">
        <div className="relative flex-1">
          <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
          <Input
            placeholder="Search by JAR name or job ID..."
            className="pl-8"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>
        <Select value={statusFilter} onValueChange={setStatusFilter}>
          <SelectTrigger className="w-full md:w-[180px]">
            <SelectValue placeholder="Filter by status" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">All Statuses</SelectItem>
            <SelectItem value="completed">Completed</SelectItem>
            <SelectItem value="running">Running</SelectItem>
            <SelectItem value="pending">Pending</SelectItem>
            <SelectItem value="failed">Failed</SelectItem>
            <SelectItem value="cancelled">Cancelled</SelectItem>
          </SelectContent>
        </Select>
      </div>

      <div className="rounded-md border">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>JAR Name</TableHead>
              <TableHead>Execution Time</TableHead>
              <TableHead>Type</TableHead>
              <TableHead>Status</TableHead>
              <TableHead>Kafka Message</TableHead>
              <TableHead>Kafka Logs</TableHead>
              <TableHead className="text-right">Actions</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {loading ? (
              <TableRow>
                <TableCell colSpan={7} className="h-24 text-center">
                  Loading job history...
                </TableCell>
              </TableRow>
            ) : filteredJobs.length > 0 ? (
              filteredJobs.map((job) => (
                <JobHistoryItem
                  key={job.id}
                  job={{
                    id: job.id,
                    jarName: job.jarName,
                    executionTime: job.executionTime || job.scheduledTime,
                    type: job.type || (job.executionType === 'immediate' ? 'Immediate' : 'Scheduled'),
                    status: formatStatus(job.status),
                    logs: job.logs || "No logs available",
                    response: job.response
                  }}
                  onCancelJob={handleCancelJob}
                />
              ))
            ) : (
              <TableRow>
                <TableCell colSpan={7} className="h-24 text-center">
                  No jobs found.
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </div>

      {latestKafkaMessage && <KafkaToastNotification message={latestKafkaMessage} />}
    </div>
  )
}
