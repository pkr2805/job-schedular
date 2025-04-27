'use client'

import { useEffect } from 'react'

// This component injects a script that prevents theme flashing
export function ThemeInitializer() {
  useEffect(() => {
    // This script runs immediately and prevents theme flashing
    const script = document.createElement('script')
    script.innerHTML = `
      (function() {
        // Get theme from localStorage or cookie
        function getTheme() {
          try {
            const storedTheme = localStorage.getItem('theme');
            if (storedTheme) return storedTheme;

            const cookieTheme = document.cookie.split('; ')
              .find(row => row.startsWith('theme='))
              ?.split('=')[1];
            if (cookieTheme) return cookieTheme;

            // Always default to light theme to match server rendering
            return 'light';
          } catch (e) {
            return 'light';
          }
        }

        // Apply theme immediately to prevent flashing
        const theme = getTheme();
        if (theme === 'dark' ||
           (theme === 'system' && window.matchMedia('(prefers-color-scheme: dark)').matches)) {
          document.documentElement.classList.add('dark');
          document.documentElement.style.colorScheme = 'dark';
        } else {
          document.documentElement.classList.remove('dark');
          document.documentElement.style.colorScheme = 'light';
        }
      })();
    `
    document.head.appendChild(script)

    return () => {
      document.head.removeChild(script)
    }
  }, [])

  return null
}
