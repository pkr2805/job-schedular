import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog"
import { ScrollArea } from "@/components/ui/scroll-area"
import { CheckCircle, AlertCircle, MessageSquare } from "lucide-react"

interface KafkaMessageViewerProps {
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

export function KafkaMessageViewer({ response }: KafkaMessageViewerProps) {
  if (!response) {
    return <span className="text-muted-foreground text-sm">No message yet</span>
  }

  const isSuccess = response.status === "success"

  return (
    <Dialog>
      <DialogTrigger asChild>
        <div className="flex items-center gap-2 cursor-pointer">
          <Badge
            variant="outline"
            className={
              isSuccess
                ? "bg-green-100 text-green-800 hover:bg-green-100 border-green-200"
                : "bg-red-100 text-red-800 hover:bg-red-100 border-red-200"
            }
          >
            {isSuccess ? (
              <>
                <CheckCircle className="h-3 w-3 mr-1" />
                Success
              </>
            ) : (
              <>
                <AlertCircle className="h-3 w-3 mr-1" />
                Failed
              </>
            )}
          </Badge>
          <Button variant="ghost" size="sm" className="h-7 px-2 text-xs">
            <MessageSquare className="h-3 w-3 mr-1" />
            View Message
          </Button>
        </div>
      </DialogTrigger>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Kafka Message</DialogTitle>
        </DialogHeader>
        <div className="space-y-4">
          <div className="flex items-center gap-2">
            <Badge
              variant="outline"
              className={
                isSuccess
                  ? "bg-green-100 text-green-800 hover:bg-green-100 border-green-200"
                  : "bg-red-100 text-red-800 hover:bg-red-100 border-red-200"
              }
            >
              {isSuccess ? "SUCCESS" : "FAILURE"}
            </Badge>
            <span className="text-sm text-muted-foreground">{new Date(response.timestamp).toLocaleString()}</span>
          </div>

          <div className="rounded-md border p-4">
            <h3 className="font-medium mb-2">Message Content</h3>
            <ScrollArea className="h-[300px]">
              <pre className="font-mono text-sm whitespace-pre-wrap">{JSON.stringify(response, null, 2)}</pre>
            </ScrollArea>
          </div>

          {isSuccess ? (
            <div className="text-green-600 flex items-center gap-2">
              <CheckCircle className="h-4 w-4" />
              <span>Job completed successfully in {response.executionTime || (response.metadata && response.metadata.executionTime) || 'unknown time'}</span>
            </div>
          ) : (
            <div className="text-red-600 flex items-center gap-2">
              <AlertCircle className="h-4 w-4" />
              <span>Error: {response.error || 'Unknown error'}</span>
            </div>
          )}
        </div>
      </DialogContent>
    </Dialog>
  )
}
