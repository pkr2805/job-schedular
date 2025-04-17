"use client"

import { ThemeProvider } from "@/components/theme-provider"
import { JobNotificationProvider } from "@/components/job-notification-provider"
import { NotificationDrawer } from "@/components/ui/notification-drawer"
import { ModeToggle } from "@/components/ui/theme-toggle"
import { cn } from "@/lib/utils"

interface RootClientWrapperProps {
  children: React.ReactNode
}

export default function RootClientWrapper({ children }: RootClientWrapperProps) {
  return (
    <ThemeProvider
      attribute="class"
      defaultTheme="system"
      enableSystem
      disableTransitionOnChange
    >
      <JobNotificationProvider>
        <div className="fixed top-4 right-4 flex items-center gap-2 z-50">
          <NotificationDrawer />
          <ModeToggle />
        </div>
        <div className={cn("min-h-screen bg-background font-sans antialiased")}>
          {children}
        </div>
      </JobNotificationProvider>
    </ThemeProvider>
  )
} 