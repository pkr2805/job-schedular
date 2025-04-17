"use client"

import Link from "next/link"
import { usePathname } from "next/navigation"
import { cn } from "@/lib/utils"
import { Button } from "@/components/ui/button"
import { ModeToggle } from "@/components/ui/theme-toggle"
import { Clock, Home, Plus } from "lucide-react"

export function MainNav() {
  const pathname = usePathname()

  return (
    <header className="sticky top-0 z-50 w-full border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
      <div className="container flex h-14 items-center">
        <div className="mr-4 hidden md:flex">
          <Link href="/" className="mr-6 flex items-center space-x-2">
            <Clock className="h-6 w-6" />
            <span className="hidden font-bold sm:inline-block">Job Scheduler</span>
          </Link>
          <nav className="flex items-center space-x-6 text-sm font-medium">
            <Link
              href="/"
              className={cn(
                "transition-colors hover:text-foreground/80",
                pathname === "/" ? "text-foreground" : "text-foreground/60",
              )}
            >
              <div className="flex items-center gap-1">
                <Home className="h-4 w-4" />
                <span>Home</span>
              </div>
            </Link>
            <Link
              href="/create-job"
              className={cn(
                "transition-colors hover:text-foreground/80",
                pathname?.startsWith("/create-job") ? "text-foreground" : "text-foreground/60",
              )}
            >
              <div className="flex items-center gap-1">
                <Plus className="h-4 w-4" />
                <span>Create Job</span>
              </div>
            </Link>
            <Link
              href="/job-history"
              className={cn(
                "transition-colors hover:text-foreground/80",
                pathname?.startsWith("/job-history") ? "text-foreground" : "text-foreground/60",
              )}
            >
              <div className="flex items-center gap-1">
                <Clock className="h-4 w-4" />
                <span>Job History</span>
              </div>
            </Link>
          </nav>
        </div>

        <div className="flex flex-1 items-center justify-between space-x-2 md:justify-end">
          <div className="w-full flex-1 md:w-auto md:flex-none">
            <nav className="flex items-center justify-between md:hidden">
              <Link href="/" className="flex items-center space-x-2">
                <Clock className="h-6 w-6" />
                <span className="font-bold">Job Scheduler</span>
              </Link>
              <div className="flex items-center gap-2">
                <Button variant="ghost" size="icon" asChild>
                  <Link href="/">
                    <Home className="h-5 w-5" />
                    <span className="sr-only">Home</span>
                  </Link>
                </Button>
                <Button variant="ghost" size="icon" asChild>
                  <Link href="/create-job">
                    <Plus className="h-5 w-5" />
                    <span className="sr-only">Create Job</span>
                  </Link>
                </Button>
                <Button variant="ghost" size="icon" asChild>
                  <Link href="/job-history">
                    <Clock className="h-5 w-5" />
                    <span className="sr-only">Job History</span>
                  </Link>
                </Button>
              </div>
            </nav>
          </div>
          <ModeToggle />
        </div>
      </div>
    </header>
  )
}
