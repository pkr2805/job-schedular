"use client"

import { useState, useEffect } from "react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Badge } from "@/components/ui/badge"
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog"
import { FileText, Search, Trash2, RefreshCw, MessageSquare, PlayCircle } from "lucide-react"
import { useToast } from "@/components/ui/use-toast"
import { JobLogs } from "@/components/job-logs"

// Job interface
interface Job {
  id: string;
  jarName: string;
  executionTime: string;
  type: string;
  status: string;
  logs: string;
  kafkaMessageStatus?: string;
  kafkaMessageId?: string;
  kafkaMessageSent?: string;
  kafkaMessageResponse?: string;
  response: any;
}

// Kafka Message Details Component
function KafkaMessageDetails({ job }: { job: Job }) {
  const hasKafkaDetails = job.kafkaMessageStatus || job.kafkaMessageResponse;
  
  if (!hasKafkaDetails) {
    return (
      <div className="p-6 text-center">
        <MessageSquare className="mx-auto h-12 w-12 text-muted-foreground opacity-20 mb-2" />
        <p className="text-muted-foreground">No Kafka message details available</p>
      </div>
    );
  }
  
  // Attempt to parse Kafka response as JSON for better display
  let kafkaResponse = job.kafkaMessageResponse;
  try {
    const parsed = JSON.parse(job.kafkaMessageResponse || "{}");
    kafkaResponse = JSON.stringify(parsed, null, 2);
  } catch (e) {
    // Use the original response if parsing fails
  }
  
  return (
    <div className="space-y-4">
      <div className="grid grid-cols-2 gap-4">
        <div>
          <h3 className="text-sm font-medium text-muted-foreground mb-1">Status</h3>
          <div>
            {job.kafkaMessageStatus === "SUCCESS" ? (
              <Badge className="bg-green-500">SUCCESS</Badge>
            ) : job.kafkaMessageStatus === "FAILED" ? (
              <Badge variant="destructive">FAILED</Badge>
            ) : job.kafkaMessageStatus === "PENDING" ? (
              <Badge variant="outline">PENDING</Badge>
            ) : (
              <Badge variant="outline">NOT SENT</Badge>
            )}
          </div>
        </div>
        
        <div>
          <h3 className="text-sm font-medium text-muted-foreground mb-1">Sent At</h3>
          <p>{job.kafkaMessageSent || "N/A"}</p>
        </div>
      </div>
      
      {job.kafkaMessageId && (
        <div>
          <h3 className="text-sm font-medium text-muted-foreground mb-1">Message ID</h3>
          <p className="text-sm font-mono break-all">{job.kafkaMessageId}</p>
        </div>
      )}
      
      {kafkaResponse && (
        <div>
          <h3 className="text-sm font-medium text-muted-foreground mb-1">Message Content</h3>
          <pre className="text-xs bg-secondary p-4 rounded-md overflow-auto max-h-[300px] font-mono">
            {kafkaResponse}
          </pre>
        </div>
      )}
    </div>
  );
}

