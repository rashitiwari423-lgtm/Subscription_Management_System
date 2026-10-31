import api from './api';

export const taxService = {
  getAll: async (params = {}) => {
    const page = params.page ?? 0;
    const size = params.size ?? 50;
    const status = params.status && params.status !== 'ALL' ? `?status=${params.status}&` : '?';
    const response = await api.get(`/taxes${status}page=${page}&size=${size}`);
    return response.data.data?.content || response.data.data || [];
  },

  getActive: async () => {
    const response = await api.get('/taxes/active');
    return response.data.data || [];
  },

  getById: async (id) => {
    const response = await api.get(`/taxes/${id}`);
    return response.data.data;
  },

  create: async (data) => {
    const response = await api.post('/taxes', {
      name: data.name,
      percentage: parseFloat(data.percentage || data.rate),
    });
    return response.data.data;
  },

  update: async (id, data) => {
    const response = await api.put(`/taxes/${id}`, {
      name: data.name,
      percentage: parseFloat(data.percentage || data.rate),
    });
    return response.data.data;
  },

  updateStatus: async (id, status) => {
    const response = await api.patch(`/taxes/${id}/status`, { status });
    return response.data.data;
  },
};

export default taxService;
