import api from './api';

export const customerService = {
  // Paginated list with search and status filters
  getAll: async (params = {}) => {
    const page = params.page ?? 0;
    const size = params.size ?? 50;
    const search = params.search ? `&search=${encodeURIComponent(params.search)}` : '';
    const status = params.status && params.status !== 'ALL' ? `&status=${params.status}` : '';
    const response = await api.get(`/customers?page=${page}&size=${size}${search}${status}`);
    // Return array or paged content
    return response.data.data?.content || response.data.data || [];
  },

  getPaged: async (params = {}) => {
    const page = params.page ?? 0;
    const size = params.size ?? 10;
    const search = params.search ? `&search=${encodeURIComponent(params.search)}` : '';
    const status = params.status && params.status !== 'ALL' ? `&status=${params.status}` : '';
    const response = await api.get(`/customers?page=${page}&size=${size}${search}${status}`);
    return response.data.data;
  },

  getById: async (id) => {
    const response = await api.get(`/customers/${id}`);
    return response.data.data;
  },

  create: async (data) => {
    const response = await api.post('/customers', {
      userId: data.userId || null,
      companyName: data.companyName || data.name || data.company,
      contactName: data.contactName || data.contactPerson || data.name,
      email: data.email,
      phone: data.phone || null,
      address: data.address || null,
      city: data.city || null,
      state: data.state || null,
      country: data.country || 'India',
    });
    return response.data.data;
  },

  update: async (id, data) => {
    const response = await api.put(`/customers/${id}`, {
      companyName: data.companyName || data.name || data.company,
      contactName: data.contactName || data.contactPerson || data.name,
      email: data.email,
      phone: data.phone || null,
      address: data.address || null,
      city: data.city || null,
      state: data.state || null,
      country: data.country || 'India',
    });
    return response.data.data;
  },

  updateStatus: async (id, status) => {
    const response = await api.patch(`/customers/${id}/status`, { status });
    return response.data.data;
  },

  getQuotations: async (customerId, page = 0, size = 10) => {
    const response = await api.get(`/customers/${customerId}/quotations?page=${page}&size=${size}`);
    return response.data.data?.content || response.data.data || [];
  },

  getSubscriptions: async (customerId, page = 0, size = 10) => {
    const response = await api.get(`/customers/${customerId}/subscriptions?page=${page}&size=${size}`);
    return response.data.data?.content || response.data.data || [];
  },

  getInvoices: async (customerId, page = 0, size = 10) => {
    const response = await api.get(`/customers/${customerId}/invoices?page=${page}&size=${size}`);
    return response.data.data?.content || response.data.data || [];
  },

  getPayments: async (customerId, page = 0, size = 10) => {
    const response = await api.get(`/customers/${customerId}/payments?page=${page}&size=${size}`);
    return response.data.data?.content || response.data.data || [];
  },
};

export default customerService;
