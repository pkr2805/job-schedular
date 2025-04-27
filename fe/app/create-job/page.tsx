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
import { CalendarIcon, RefreshCw } from "lucide-react"
import { Input } from "@/components/ui/input"
import { JobPreview } from "@/components/job-preview"
import { fetchJarFiles, createJobSchedule, JarFile } from "@/lib/api"

const formSchema = z.object({
  jarFile: z.string({
    required_error: "Please select a JAR file",
  }),
  executionType: z.enum(["immediate", "scheduled"], {
    required_error: "Please select an execution type",
  }),
  scheduledTime: z.date().optional(),
  frequency: z.enum(["one-time", "hourly", "daily", "weekly"], {
    required_error: "Please select a frequency",
  }),
})

export default function CreateJobPage() {
  const router = useRouter()
  const { toast } = useToast()
  const [showFrequency, setShowFrequency] = useState(false)
  const [jarFiles, setJarFiles] = useState<JarFile[]>([])
  const [loading, setLoading] = useState(false)
  const [isLoadingJars, setIsLoadingJars] = useState(true)

  // Function to fetch JAR files
  const getJarFiles = async () => {
    setIsLoadingJars(true);
    console.log('Starting to fetch JAR files...');

    try {
      // Try to fetch JAR files from the API
      console.log('Calling fetchJarFiles()...');
      const files = await fetchJarFiles();
      console.log('fetchJarFiles() returned:', files);

      // Set the JAR files from the API
      setJarFiles(files);
      console.log('Using JAR files from API:', files);

      // Show a message if no JAR files are available
      if (files.length === 0) {
        console.log('No JAR files found, showing toast...');
        toast({
          title: "No JAR files found",
          description: "No JAR files are available in MinIO. Please upload JAR files to MinIO first.",
          variant: "destructive",
        });
      } else {
        // Reset the form's JAR file selection if it's not in the new list
        const currentJarId = form.getValues("jarFile");
        if (currentJarId && !files.some(jar => jar.id === currentJarId)) {
          form.setValue("jarFile", "");
        }
      }
    } catch (error) {
      console.error('Error fetching JAR files:', error);
      toast({
        title: "Error",
        description: "Failed to fetch JAR files from the server.",
        variant: "destructive",
      });

      // Set empty array in case of error
      setJarFiles([]);
    } finally {
      console.log('Finished fetching JAR files, setting isLoadingJars to false');
      setIsLoadingJars(false);
    }
  };

  // Fetch JAR files on component mount
  useEffect(() => {
    getJarFiles();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

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
    try {
      setLoading(true);

      // Show loading toast
      toast({
        title: "Creating job...",
        description: "Your job is being created",
      });

      // Verify that the selected JAR file exists
      console.log('Available JAR files:', jarFiles);
      const selectedJar = jarFiles.find(jar => jar.id === values.jarFile);
      console.log('Selected JAR file:', selectedJar);

      if (!selectedJar) {
        throw new Error(`Selected JAR file with ID ${values.jarFile} not found. Please refresh the page and try again.`);
      }

      // Prepare request payload
      const jobScheduleRequest = {
        jarFileId: values.jarFile,
        executionType: values.executionType.toUpperCase(),
        scheduledTime: values.scheduledTime ? values.scheduledTime.toISOString() : null,
        recurrenceType: values.executionType === 'scheduled' ?
          // Convert frequency to the format expected by the backend
          values.frequency === 'one-time' ? 'ONE_TIME' :
          values.frequency === 'hourly' ? 'HOURLY' :
          values.frequency === 'daily' ? 'DAILY' :
          values.frequency === 'weekly' ? 'WEEKLY' : null
          : null,
      };

      console.log('Form values:', values);
      console.log('Job schedule request:', jobScheduleRequest);

      // Call API to create job
      console.log('Calling createJobSchedule with request:', jobScheduleRequest);
      const createdJob = await createJobSchedule(jobScheduleRequest);
      console.log('Job created successfully:', createdJob);

      // Show success toast
      toast({
        title: "Job created successfully",
        description: "Your job has been scheduled",
        variant: "default",
      });

      // Redirect to job history
      router.push("/job-history");
    } catch (error: any) {
      console.error('Error creating job:', error);

      // Extract error message from error object
      let errorMessage = "Failed to create job. Please try again.";
      if (error instanceof Error) {
        errorMessage = error.message;
      }

      toast({
        title: "Error",
        description: errorMessage,
        variant: "destructive",
      });
    } finally {
      setLoading(false);
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
                    <div className="flex justify-between items-center">
                      <FormLabel>JAR File</FormLabel>
                      <Button
                        type="button"
                        variant="outline"
                        size="sm"
                        onClick={(e) => {
                          e.preventDefault();
                          getJarFiles();
                        }}
                        disabled={isLoadingJars}
                      >
                        <RefreshCw className={`h-4 w-4 mr-2 ${isLoadingJars ? 'animate-spin' : ''}`} />
                        Refresh
                      </Button>
                    </div>
                    <Select onValueChange={field.onChange} value={field.value}>
                      <FormControl>
                        <SelectTrigger>
                          <SelectValue placeholder="Select a JAR file" />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        {isLoadingJars ? (
                          <SelectItem value="loading" disabled>
                            <div>
                              <span className="font-medium">Loading JAR files...</span>
                              <p className="text-xs text-muted-foreground">Please wait</p>
                            </div>
                          </SelectItem>
                        ) : jarFiles.length > 0 ? (
                          jarFiles.map((jar) => (
                            <SelectItem key={jar.id} value={jar.id}>
                              <div>
                                <span className="font-medium">{jar.name}</span>
                                <p className="text-xs text-muted-foreground">{jar.description}</p>
                              </div>
                            </SelectItem>
                          ))
                        ) : (
                          <SelectItem value="no-jars" disabled>
                            <div>
                              <span className="font-medium">No JAR files available</span>
                              <p className="text-xs text-muted-foreground">Please upload JAR files to MinIO first</p>
                            </div>
                          </SelectItem>
                        )}
                      </SelectContent>
                    </Select>
                    <FormDescription>Select a JAR file from MinIO to execute</FormDescription>
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

              <Button type="submit" className="w-full md:w-auto" disabled={loading || isLoadingJars || jarFiles.length === 0}>
                {loading ? "Creating..." : isLoadingJars ? "Loading JAR Files..." : jarFiles.length === 0 ? "No JAR Files Available" : "Create Job"}
              </Button>
            </form>
          </Form>
        </div>

        <div>
          <h2 className="text-xl font-semibold mb-4">Job Preview</h2>
          <Card>
            <CardContent className="pt-6">
              <JobPreview
                jarFile={watchJarFile ? jarFiles.find((jar) => jar.id === watchJarFile) : undefined}
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
