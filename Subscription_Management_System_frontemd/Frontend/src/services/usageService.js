import api from './api';

export const usageService = {
  getAll: async (params = {}) => {
    const page = params.page ?? 0;
    const size = params.size ?? 50;
    const response = await api.get(`/usage?page=${page}&size=${size}`);
    return response.data.data?.content || response.data.data || [];
  },

  getPaged: async (params = {}) => {
    const page = params.page ?? 0;
    const size = params.size ?? 10;
    const response = await api.get(`/usage?page=${page}&size=${size}`);
    return response.data.data;
  },

  getById: async (id) => {
    const response = await api.get(`/usage/${id}`);
    return response.data.data;
  },

  getByCustomerId: async (customerId, page = 0, size = 10) => {
    const response = await api.get(`/usage/customer/${customerId}?page=${page}&size=${size}`);
    return response.data.data?.content || response.data.data || [];
  },

  getBySubscriptionId: async (subscriptionId, page = 0, size = 10) => {
    const response = await api.get(`/usage/subscription/${subscriptionId}?page=${page}&size=${size}`);
    return response.data.data?.content || response.data.data || [];
  },

  record: async (data) => {
    const payload = {
      customerId: data.customerId,
      subscriptionId: data.subscriptionId,
      resourceType: data.resourceType || 'LICENSE',
      resourceName: data.resourceName || data.name,
      allocatedQuantity: parseInt(data.allocatedQuantity || data.allocated, 10),
      usedQuantity: parseInt(data.usedQuantity || data.used, 10),
      unitCost: data.unitCost || 0,
      snapshotDate: data.snapshotDate || new Date().toISOString().split('T')[0],
    };
    const response = await api.post('/usage', payload);
    return response.data.data;
  },
};

export default usageService;

