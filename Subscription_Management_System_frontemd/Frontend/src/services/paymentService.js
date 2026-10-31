import api from './api';

export const paymentService = {
  getAll: async (params = {}) => {
    const page = params.page ?? 0;
    const size = params.size ?? 50;
    const response = await api.get(`/payments?page=${page}&size=${size}`);
    return response.data.data?.content || response.data.data || [];
  },

  getPaged: async (params = {}) => {
    const page = params.page ?? 0;
    const size = params.size ?? 10;
    const response = await api.get(`/payments?page=${page}&size=${size}`);
    return response.data.data;
  },

  getById: async (id) => {
    const response = await api.get(`/payments/${id}`);
    return response.data.data;
  },

  getByInvoiceId: async (invoiceId, page = 0, size = 10) => {
    const response = await api.get(`/invoices/${invoiceId}/payments?page=${page}&size=${size}`);
    return response.data.data?.content || response.data.data || [];
  },

  getByCustomerId: async (customerId, page = 0, size = 10) => {
    const response = await api.get(`/customers/${customerId}/payments?page=${page}&size=${size}`);
    return response.data.data?.content || response.data.data || [];
  },

  record: async (invoiceId, data) => {
    const payload = {
      amount: data.amount,
      paymentDate: data.paymentDate || new Date().toISOString(),
      paymentMethod: data.paymentMethod || data.method || 'BANK_TRANSFER',
      transactionReference: data.transactionReference || data.reference || null,
      notes: data.notes || '',
    };
    const response = await api.post(`/invoices/${invoiceId}/payments`, payload);
    return response.data.data;
  },

  // Alias for backward compatibility
  create: async (data) => {
    const invoiceId = data.invoiceId;
    if (!invoiceId) throw new Error('Invoice ID is required to record a payment');
    return paymentService.record(invoiceId, data);
  },
};

export default paymentService;
