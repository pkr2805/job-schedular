import "@/styles/globals.css"
import { Inter } from "next/font/google"
import { Metadata } from "next"
import { Toaster } from "@/components/ui/toaster"
import { MainNav } from "@/components/main-nav"
import RootClientWrapper from "@/components/root-client-wrapper"

const inter = Inter({ subsets: ["latin"] })

export const metadata: Metadata = {
  title: "Job Scheduler",
  description: "A professional job scheduler application",
  generator: 'v0.dev'
}

export default function RootLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <html lang="en" suppressHydrationWarning>
      <body className={inter.className}>
        <RootClientWrapper>
          <div className="min-h-screen flex flex-col">
            <MainNav />
            <main className="flex-1">{children}</main>
          </div>
          <Toaster />
        </RootClientWrapper>
      </body>
    </html>
  )
}

import './globals.css'