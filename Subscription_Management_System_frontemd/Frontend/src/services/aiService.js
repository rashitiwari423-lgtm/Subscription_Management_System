import api from './api';

export const aiService = {
  ask: async (question) => {
    const response = await api.post('/ai/ask', { question });
    return response.data.data;
  },

  getInsights: async (params = {}) => {
    const page = params.page ?? 0;
    const size = params.size ?? 50;
    const severity = params.severity && params.severity !== 'ALL' ? `&severity=${params.severity}` : '';
    const status = params.status && params.status !== 'ALL' ? `&status=${params.status}` : '';
    const type = params.type ? `&type=${encodeURIComponent(params.type)}` : '';
    const response = await api.get(`/ai/insights?page=${page}&size=${size}${severity}${status}${type}`);
    return response.data.data?.content || response.data.data || [];
  },

  getPagedInsights: async (params = {}) => {
    const page = params.page ?? 0;
    const size = params.size ?? 10;
    const severity = params.severity && params.severity !== 'ALL' ? `&severity=${params.severity}` : '';
    const status = params.status && params.status !== 'ALL' ? `&status=${params.status}` : '';
    const type = params.type ? `&type=${encodeURIComponent(params.type)}` : '';
    const response = await api.get(`/ai/insights?page=${page}&size=${size}${severity}${status}${type}`);
    return response.data.data;
  },

  getInsightById: async (id) => {
    const response = await api.get(`/ai/insights/${id}`);
    return response.data.data;
  },

  updateInsightStatus: async (id, status) => {
    const response = await api.patch(`/ai/insights/${id}/status`, { status });
    return response.data.data;
  },

  // Controlled AI Business Actions
  previewAction: async (insightId, actionType) => {
    const response = await api.post('/ai/actions/preview', {
      insightId,
      actionType,
      idempotencyKey: `act-prev-${insightId}-${Date.now()}`,
    });
    return response.data.data;
  },

  approveAction: async (actionId, approvalNotes = '') => {
    const response = await api.post(`/ai/actions/${actionId}/approve`, {
      approvalNotes,
    });
    return response.data.data;
  },

  rejectAction: async (actionId, rejectionReason = '') => {
    const response = await api.post(`/ai/actions/${actionId}/reject`, {
      rejectionReason,
    });
    return response.data.data;
  },

  executeAction: async (actionId, executionNotes = '') => {
    const response = await api.post(`/ai/actions/${actionId}/execute`, {
      executionNotes,
      idempotencyKey: `exec-${actionId}-${Date.now()}`,
    });
    return response.data.data;
  },

  getActions: async (params = {}) => {
    const page = params.page ?? 0;
    const size = params.size ?? 50;
    const actionType = params.actionType ? `&actionType=${params.actionType}` : '';
    const status = params.status ? `&status=${params.status}` : '';
    const response = await api.get(`/ai/actions?page=${page}&size=${size}${actionType}${status}`);
    return response.data.data?.content || response.data.data || [];
  },

  getActionById: async (id) => {
    const response = await api.get(`/ai/actions/${id}`);
    return response.data.data;
  },

  getActionHistory: async (params = {}) => {
    const page = params.page ?? 0;
    const size = params.size ?? 50;
    const response = await api.get(`/ai/actions/history?page=${page}&size=${size}`);
    return response.data.data?.content || response.data.data || [];
  },
};

export default aiService;
