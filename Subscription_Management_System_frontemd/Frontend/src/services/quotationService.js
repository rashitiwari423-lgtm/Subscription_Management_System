import api from './api';

export const quotationService = {
  getAll: async (params = {}) => {
    const page = params.page ?? 0;
    const size = params.size ?? 50;
    const search = params.search ? `&search=${encodeURIComponent(params.search)}` : '';
    const status = params.status && params.status !== 'ALL' ? `&status=${params.status}` : '';
    const response = await api.get(`/quotations?page=${page}&size=${size}${search}${status}`);
    return response.data.data?.content || response.data.data || [];
  },

  getPaged: async (params = {}) => {
    const page = params.page ?? 0;
    const size = params.size ?? 10;
    const search = params.search ? `&search=${encodeURIComponent(params.search)}` : '';
    const status = params.status && params.status !== 'ALL' ? `&status=${params.status}` : '';
    const response = await api.get(`/quotations?page=${page}&size=${size}${search}${status}`);
    return response.data.data;
  },

  getById: async (id) => {
    const response = await api.get(`/quotations/${id}`);
    return response.data.data;
  },

  create: async (data) => {
    // Format payload to match CreateQuotationRequest exactly
    const payload = {
      customerId: data.customerId,
      quotationDate: data.quotationDate || new Date().toISOString().split('T')[0],
      validUntil: data.validUntil || new Date(Date.now() + 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
      notes: data.notes || '',
      items: data.items.map(item => ({
        planId: item.planId,
        quantity: item.quantity || 1,
        discountAmount: item.discountAmount || 0,
        taxAmount: item.taxAmount || 0,
      })),
    };
    const response = await api.post('/quotations', payload);
    return response.data.data;
  },

  update: async (id, data) => {
    const payload = {
      quotationDate: data.quotationDate,
      validUntil: data.validUntil,
      notes: data.notes || '',
      items: data.items.map(item => ({
        planId: item.planId,
        quantity: item.quantity || 1,
        discountAmount: item.discountAmount || 0,
        taxAmount: item.taxAmount || 0,
      })),
    };
    const response = await api.put(`/quotations/${id}`, payload);
    return response.data.data;
  },

  updateStatus: async (id, status) => {
    const response = await api.patch(`/quotations/${id}/status`, { status });
    return response.data.data;
  },

  convert: async (id) => {
    const response = await api.post(`/quotations/${id}/convert`);
    return response.data.data;
  },
};

export default quotationService;
