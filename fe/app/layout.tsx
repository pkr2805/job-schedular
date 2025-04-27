import type React from "react"
import "@/app/globals.css"
import type { Metadata } from "next"
import { Inter } from "next/font/google"
import { ThemeProvider } from "@/components/theme-provider"
import { ThemeInitializer } from "@/components/theme-initializer"
import { ThemeScript } from "@/components/theme-script"
import { Toaster } from "@/components/ui/toaster"
import { MainNav } from "@/components/main-nav"

const inter = Inter({ subsets: ["latin"] })

export const metadata: Metadata = {
  title: "Job Scheduler",
  description: "A professional job scheduler application",
  generator: 'v0.dev'
}

// We're using a script-based approach to prevent theme flashing
// and avoid hydration mismatches between server and client

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode
}>) {

  return (
    <html lang="en" suppressHydrationWarning>
      <head>
        {/* Removed the script to prevent theme flashing */}
      </head>
      <body className={inter.className}>
        {/* Add the theme initializer component */}
        <ThemeInitializer />

        <ThemeProvider
          attribute="class"
          defaultTheme="light"
          enableSystem
          disableTransitionOnChange
        >
          {/* Add the theme script component */}
          <ThemeScript />
          <div className="min-h-screen flex flex-col">
            <MainNav />
            <main className="flex-1">{children}</main>
          </div>
          <Toaster />
        </ThemeProvider>
      </body>
    </html>
  )
}