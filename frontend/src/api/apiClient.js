import axios from 'axios';

/**
 * Central API client — the single point of contact with the backend.
 *
 * DECOUPLED FRONTEND DESIGN:
 * All API calls go through this client. The base URL comes from a single
 * environment variable: VITE_API_BASE_URL
 *
 * To switch backends (e.g., dev → prod, or swap entirely to a new backend):
 *   1. Change VITE_API_BASE_URL in your .env file
 *   2. Rebuild the frontend
 *   3. Zero changes to backend code needed
 *
 * To replace this frontend entirely (e.g., with Angular or a mobile app):
 *   Point the new app at the same VITE_API_BASE_URL — all endpoints are versioned
 *   under /api/v1/ and documented at /swagger-ui.html
 */
const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 30000,
});

// Response interceptor — unwrap the ApiResponse<T> envelope
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    console.error('API error:', error.response?.data || error.message);
    return Promise.reject(error);
  }
);

export default apiClient;
