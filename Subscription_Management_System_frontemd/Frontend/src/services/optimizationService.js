import api from './api';

export const optimizationService = {
  analyze: async () => {
    const response = await api.post('/optimization/analyze');
    return response.data.data || [];
  },

  getInsights: async (params = {}) => {
    const page = params.page ?? 0;
    const size = params.size ?? 50;
    const response = await api.get(`/optimization/insights?page=${page}&size=${size}`);
    return response.data.data?.content || response.data.data || [];
  },

  getPagedInsights: async (params = {}) => {
    const page = params.page ?? 0;
    const size = params.size ?? 10;
    const response = await api.get(`/optimization/insights?page=${page}&size=${size}`);
    return response.data.data;
  },

  getInsightById: async (id) => {
    const response = await api.get(`/optimization/insights/${id}`);
    return response.data.data;
  },

  preview: async (id) => {
    const response = await api.post(`/optimization/insights/${id}/preview`);
    return response.data.data;
  },

  approve: async (id) => {
    const response = await api.post(`/optimization/insights/${id}/approve`);
    return response.data.data;
  },

  reject: async (id, reason = '') => {
    const query = reason ? `?reason=${encodeURIComponent(reason)}` : '';
    const response = await api.post(`/optimization/insights/${id}/reject${query}`);
    return response.data.data;
  },

  getHistory: async (params = {}) => {
    const page = params.page ?? 0;
    const size = params.size ?? 50;
    const response = await api.get(`/optimization/history?page=${page}&size=${size}`);
    return response.data.data?.content || response.data.data || [];
  },
};

export default optimizationService;

