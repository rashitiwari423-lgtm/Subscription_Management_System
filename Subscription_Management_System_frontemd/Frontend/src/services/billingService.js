import api from './api';

export const billingService = {
  triggerRecurringBilling: async () => {
    const response = await api.post('/admin/billing/run');
    return response.data.data;
  },
};

export default billingService;

