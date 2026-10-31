import api from './api';

export const productService = {
  getAll: async (params = {}) => {
    const page = params.page ?? 0;
    const size = params.size ?? 50;
    const search = params.search ? `&search=${encodeURIComponent(params.search)}` : '';
    const category = params.category ? `&category=${encodeURIComponent(params.category)}` : '';
    const status = params.status && params.status !== 'ALL' ? `&status=${params.status}` : '';
    const response = await api.get(`/products?page=${page}&size=${size}${search}${category}${status}`);
    return response.data.data?.content || response.data.data || [];
  },

  getPaged: async (params = {}) => {
    const page = params.page ?? 0;
    const size = params.size ?? 10;
    const search = params.search ? `&search=${encodeURIComponent(params.search)}` : '';
    const category = params.category ? `&category=${encodeURIComponent(params.category)}` : '';
    const status = params.status && params.status !== 'ALL' ? `&status=${params.status}` : '';
    const response = await api.get(`/products?page=${page}&size=${size}${search}${category}${status}`);
    return response.data.data;
  },

  getById: async (id) => {
    const response = await api.get(`/products/${id}`);
    return response.data.data;
  },

  create: async (data) => {
    const response = await api.post('/products', {
      name: data.name,
      description: data.description || '',
      category: data.category || 'Subscription Software',
    });
    return response.data.data;
  },

  update: async (id, data) => {
    const response = await api.put(`/products/${id}`, {
      name: data.name,
      description: data.description || '',
      category: data.category || 'Subscription Software',
    });
    return response.data.data;
  },

  updateStatus: async (id, status) => {
    const response = await api.patch(`/products/${id}/status`, { status });
    return response.data.data;
  },

  // Product Variants
  getVariantsByProductId: async (productId) => {
    const response = await api.get(`/products/${productId}/variants`);
    return response.data.data || [];
  },

  getVariantById: async (id) => {
    const response = await api.get(`/variants/${id}`);
    return response.data.data;
  },

  createVariant: async (productId, data) => {
    const response = await api.post(`/products/${productId}/variants`, {
      name: data.name,
      sku: data.sku,
      description: data.description || '',
    });
    return response.data.data;
  },

  updateVariant: async (id, data) => {
    const response = await api.put(`/variants/${id}`, {
      name: data.name,
      sku: data.sku,
      description: data.description || '',
    });
    return response.data.data;
  },

  updateVariantStatus: async (id, status) => {
    const response = await api.patch(`/variants/${id}/status`, { status });
    return response.data.data;
  },
};

export default productService;
