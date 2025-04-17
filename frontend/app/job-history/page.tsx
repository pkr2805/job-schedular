"use client"

import { useState } from "react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Badge } from "@/components/ui/badge"
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog"
import { FileText, Search, Trash2 } from "lucide-react"
import { useToast } from "@/components/ui/use-toast"
import { JobLogs } from "@/components/job-logs"

// Mock job data
const initialJobs = [
  {
    id: "job-001",
    jarName: "hello-world.jar",
    executionTime: "2025-04-18 10:00 AM",
    type: "Immediate",
    status: "Completed",
    logs: "Hello, World!\nJob completed successfully.\nExecution time: 1.2s",
    response: {
      status: "success",
      timestamp: "2025-04-18T10:00:15Z",
      metadata: {
        user: "admin",
        jobId: "job-001",
        executionTime: "1.2s",
      },
    },
  },
  {
    id: "job-002",
    jarName: "date-printer.jar",
    executionTime: "2025-04-18 03:00 PM",
    type: "Recurring (Daily)",
    status: "Running",
    logs: "Starting job...\nPrinting current date and time...",
    response: null,
  },
  {
    id: "job-003",
    jarName: "data-processor.jar",
    executionTime: "2025-04-19 09:30 AM",
    type: "Scheduled",
    status: "Pending",
    logs: "",
    response: null,
  },
  {
    id: "job-004",
    jarName: "report-generator.jar",
    executionTime: "2025-04-17 02:15 PM",
    type: "Immediate",
    status: "Failed",
    logs: "Starting job...\nError: Could not find input file.\nJob failed with exit code 1.",
    response: {
      status: "failure",
      timestamp: "2025-04-17T14:15:30Z",
      error: "Could not find input file",
      metadata: {
        user: "admin",
        jobId: "job-004",
      },
    },
  },
  {
    id: "job-005",
    jarName: "hello-world.jar",
    executionTime: "2025-04-16 11:45 AM",
    type: "Immediate",
    status: "Completed",
    logs: "Hello, World!\nJob completed successfully.\nExecution time: 0.8s",
    response: {
      status: "success",
      timestamp: "2025-04-16T11:45:12Z",
      metadata: {
        user: "admin",
        jobId: "job-005",
        executionTime: "0.8s",
      },
    },
  },
]

export default function JobHistoryPage() {
  const [jobs, setJobs] = useState(initialJobs)
  const [searchTerm, setSearchTerm] = useState("")
  const [statusFilter, setStatusFilter] = useState("all")
  const { toast } = useToast()

  const filteredJobs = jobs.filter((job) => {
    const matchesSearch =
      job.jarName.toLowerCase().includes(searchTerm.toLowerCase()) ||
      job.id.toLowerCase().includes(searchTerm.toLowerCase())
    const matchesStatus = statusFilter === "all" || job.status.toLowerCase() === statusFilter.toLowerCase()
    return matchesSearch && matchesStatus
  })

  const handleCancelJob = (jobId: string) => {
    // Show confirmation toast
    toast({
      title: "Cancelling job...",
      description: `Cancelling job ${jobId}`,
    })

    // Simulate API delay
    setTimeout(() => {
      // Update job status
      setJobs(jobs.map((job) => (job.id === jobId ? { ...job, status: "Cancelled" } : job)))

      // Show success toast
      toast({
        title: "Job cancelled",
        description: `Job ${jobId} has been cancelled`,
        variant: "default",
      })
    }, 1000)
  }

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
              <TableHead className="text-right">Actions</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {filteredJobs.length > 0 ? (
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

                      {(job.status === "Pending" || job.status === "Running") && (
                        <Button variant="destructive" size="sm" onClick={() => handleCancelJob(job.id)}>
                          <Trash2 className="h-4 w-4 mr-1" />
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
