import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 15000,
});

// Request interceptor: Attach JWT token from localStorage
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('subscriptiq_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor: Global HTTP error handling
api.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response ? error.response.status : null;
    const data = error.response ? error.response.data : null;

    let friendlyMessage = 'An unexpected error occurred. Please try again.';

    if (data && data.message) {
      friendlyMessage = data.message;
    } else if (data && data.error) {
      friendlyMessage = data.error;
    }

    if (status === 401) {
      localStorage.removeItem('subscriptiq_token');
      localStorage.removeItem('subscriptiq_user');
      const currentPath = window.location.pathname;
      const publicPaths = ['/login', '/register', '/forgot-password', '/reset-password', '/'];
      if (!publicPaths.includes(currentPath)) {
        window.location.href = '/login?session_expired=true';
      }
      friendlyMessage = friendlyMessage || 'Session expired or unauthenticated. Please log in.';
    } else if (status === 403) {
      friendlyMessage = 'You do not have permission to access this resource or perform this action.';
    } else if (status === 404) {
      friendlyMessage = friendlyMessage || 'The requested resource was not found.';
    } else if (status === 409) {
      friendlyMessage = friendlyMessage || 'Conflict: The resource already exists or state is invalid.';
    } else if (status === 400 || status === 422) {
      if (data && data.validationErrors) {
        const errorDetails = Object.entries(data.validationErrors)
          .map(([field, msg]) => `${field}: ${msg}`)
          .join(', ');
        friendlyMessage = `Validation failed - ${errorDetails}`;
      } else {
        friendlyMessage = friendlyMessage || 'Invalid request parameters.';
      }
    } else if (status >= 500) {
      friendlyMessage = 'Internal server error. Please try again later or contact support.';
    }

    // Attach enriched error properties for UI consumption
    error.userMessage = friendlyMessage;
    error.validationErrors = data?.validationErrors || null;
    error.statusCode = status;

    return Promise.reject(error);
  }
);

export default api;
