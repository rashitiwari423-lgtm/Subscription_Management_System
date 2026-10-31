import api from './api';

export const invoiceService = {
  getAll: async (params = {}) => {
    const page = params.page ?? 0;
    const size = params.size ?? 50;
    const status = params.status && params.status !== 'ALL' ? `?status=${params.status}&` : '?';
    const response = await api.get(`/invoices${status}page=${page}&size=${size}`);
    return response.data.data?.content || response.data.data || [];
  },

  getPaged: async (params = {}) => {
    const page = params.page ?? 0;
    const size = params.size ?? 10;
    const status = params.status && params.status !== 'ALL' ? `?status=${params.status}&` : '?';
    const response = await api.get(`/invoices${status}page=${page}&size=${size}`);
    return response.data.data;
  },

  getById: async (id) => {
    const response = await api.get(`/invoices/${id}`);
    return response.data.data;
  },

  getByCustomerId: async (customerId, page = 0, size = 10) => {
    const response = await api.get(`/customers/${customerId}/invoices?page=${page}&size=${size}`);
    return response.data.data?.content || response.data.data || [];
  },

  create: async (data) => {
    const payload = {
      customerId: data.customerId,
      subscriptionId: data.subscriptionId || null,
      invoiceDate: data.invoiceDate || new Date().toISOString().split('T')[0],
      dueDate: data.dueDate || new Date(Date.now() + 7 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
      billingPeriodStart: data.billingPeriodStart || null,
      billingPeriodEnd: data.billingPeriodEnd || null,
      notes: data.notes || '',
      items: (data.items || []).map(item => ({
        planId: item.planId || null,
        description: item.description || item.name || 'Subscription Service',
        quantity: item.quantity || 1,
        unitPrice: item.unitPrice,
        discountAmount: item.discountAmount || 0,
        taxAmount: item.taxAmount || 0,
      })),
    };
    const response = await api.post('/invoices', payload);
    return response.data.data;
  },

  updateStatus: async (id, status) => {
    const response = await api.patch(`/invoices/${id}/status`, { status });
    return response.data.data;
  },

  voidInvoice: async (id) => {
    const response = await api.patch(`/invoices/${id}/void`);
    return response.data.data;
  },
};

export default invoiceService;
