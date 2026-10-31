import api from './api';
import {
  customers as initialCustomers,
  subscriptions as initialSubscriptions,
  invoices as initialInvoices,
  payments as initialPayments,
  quotations as initialQuotations,
  plans as initialPlans,
  products as initialProducts,
  discounts as initialDiscounts,
  taxes as initialTaxes,
  aiInsights as initialAiInsights,
  costRecommendations as initialCostRecommendations,
  dunningData as initialDunningData,
  billingRuns as initialBillingRuns,
  staffMembers as initialStaff
} from '../data/mockData';

// Local storage backed stateful mock DB
const getStored = (key, fallback) => {
  const data = localStorage.getItem(`siq_${key}`);
  return data ? JSON.parse(data) : fallback;
};

const setStored = (key, val) => {
  localStorage.setItem(`siq_${key}`, JSON.stringify(val));
};

export const customerService = {
  getAll: async () => {
    return getStored('customers', initialCustomers);
  },
  getById: async (id) => {
    const list = getStored('customers', initialCustomers);
    return list.find(c => c.id === id) || null;
  },
  create: async (data) => {
    const list = getStored('customers', initialCustomers);
    const newCust = {
      id: `C00${list.length + 1}`,
      since: new Date().toISOString().split('T')[0],
      status: 'Active',
      ...data
    };
    list.unshift(newCust);
    setStored('customers', list);
    return newCust;
  },
  update: async (id, data) => {
    const list = getStored('customers', initialCustomers);
    const idx = list.findIndex(c => c.id === id);
    if (idx !== -1) {
      list[idx] = { ...list[idx], ...data };
      setStored('customers', list);
      return list[idx];
    }
    return null;
  },
  delete: async (id) => {
    const list = getStored('customers', initialCustomers);
    const filtered = list.filter(c => c.id !== id);
    setStored('customers', filtered);
    return true;
  }
};

export const subscriptionService = {
  getAll: async () => getStored('subscriptions', initialSubscriptions),
  getById: async (id) => {
    const list = getStored('subscriptions', initialSubscriptions);
    return list.find(s => s.id === id) || null;
  },
  create: async (data) => {
    const list = getStored('subscriptions', initialSubscriptions);
    const newSub = {
      id: `SUB00${list.length + 1}`,
      startDate: new Date().toISOString().split('T')[0],
      renewalDate: new Date(Date.now() + 365*24*60*60*1000).toISOString().split('T')[0],
      status: 'Active',
      usagePercent: 100,
      ...data
    };
    list.unshift(newSub);
    setStored('subscriptions', list);
    return newSub;
  },
  updateStatus: async (id, status) => {
    const list = getStored('subscriptions', initialSubscriptions);
    const idx = list.findIndex(s => s.id === id);
    if (idx !== -1) {
      list[idx].status = status;
      setStored('subscriptions', list);
      return list[idx];
    }
    return null;
  }
};

export const invoiceService = {
  getAll: async () => getStored('invoices', initialInvoices),
  getById: async (id) => {
    const list = getStored('invoices', initialInvoices);
    return list.find(i => i.id === id) || null;
  },
  create: async (data) => {
    const list = getStored('invoices', initialInvoices);
    const newInv = {
      id: `INV-2024-0${list.length + 1}`,
      date: new Date().toISOString().split('T')[0],
      dueDate: new Date(Date.now() + 15*24*60*60*1000).toISOString().split('T')[0],
      status: 'Pending',
      ...data
    };
    list.unshift(newInv);
    setStored('invoices', list);
    return newInv;
  }
};

export const paymentService = {
  getAll: async () => getStored('payments', initialPayments),
  create: async (data) => {
    const list = getStored('payments', initialPayments);
    const newPay = {
      id: `PAY-00${list.length + 1}`,
      date: new Date().toISOString().split('T')[0],
      status: 'Success',
      ...data
    };
    list.unshift(newPay);
    setStored('payments', list);
    return newPay;
  }
};

export const quotationService = {
  getAll: async () => getStored('quotations', initialQuotations),
  create: async (data) => {
    const list = getStored('quotations', initialQuotations);
    const newQt = {
      id: `QT-2024-${8890 + list.length + 1}`,
      status: 'Draft',
      validUntil: new Date(Date.now() + 30*24*60*60*1000).toISOString().split('T')[0],
      ...data
    };
    list.unshift(newQt);
    setStored('quotations', list);
    return newQt;
  }
};

export const planService = {
  getAll: async () => getStored('plans', initialPlans),
  create: async (data) => {
    const list = getStored('plans', initialPlans);
    const newPlan = { id: `PLAN-0${list.length + 1}`, status: 'Active', subscribers: 0, activeMRR: 0, ...data };
    list.push(newPlan);
    setStored('plans', list);
    return newPlan;
  }
};

export const productService = {
  getAll: async () => getStored('products', initialProducts),
  create: async (data) => {
    const list = getStored('products', initialProducts);
    const newProd = { id: `PROD-0${list.length + 1}`, status: 'Active', variantsCount: 1, ...data };
    list.push(newProd);
    setStored('products', list);
    return newProd;
  }
};

export const discountService = {
  getAll: async () => getStored('discounts', initialDiscounts),
  create: async (data) => {
    const list = getStored('discounts', initialDiscounts);
    const newDsc = { id: `DSC-0${list.length + 1}`, status: 'Active', usesCount: 0, ...data };
    list.push(newDsc);
    setStored('discounts', list);
    return newDsc;
  }
};

export const taxService = {
  getAll: async () => getStored('taxes', initialTaxes)
};

export const staffService = {
  getAll: async () => getStored('staff', initialStaff),
  create: async (data) => {
    const list = getStored('staff', initialStaff);
    const newStf = { id: `STF00${list.length + 1}`, status: 'Active', assignedCustomers: 0, ...data };
    list.push(newStf);
    setStored('staff', list);
    return newStf;
  }
};

export const aiService = {
  getInsights: async () => getStored('aiInsights', initialAiInsights),
  getRecommendations: async () => getStored('costRecommendations', initialCostRecommendations),
  applyRecommendation: async (id) => {
    const recs = getStored('costRecommendations', initialCostRecommendations);
    const filtered = recs.filter(r => r.id !== id);
    setStored('costRecommendations', filtered);
    return true;
  }
};

