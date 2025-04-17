"use client"

import { useState, useEffect } from "react"
import { useRouter } from "next/navigation"
import { zodResolver } from "@hookform/resolvers/zod"
import { useForm } from "react-hook-form"
import { z } from "zod"
import { Button } from "@/components/ui/button"
import { Card, CardContent } from "@/components/ui/card"
import { Form, FormControl, FormDescription, FormField, FormItem, FormLabel, FormMessage } from "@/components/ui/form"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group"
import { useToast } from "@/components/ui/use-toast"
import { Calendar } from "@/components/ui/calendar"
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover"
import { cn } from "@/lib/utils"
import { format } from "date-fns"
import { CalendarIcon } from "lucide-react"
import { Input } from "@/components/ui/input"
import { JobPreview } from "@/components/job-preview"

// Interface for JAR files
interface JarFile {
  id: string;
  name: string;
  desc: string;
}

const formSchema = z.object({
  jarFile: z.string({
    required_error: "Please select a JAR file",
  }),
  executionType: z.enum(["immediate", "scheduled"], {
    required_error: "Please select an execution type",
  }),
  scheduledTime: z.date().optional(),
  frequency: z.enum(["one-time", "hourly", "daily", "weekly"]).default("one-time"),
})

export default function CreateJobPage() {
  const router = useRouter()
  const { toast } = useToast()
  const [showFrequency, setShowFrequency] = useState(false)
  const [jarFiles, setJarFiles] = useState<JarFile[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [isSubmitting, setIsSubmitting] = useState(false)

  useEffect(() => {
    // Fetch JAR files from the backend API
    const fetchJarFiles = async () => {
      try {
        setIsLoading(true)
        const response = await fetch("http://localhost:8083/api/jars")
        if (!response.ok) {
          throw new Error("Failed to fetch JAR files")
        }
        const data = await response.json()
        setJarFiles(data)
      } catch (error) {
        console.error("Error fetching JAR files:", error)
        toast({
          title: "Error",
          description: "Failed to load JAR files. Please try again later.",
          variant: "destructive",
        })
      } finally {
        setIsLoading(false)
      }
    }

    fetchJarFiles()
  }, [toast])

  const form = useForm<z.infer<typeof formSchema>>({
    resolver: zodResolver(formSchema),
    defaultValues: {
      executionType: "immediate",
      frequency: "one-time",
    },
  })

  const watchJarFile = form.watch("jarFile")
  const watchExecutionType = form.watch("executionType")
  const watchScheduledTime = form.watch("scheduledTime")
  const watchFrequency = form.watch("frequency")

  const onSubmit = async (values: z.infer<typeof formSchema>) => {
    setIsSubmitting(true)
    
    try {
      // Get selected JAR file
      const selectedJar = jarFiles.find((jar) => jar.id === values.jarFile)
      
      if (!selectedJar) {
        throw new Error("Selected JAR file not found")
      }
      
      // Prepare job data
      const jobData = {
        name: `${selectedJar.name} job`,
        jarFile: selectedJar.name,
        description: selectedJar.desc,
        type: values.executionType === "immediate" ? "IMMEDIATE" : "SCHEDULED",
        status: "PENDING",
        scheduledAt: values.executionType === "scheduled" ? values.scheduledTime?.toISOString() : null,
        priority: 1,
        executionCount: 0,
        maxExecutions: values.executionType === "scheduled" && values.frequency !== "one-time" ? 100 : 1,
        frequency: values.executionType === "scheduled" ? mapFrequency(values.frequency) : null,
        cronExpression: values.executionType === "scheduled" ? getCronExpression(values.frequency, values.scheduledTime) : null
      }
      
      // Show loading toast
      toast({
        title: "Creating job...",
        description: "Your job is being created",
      })
      
      // Submit job to backend
      const response = await fetch("http://localhost:8083/api/jobs", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(jobData),
      })
      
      if (!response.ok) {
        throw new Error("Failed to create job")
      }
      
      const createdJob = await response.json()
      
      // Show success toast
      toast({
        title: "Job created successfully",
        description: `Job ID: ${createdJob.id} has been scheduled`,
        variant: "default",
      })
      
      // Automatically execute the job if it's immediate
      if (values.executionType === "immediate") {
        try {
          const executeResponse = await fetch(`http://localhost:8083/api/jobs/${createdJob.id}/execute`, {
            method: "POST",
          })
          
          if (executeResponse.ok) {
            toast({
              title: "Job execution started",
              description: "The job has been automatically started",
              variant: "default",
            })
          }
        } catch (executeError) {
          console.error("Error executing job:", executeError)
          // Don't show error toast, just log it, as the job creation was successful
        }
      }
      
      // Redirect to job history
      router.push("/job-history")
    } catch (error) {
      console.error("Error creating job:", error)
      toast({
        title: "Error",
        description: error instanceof Error ? error.message : "Failed to create job",
        variant: "destructive",
      })
    } finally {
      setIsSubmitting(false)
    }
  }
  
  // Helper function to generate cron expression from form values
  const getCronExpression = (frequency: string, scheduledDate?: Date): string => {
    if (!scheduledDate) return "";
    
    const minutes = scheduledDate.getMinutes();
    const hours = scheduledDate.getHours();
    const dayOfMonth = scheduledDate.getDate();
    const month = scheduledDate.getMonth() + 1;
    const dayOfWeek = scheduledDate.getDay();
    
    switch (frequency) {
      case "hourly":
        return `0 ${minutes} * * * ?`;
      case "daily":
        return `0 ${minutes} ${hours} * * ?`;
      case "weekly":
        return `0 ${minutes} ${hours} ? * ${dayOfWeek}`;
      default: // one-time
        return `0 ${minutes} ${hours} ${dayOfMonth} ${month} ?`;
    }
  };
  
  // Helper function to map frontend frequency to backend JobFrequency enum
  const mapFrequency = (frequency: string): string => {
    switch (frequency) {
      case "hourly":
        return "HOURLY";
      case "daily":
        return "DAILY";
      case "weekly":
        return "WEEKLY";
      case "monthly":
        return "MONTHLY";
      default: // one-time
        return "ONE_TIME";
    }
  };

  return (
    <div className="container mx-auto py-10">
      <h1 className="text-3xl font-bold mb-6">Create New Job</h1>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <div className="lg:col-span-2">
          <Form {...form}>
            <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-8">
              <FormField
                control={form.control}
                name="jarFile"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>JAR File</FormLabel>
                    <Select onValueChange={field.onChange} defaultValue={field.value}>
                      <FormControl>
                        <SelectTrigger>
                          <SelectValue placeholder="Select a JAR file" />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        {isLoading ? (
                          <SelectItem value="loading" disabled>Loading JAR files...</SelectItem>
                        ) : jarFiles.length > 0 ? (
                          jarFiles.map((jar) => (
                            <SelectItem key={jar.id} value={jar.id}>
                              <div>
                                <span className="font-medium">{jar.name}</span>
                                <p className="text-xs text-muted-foreground">{jar.desc}</p>
                              </div>
                            </SelectItem>
                          ))
                        ) : (
                          <SelectItem value="none" disabled>No JAR files available</SelectItem>
                        )}
                      </SelectContent>
                    </Select>
                    <FormDescription>Select a JAR file to execute</FormDescription>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name="executionType"
                render={({ field }) => (
                  <FormItem className="space-y-3">
                    <FormLabel>Execution Type</FormLabel>
                    <FormControl>
                      <RadioGroup
                        onValueChange={(value) => {
                          field.onChange(value)
                          setShowFrequency(value === "scheduled")
                        }}
                        defaultValue={field.value}
                        className="flex flex-col space-y-1"
                      >
                        <FormItem className="flex items-center space-x-3 space-y-0">
                          <FormControl>
                            <RadioGroupItem value="immediate" />
                          </FormControl>
                          <FormLabel className="font-normal">Run Immediately</FormLabel>
                        </FormItem>
                        <FormItem className="flex items-center space-x-3 space-y-0">
                          <FormControl>
                            <RadioGroupItem value="scheduled" />
                          </FormControl>
                          <FormLabel className="font-normal">Schedule for later</FormLabel>
                        </FormItem>
                      </RadioGroup>
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              {watchExecutionType === "scheduled" && (
                <>
                  <FormField
                    control={form.control}
                    name="scheduledTime"
                    render={({ field }) => (
                      <FormItem className="flex flex-col">
                        <FormLabel>Scheduled Time</FormLabel>
                        <Popover>
                          <PopoverTrigger asChild>
                            <FormControl>
                              <Button
                                variant={"outline"}
                                className={cn(
                                  "w-full pl-3 text-left font-normal",
                                  !field.value && "text-muted-foreground",
                                )}
                              >
                                {field.value ? format(field.value, "PPP HH:mm:ss") : <span>Pick a date and time</span>}
                                <CalendarIcon className="ml-auto h-4 w-4 opacity-50" />
                              </Button>
                            </FormControl>
                          </PopoverTrigger>
                          <PopoverContent className="w-auto p-0" align="start">
                            <Calendar mode="single" selected={field.value} onSelect={field.onChange} initialFocus />
                            <div className="p-3 border-t border-border">
                              <Input
                                type="time"
                                step="1"
                                onChange={(e) => {
                                  const [hours, minutes, seconds] = e.target.value.split(":").map(Number)
                                  const newDate = field.value ? new Date(field.value) : new Date()
                                  newDate.setHours(hours || 0, minutes || 0, seconds || 0)
                                  field.onChange(newDate)
                                }}
                              />
                            </div>
                          </PopoverContent>
                        </Popover>
                        <FormDescription>Select when you want the job to run</FormDescription>
                        <FormMessage />
                      </FormItem>
                    )}
                  />

                  <FormField
                    control={form.control}
                    name="frequency"
                    render={({ field }) => (
                      <FormItem>
                        <FormLabel>Frequency</FormLabel>
                        <Select onValueChange={field.onChange} defaultValue={field.value}>
                          <FormControl>
                            <SelectTrigger>
                              <SelectValue placeholder="Select frequency" />
                            </SelectTrigger>
                          </FormControl>
                          <SelectContent>
                            <SelectItem value="one-time">One-time</SelectItem>
                            <SelectItem value="hourly">Hourly</SelectItem>
                            <SelectItem value="daily">Daily</SelectItem>
                            <SelectItem value="weekly">Weekly</SelectItem>
                          </SelectContent>
                        </Select>
                        <FormDescription>How often should this job run?</FormDescription>
                        <FormMessage />
                      </FormItem>
                    )}
                  />
                </>
              )}

              <Button type="submit" className="w-full md:w-auto" disabled={isLoading || jarFiles.length === 0 || isSubmitting}>
                {isSubmitting ? "Creating..." : "Create Job"}
              </Button>
            </form>
          </Form>
        </div>

        <div>
          <h2 className="text-xl font-semibold mb-4">Job Preview</h2>
          <Card>
            <CardContent className="pt-6">
              <JobPreview
                jarFile={watchJarFile && jarFiles.length > 0 ? 
                  jarFiles.find((jar) => jar.id === watchJarFile) : undefined}
                executionType={watchExecutionType}
                scheduledTime={watchScheduledTime}
                frequency={watchFrequency}
              />
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  )
}
