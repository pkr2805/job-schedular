import { format } from "date-fns"
import { FileText, Calendar, Clock, RefreshCw } from "lucide-react"

interface JobPreviewProps {
  jarFile?: {
    id: string
    name: string
    description: string
  }
  executionType: string
  scheduledTime?: Date
  frequency: string
}

export function JobPreview({ jarFile, executionType, scheduledTime, frequency }: JobPreviewProps) {
  if (!jarFile) {
    return (
      <div className="text-center py-8 text-muted-foreground">
        <FileText className="mx-auto h-12 w-12 opacity-20 mb-2" />
        <p>Select a JAR file to preview job details</p>
      </div>
    )
  }

  return (
    <div className="space-y-4">
      <div>
        <h3 className="text-sm font-medium text-muted-foreground mb-1">JAR File</h3>
        <div className="flex items-start gap-2">
          <FileText className="h-5 w-5 mt-0.5 text-muted-foreground" />
          <div>
            <p className="font-medium">{jarFile.name}</p>
            <p className="text-sm text-muted-foreground">{jarFile.description}</p>
          </div>
        </div>
      </div>

      <div>
        <h3 className="text-sm font-medium text-muted-foreground mb-1">Execution</h3>
        <div className="flex items-center gap-2">
          {executionType === "immediate" ? (
            <>
              <Clock className="h-5 w-5 text-muted-foreground" />
              <p>Run immediately</p>
            </>
          ) : (
            <>
              <Calendar className="h-5 w-5 text-muted-foreground" />
              <p>{scheduledTime ? format(scheduledTime, "PPP 'at' h:mm a") : "Schedule not set"}</p>
            </>
          )}
        </div>
      </div>

      {executionType === "scheduled" && (
        <div>
          <h3 className="text-sm font-medium text-muted-foreground mb-1">Frequency</h3>
          <div className="flex items-center gap-2">
            <RefreshCw className="h-5 w-5 text-muted-foreground" />
            <p className="capitalize">{frequency.replace("-", " ")}</p>
          </div>
        </div>
      )}
    </div>
  )
}
