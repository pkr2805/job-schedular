"use client"

import { useState, useEffect } from "react"
import { useRouter } from "next/navigation"
import { zodResolver } from "@hookform/resolvers/zod"
import { useForm } from "react-hook-form"
import { z } from "zod"
import { Button } from "../../components/ui/button"
import { Card, CardContent } from "../../components/ui/card"
import { Form, FormControl, FormDescription, FormField, FormItem, FormLabel, FormMessage } from "../../components/ui/form"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "../../components/ui/select"
import { RadioGroup, RadioGroupItem } from "../../components/ui/radio-group"
import { useToast } from "../../components/ui/use-toast"
import { Calendar } from "../../components/ui/calendar"
import { Popover, PopoverContent, PopoverTrigger } from "../../components/ui/popover"
import { cn } from "../../lib/utils"
import { format } from "date-fns"
import { CalendarIcon } from "lucide-react"
import { Input } from "../../components/ui/input"
import { JobPreview } from "../../components/job-preview"
import { JarFile, getJarFiles } from "../../lib/api/jar-files"
import { CreateJobRequest, createJob } from "../../lib/api/jobs"

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
  const [isLoading, setIsLoading] = useState(false)

  useEffect(() => {
    async function loadJarFiles() {
      try {
        const files = await getJarFiles()
        setJarFiles(files)
      } catch (error) {
        console.error("Failed to load JAR files:", error)
        toast({
          title: "Error loading JAR files",
          description: "Could not load JAR files from the server. Using sample data instead.",
          variant: "destructive",
        })
      }
    }
    
    loadJarFiles()
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
    setIsLoading(true)
    
    try {
      // Show loading toast
      toast({
        title: "Creating job...",
        description: "Your job is being created",
      })

      // Find the selected JAR file details
      const selectedJar = jarFiles.find(jar => jar.id === values.jarFile)
      
      if (!selectedJar) {
        throw new Error("Selected JAR file not found")
      }
      
      // Create job request
      const jobRequest: CreateJobRequest = {
        name: `Job for ${selectedJar.name}`,
        jarFile: selectedJar.name,
        type: values.executionType === "immediate" ? "IMMEDIATE" : "SCHEDULED",
        description: selectedJar.desc,
        priority: 5 // Default priority
      }
      
      // Add scheduled time if applicable
      if (values.executionType === "scheduled" && values.scheduledTime) {
        jobRequest.scheduledAt = values.scheduledTime.toISOString()
        
        // Add cron expression based on frequency
        if (values.frequency !== "one-time") {
          // Simple cron expressions
          switch (values.frequency) {
            case "hourly":
              jobRequest.cronExpression = "0 0 * * * *" // Every hour
              jobRequest.type = "RECURRING"
              break
            case "daily":
              jobRequest.cronExpression = "0 0 0 * * *" // Every day at midnight
              jobRequest.type = "RECURRING"
              break
            case "weekly":
              jobRequest.cronExpression = "0 0 0 * * 0" // Every Sunday at midnight
              jobRequest.type = "RECURRING"
              break
          }
        }
      }
      
      // Send the request to create the job
      const result = await createJob(jobRequest)
      
      // Show success toast
      toast({
        title: "Job created successfully",
        description: `Your job "${result.name}" has been scheduled`,
        variant: "default",
      })
      
      // Redirect to job history
      router.push("/job-history")
    } catch (error) {
      console.error("Failed to create job:", error)
      toast({
        title: "Error creating job",
        description: error instanceof Error ? error.message : "An unknown error occurred",
        variant: "destructive",
      })
    } finally {
      setIsLoading(false)
    }
  }

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
                        {jarFiles.map((jar) => (
                          <SelectItem key={jar.id} value={jar.id}>
                            <div>
                              <span className="font-medium">{jar.name}</span>
                              <p className="text-xs text-muted-foreground">{jar.desc}</p>
                            </div>
                          </SelectItem>
                        ))}
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

              <Button type="submit" disabled={isLoading}>
                {isLoading ? "Creating..." : "Create Job"}
              </Button>
            </form>
          </Form>
        </div>
        <div>
          <Card>
            <CardContent className="p-6">
              <h2 className="text-xl font-bold mb-4">Job Preview</h2>
              <JobPreview
                jarFile={watchJarFile ? jarFiles.find(jar => jar.id === watchJarFile) : undefined}
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
