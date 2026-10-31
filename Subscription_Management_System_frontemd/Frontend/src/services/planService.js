import api from './api';

export const planService = {
  getAll: async (params = {}) => {
    const page = params.page ?? 0;
    const size = params.size ?? 50;
    const status = params.status && params.status !== 'ALL' ? `?status=${params.status}&` : '?';
    const response = await api.get(`/plans${status}page=${page}&size=${size}`);
    return response.data.data?.content || response.data.data || [];
  },

  getPaged: async (params = {}) => {
    const page = params.page ?? 0;
    const size = params.size ?? 10;
    const status = params.status && params.status !== 'ALL' ? `?status=${params.status}&` : '?';
    const response = await api.get(`/plans${status}page=${page}&size=${size}`);
    return response.data.data;
  },

  getById: async (id) => {
    const response = await api.get(`/plans/${id}`);
    return response.data.data;
  },

  getByVariantId: async (variantId) => {
    const response = await api.get(`/variants/${variantId}/plans`);
    return response.data.data || [];
  },

  create: async (variantId, data) => {
    const response = await api.post(`/variants/${variantId}/plans`, {
      name: data.name,
      description: data.description || '',
      billingCycle: data.billingCycle || 'MONTHLY',
      price: data.price,
      currency: data.currency || 'INR',
      trialDays: data.trialDays ? parseInt(data.trialDays, 10) : 0,
    });
    return response.data.data;
  },

  update: async (id, data) => {
    const response = await api.put(`/plans/${id}`, {
      name: data.name,
      description: data.description || '',
      billingCycle: data.billingCycle || 'MONTHLY',
      price: data.price,
      currency: data.currency || 'INR',
      trialDays: data.trialDays ? parseInt(data.trialDays, 10) : 0,
    });
    return response.data.data;
  },

  updateStatus: async (id, status) => {
    const response = await api.patch(`/plans/${id}/status`, { status });
    return response.data.data;
  },
};

export default planService;
