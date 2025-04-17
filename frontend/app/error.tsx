"use client"

import { useEffect } from "react"
import Link from "next/link"
import { Button } from "@/components/ui/button"
import { AlertCircle } from "lucide-react"

export default function Error({
  error,
  reset,
}: {
  error: Error & { digest?: string }
  reset: () => void
}) {
  useEffect(() => {
    // Log the error to an error reporting service
    console.error(error)
  }, [error])

  return (
    <div className="flex flex-col items-center justify-center min-h-[80vh] px-4">
      <div className="flex flex-col items-center text-center max-w-md">
        <div className="rounded-full bg-red-100 p-4 mb-4">
          <AlertCircle className="h-10 w-10 text-red-600" />
        </div>
        <h1 className="text-3xl font-bold mb-2">Something went wrong</h1>
        <p className="text-muted-foreground mb-6">
          We apologize for the inconvenience. An error occurred while processing your request.
        </p>
        <div className="flex flex-col sm:flex-row gap-4">
          <Button onClick={reset} variant="default">
            Try again
          </Button>
          <Button asChild variant="outline">
            <Link href="/">Return to home</Link>
          </Button>
        </div>
      </div>
    </div>
  )
}
