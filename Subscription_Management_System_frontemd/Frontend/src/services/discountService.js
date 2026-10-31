import api from './api';

export const discountService = {
  getAll: async (params = {}) => {
    const page = params.page ?? 0;
    const size = params.size ?? 50;
    const status = params.status && params.status !== 'ALL' ? `?status=${params.status}&` : '?';
    const response = await api.get(`/discounts${status}page=${page}&size=${size}`);
    return response.data.data?.content || response.data.data || [];
  },

  getActive: async () => {
    const response = await api.get('/discounts/active');
    return response.data.data || [];
  },

  getById: async (id) => {
    const response = await api.get(`/discounts/${id}`);
    return response.data.data;
  },

  create: async (data) => {
    const response = await api.post('/discounts', {
      name: data.name,
      type: data.type || 'PERCENTAGE',
      value: parseFloat(data.value || data.amount),
    });
    return response.data.data;
  },

  update: async (id, data) => {
    const response = await api.put(`/discounts/${id}`, {
      name: data.name,
      type: data.type || 'PERCENTAGE',
      value: parseFloat(data.value || data.amount),
    });
    return response.data.data;
  },

  updateStatus: async (id, status) => {
    const response = await api.patch(`/discounts/${id}/status`, { status });
    return response.data.data;
  },
};

export default discountService;
