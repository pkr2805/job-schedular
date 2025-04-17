import Link from "next/link"
import { Button } from "@/components/ui/button"
import { FileQuestion } from "lucide-react"

export default function NotFound() {
  return (
    <div className="flex flex-col items-center justify-center min-h-[80vh] px-4">
      <div className="flex flex-col items-center text-center max-w-md">
        <div className="rounded-full bg-muted p-4 mb-4">
          <FileQuestion className="h-10 w-10 text-muted-foreground" />
        </div>
        <h1 className="text-3xl font-bold mb-2">Page not found</h1>
        <p className="text-muted-foreground mb-6">The page you are looking for doesn't exist or has been moved.</p>
        <Button asChild>
          <Link href="/">Return to home</Link>
        </Button>
      </div>
    </div>
  )
}
