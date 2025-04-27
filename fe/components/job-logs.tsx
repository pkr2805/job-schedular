import { ScrollArea } from "@/components/ui/scroll-area"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert"
import { CheckCircle, AlertCircle } from "lucide-react"

interface JobLogsProps {
  logs: string
  response?: {
    status: string
    timestamp: string
    metadata?: {
      jarName?: string
      executionTime?: string
      [key: string]: any
    }
    error?: string
    logs?: string
    executionTime?: string
    jobId?: string
  } | null
}

export function JobLogs({ logs, response }: JobLogsProps) {
  return (
    <Tabs defaultValue="logs">
      <TabsList className="mb-4">
        <TabsTrigger value="logs">Execution Logs</TabsTrigger>
        <TabsTrigger value="response" disabled={!response}>
          Kafka Response
        </TabsTrigger>
      </TabsList>

      <TabsContent value="logs">
        {logs ? (
          <ScrollArea className="h-[300px] w-full rounded-md border p-4">
            <pre className="font-mono text-sm whitespace-pre-wrap">{logs}</pre>
          </ScrollArea>
        ) : (
          <div className="text-center py-8 text-muted-foreground">
            <p>No logs available for this job yet.</p>
          </div>
        )}
      </TabsContent>

      <TabsContent value="response">
        {response && (
          <div className="space-y-4">
            <Alert variant={response.status === "success" ? "default" : "destructive"}>
              {response.status === "success" ? (
                <CheckCircle className="h-4 w-4" />
              ) : (
                <AlertCircle className="h-4 w-4" />
              )}
              <AlertTitle className="capitalize">{response.status}</AlertTitle>
              <AlertDescription>
                {response.status === "success"
                  ? `Job completed successfully in ${response.executionTime || (response.metadata && response.metadata.executionTime) || 'unknown time'}`
                  : `Job failed: ${response.error || 'Unknown error'}`}
              </AlertDescription>
            </Alert>

            <div className="rounded-md border p-4">
              <h3 className="font-medium mb-2">Response Metadata</h3>
              <ScrollArea className="h-[200px]">
                <pre className="font-mono text-sm">{JSON.stringify(response, null, 2)}</pre>
              </ScrollArea>
            </div>
          </div>
        )}
      </TabsContent>
    </Tabs>
  )
}
