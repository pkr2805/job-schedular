'use client'

import { useEffect } from 'react'
import { useTheme } from 'next-themes'

// This component is used to initialize the theme on the client side
// It doesn't render anything visible
export function ThemeScript() {
  const { setTheme } = useTheme()
  
  useEffect(() => {
    // Get theme from localStorage or cookie
    const storedTheme = localStorage.getItem('theme') || 
                        document.cookie.split('; ').find(row => row.startsWith('theme='))?.split('=')[1] || 
                        'system'
    
    // Set the theme
    setTheme(storedTheme)
  }, [setTheme])
  
  return null
}