export default function JobHistoryPage() {
  const [jobs, setJobs] = useState<Job[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [searchTerm, setSearchTerm] = useState("")
  const [statusFilter, setStatusFilter] = useState("all")
  const { toast } = useToast()

  // Fetch jobs from backend
  useEffect(() => {
    const fetchJobs = async () => {
      try {
        setIsLoading(true)
        const response = await fetch("http://localhost:8083/api/jobs")
        
        if (!response.ok) {
          throw new Error("Failed to fetch jobs")
        }
        
        const data = await response.json()
        setJobs(data.map((job: any) => ({
          id: job.id,
          jarName: job.jarFile,
          executionTime: job.lastRunAt || job.scheduledAt || job.createdAt,
          type: job.type === "IMMEDIATE" ? "Immediate" : 
                job.type === "SCHEDULED" ? "Scheduled" : 
                "Recurring",
          status: job.status,
          logs: job.output || "",
          kafkaMessageId: job.kafkaMessageId,
          kafkaMessageStatus: job.kafkaMessageStatus,
          kafkaMessageSent: job.kafkaMessageSent,
          kafkaMessageResponse: job.kafkaMessageResponse,
          response: {
            status: job.status === "COMPLETED" ? "success" : 
                   job.status === "FAILED" ? "failure" : null,
            timestamp: job.updatedAt,
            error: job.error,
            metadata: {
              jobId: job.id
            }
          }
        })))
      } catch (error) {
        console.error("Error fetching jobs:", error)
        toast({
          title: "Error",
          description: "Failed to load jobs. Please try again later.",
          variant: "destructive",
        })
        setJobs([])
      } finally {
        setIsLoading(false)
      }
    }

    fetchJobs()
  }, [toast])

  const filteredJobs = jobs.filter((job) => {
    const matchesSearch =
      job.jarName.toLowerCase().includes(searchTerm.toLowerCase()) ||
      job.id.toLowerCase().includes(searchTerm.toLowerCase())
    const matchesStatus = statusFilter === "all" || job.status.toLowerCase() === statusFilter.toLowerCase()
    return matchesSearch && matchesStatus
  })

  const handleCancelJob = async (jobId: string) => {
    // Show confirmation toast
    toast({
      title: "Cancelling job...",
      description: `Cancelling job ${jobId}`,
    })

    try {
      const response = await fetch(`http://localhost:8083/api/jobs/${jobId}/cancel`, {
        method: "POST"
      })
      
      if (!response.ok) {
        throw new Error("Failed to cancel job")
      }
      
      // Update job status locally
      setJobs(jobs.map((job) => (job.id === jobId ? { ...job, status: "CANCELLED" } : job)))
      
      // Show success toast
      toast({
        title: "Job cancelled",
        description: `Job ${jobId} has been cancelled`,
        variant: "default",
      })
    } catch (error) {
      console.error("Error cancelling job:", error)
      toast({
        title: "Error",
        description: "Failed to cancel job. Please try again later.",
        variant: "destructive",
      })
    }
  }

  const handleRefresh = async () => {
    setIsLoading(true)
    try {
      const response = await fetch("http://localhost:8083/api/jobs")
      
      if (!response.ok) {
        throw new Error("Failed to fetch jobs")
      }
      
      const data = await response.json()
      setJobs(data.map((job: any) => ({
        id: job.id,
        jarName: job.jarFile,
        executionTime: job.lastRunAt || job.scheduledAt || job.createdAt,
        type: job.type === "IMMEDIATE" ? "Immediate" : 
              job.type === "SCHEDULED" ? "Scheduled" : 
              "Recurring",
        status: job.status,
        logs: job.output || "",
        kafkaMessageId: job.kafkaMessageId,
        kafkaMessageStatus: job.kafkaMessageStatus,
        kafkaMessageSent: job.kafkaMessageSent,
        kafkaMessageResponse: job.kafkaMessageResponse,
        response: {
          status: job.status === "COMPLETED" ? "success" : 
                 job.status === "FAILED" ? "failure" : null,
          timestamp: job.updatedAt,
          error: job.error,
          metadata: {
            jobId: job.id
          }
        }
      })))
      
      toast({
        title: "Refreshed",
        description: "Job list has been refreshed",
      })
    } catch (error) {
      console.error("Error refreshing jobs:", error)
      toast({
        title: "Error",
        description: "Failed to refresh jobs. Please try again later.",
        variant: "destructive",
      })
    } finally {
      setIsLoading(false)
    }
  }

  const handleExecute = async (jobId: string) => {
    try {
      setIsLoading(true);
      const response = await fetch(`http://localhost:8083/api/jobs/${jobId}/execute`, {
        method: "POST",
      });

      if (!response.ok) {
        throw new Error("Failed to execute job");
      }

      toast({
        title: "Success",
        description: "Job execution started",
        variant: "default",
      });

      // Refresh job list
      try {
        const response = await fetch("http://localhost:8083/api/jobs");
        
        if (!response.ok) {
          throw new Error("Failed to fetch jobs");
        }
        
        const data = await response.json();
        setJobs(data.map((job: any) => ({
          id: job.id,
          jarName: job.jarFile,
          executionTime: job.lastRunAt || job.scheduledAt || job.createdAt,
          type: job.type === "IMMEDIATE" ? "Immediate" : 
                job.type === "SCHEDULED" ? "Scheduled" : 
                "Recurring",
          status: job.status,
          logs: job.output || "",
          kafkaMessageId: job.kafkaMessageId,
          kafkaMessageStatus: job.kafkaMessageStatus,
          kafkaMessageSent: job.kafkaMessageSent,
          kafkaMessageResponse: job.kafkaMessageResponse,
          response: {
            status: job.status === "COMPLETED" ? "success" : 
                   job.status === "FAILED" ? "failure" : null,
            timestamp: job.updatedAt,
            error: job.error,
            metadata: {
              jobId: job.id
            }
          }
        })));
      } catch (error) {
        console.error("Error refreshing jobs:", error);
      }
    } catch (error) {
      console.error("Error executing job:", error);
      toast({
        title: "Error",
        description: "Failed to execute job",
        variant: "destructive",
      });
    } finally {
      setIsLoading(false);
    }
  };

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
      case "scheduled":
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
    <div className="container mx-auto py-10">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold">Job History</h1>
        <Button variant="outline" onClick={handleRefresh} disabled={isLoading}>
          <RefreshCw className="h-4 w-4 mr-2" />
          Refresh
        </Button>
      </div>

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
              <TableHead className="text-right">Actions</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {isLoading ? (
              <TableRow>
                <TableCell colSpan={5} className="h-24 text-center">
                  Loading jobs...
                </TableCell>
              </TableRow>
            ) : filteredJobs.length > 0 ? (
              filteredJobs.map((job) => (
                <TableRow key={job.id}>
                  <TableCell className="font-medium">{job.jarName}</TableCell>
                  <TableCell>{job.executionTime}</TableCell>
                  <TableCell>{job.type}</TableCell>
                  <TableCell>{getStatusBadge(job.status)}</TableCell>
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
                          <JobLogs logs={job.logs} response={job.response} />
                        </DialogContent>
                      </Dialog>
                      
                      <Dialog>
                        <DialogTrigger asChild>
                          <Button variant="outline" size="sm">
                            <MessageSquare className="h-4 w-4 mr-1" />
                            Kafka
                          </Button>
                        </DialogTrigger>
                        <DialogContent className="max-w-2xl">
                          <DialogHeader>
                            <DialogTitle>Kafka Message Details: {job.id}</DialogTitle>
                          </DialogHeader>
                          <KafkaMessageDetails job={job} />
                        </DialogContent>
                      </Dialog>

                      {(job.status.toLowerCase() === "pending" || job.status.toLowerCase() === "running") && (
                        <Button
                          variant="destructive"
                          size="sm"
                          onClick={() => handleCancelJob(job.id)}
                        >
                          <Trash2 className="mr-1 h-4 w-4" />
                          Cancel
                        </Button>
                      )}
                    </div>
                  </TableCell>
                </TableRow>
              ))
            ) : (
              <TableRow>
                <TableCell colSpan={5} className="h-24 text-center">
                  No jobs found.
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </div>
    </div>
  )
}
