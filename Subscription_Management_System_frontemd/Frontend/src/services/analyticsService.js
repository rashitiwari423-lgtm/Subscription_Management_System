import api from './api';

export const analyticsService = {
  getOverview: async () => {
    const response = await api.get('/analytics/overview');
    return response.data.data;
  },

  getDashboard: async () => {
    const response = await api.get('/analytics/dashboard');
    return response.data.data;
  },

  getRevenue: async (params = {}) => {
    const query = new URLSearchParams(params).toString();
    const response = await api.get(`/analytics/revenue${query ? '?' + query : ''}`);
    return response.data.data;
  },

  getSubscriptions: async (params = {}) => {
    const query = new URLSearchParams(params).toString();
    const response = await api.get(`/analytics/subscriptions${query ? '?' + query : ''}`);
    return response.data.data;
  },

  getCustomers: async (params = {}) => {
    const query = new URLSearchParams(params).toString();
    const response = await api.get(`/analytics/customers${query ? '?' + query : ''}`);
    return response.data.data;
  },

  getPayments: async (params = {}) => {
    const query = new URLSearchParams(params).toString();
    const response = await api.get(`/analytics/payments${query ? '?' + query : ''}`);
    return response.data.data;
  },

  getChurn: async (params = {}) => {
    const query = new URLSearchParams(params).toString();
    const response = await api.get(`/analytics/churn${query ? '?' + query : ''}`);
    return response.data.data;
  },

  getForecast: async (months = 3, methodology = 'RECENT_MONTHLY_AVERAGE') => {
    const response = await api.get(`/analytics/forecast?months=${months}&methodology=${methodology}`);
    return response.data.data;
  },

  getPlans: async (params = {}) => {
    const query = new URLSearchParams(params).toString();
    const response = await api.get(`/analytics/plans${query ? '?' + query : ''}`);
    return response.data.data;
  },

  getOptimization: async () => {
    const response = await api.get('/analytics/optimization');
    return response.data.data;
  },
};

export default analyticsService;
