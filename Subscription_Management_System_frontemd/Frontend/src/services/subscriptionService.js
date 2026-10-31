import api from './api';

export const subscriptionService = {
  getAll: async (params = {}) => {
    const page = params.page ?? 0;
    const size = params.size ?? 50;
    const search = params.search ? `&search=${encodeURIComponent(params.search)}` : '';
    const status = params.status && params.status !== 'ALL' ? `&status=${params.status}` : '';
    const response = await api.get(`/subscriptions?page=${page}&size=${size}${search}${status}`);
    return response.data.data?.content || response.data.data || [];
  },

  getPaged: async (params = {}) => {
    const page = params.page ?? 0;
    const size = params.size ?? 10;
    const search = params.search ? `&search=${encodeURIComponent(params.search)}` : '';
    const status = params.status && params.status !== 'ALL' ? `&status=${params.status}` : '';
    const response = await api.get(`/subscriptions?page=${page}&size=${size}${search}${status}`);
    return response.data.data;
  },

  getById: async (id) => {
    const response = await api.get(`/subscriptions/${id}`);
    return response.data.data;
  },

  getByCustomerId: async (customerId, page = 0, size = 10) => {
    const response = await api.get(`/customers/${customerId}/subscriptions?page=${page}&size=${size}`);
    return response.data.data?.content || response.data.data || [];
  },

  pause: async (id) => {
    const response = await api.patch(`/subscriptions/${id}/pause`);
    return response.data.data;
  },

  resume: async (id) => {
    const response = await api.patch(`/subscriptions/${id}/resume`);
    return response.data.data;
  },

  close: async (id) => {
    const response = await api.patch(`/subscriptions/${id}/close`);
    return response.data.data;
  },

  renew: async (id) => {
    const response = await api.patch(`/subscriptions/${id}/renew`);
    return response.data.data;
  },

  createInvoice: async (id) => {
    const response = await api.post(`/subscriptions/${id}/invoice`);
    return response.data.data;
  },
};

export default subscriptionService;
