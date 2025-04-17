import Link from "next/link"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Clock, Plus } from "lucide-react"

export default function HomePage() {
  return (
    <div className="container mx-auto py-10">
      <h1 className="text-4xl font-bold mb-8">Job Scheduler</h1>
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <Link href="/create-job">
          <Card className="h-full hover:shadow-md transition-shadow cursor-pointer">
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Plus className="h-5 w-5" />
                Create Job
              </CardTitle>
              <CardDescription>Schedule a new job by selecting a JAR file</CardDescription>
            </CardHeader>
            <CardContent>
              <p className="text-muted-foreground">
                Create a new job by selecting a JAR file and configuring execution settings. Run immediately or schedule
                for later.
              </p>
              <Button className="mt-4">Create New Job</Button>
            </CardContent>
          </Card>
        </Link>
        <Link href="/job-history">
          <Card className="h-full hover:shadow-md transition-shadow cursor-pointer">
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Clock className="h-5 w-5" />
                Job History
              </CardTitle>
              <CardDescription>View and manage all scheduled jobs</CardDescription>
            </CardHeader>
            <CardContent>
              <p className="text-muted-foreground">
                View the history of all jobs, their status, and execution details. Filter and search through your job
                history.
              </p>
              <Button className="mt-4" variant="outline">
                View Job History
              </Button>
            </CardContent>
          </Card>
        </Link>
      </div>
    </div>
  )
}
