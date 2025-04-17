// Base URL for backend API
export const API_BASE_URL = 'http://localhost:8082';

// Default request timeout in milliseconds
export const REQUEST_TIMEOUT = 10000;

// Common headers for API requests
export const DEFAULT_HEADERS = {
  'Content-Type': 'application/json',
  'Accept': 'application/json',
};

// Handle API errors
export function handleApiError(error: any): never {
  console.error('API Error:', error);
  
  if (error.response) {
    // The request was made and the server responded with a status code
    // that falls out of the range of 2xx
    throw new Error(error.response.data?.message || `Error ${error.response.status}: ${error.response.statusText}`);
  } else if (error.request) {
    // The request was made but no response was received
    throw new Error('No response received from server. Please check your connection.');
  } else {
    // Something happened in setting up the request that triggered an Error
    throw new Error(error.message || 'An unknown error occurred');
  }
} 