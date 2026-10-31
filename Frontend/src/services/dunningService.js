import { invoiceService, paymentService, customerService } from './appServices';

const DUNNING_STORAGE_KEY = 'siq_dunning_cases';
const DUNNING_RULES_KEY = 'siq_dunning_rules';

const DEFAULT_RULES = {
  firstReminderDays: 3,
  secondReminderDays: 7,
  finalReminderDays: 14,
  escalationDays: 21,
  autoRetryEnabled: true,
  retryIntervalDays: 3,
  maxRetryAttempts: 4,
};

const INITIAL_CASES = [
  {
    id: 'DUN-001',
    invoiceId: 'INV-2024-003',
    customer: 'Vertex Labs',
    customerId: 'C003',
    invoiceAmount: 32000,
    paidAmount: 0,
    outstandingAmount: 32000,
    dueDate: '2024-01-15',
    daysOverdue: 74,
    recoveryStatus: 'Escalated', // Upcoming | Reminder Sent | Payment Promise | Partially Recovered | Recovered | Escalated
    failureReason: 'Insufficient Funds',
    retryCount: 3,
    lastReminderDate: '2024-03-05',
    nextReminderDate: '2024-03-12',
    paymentPromiseDate: null,
    notes: 'Escalated to VP Finance. Customer requested 10-day invoice extension.',
    remindersHistory: [
      { id: 'REM-1', type: 'Email', date: '2024-01-18', label: 'First Overdue Notice', status: 'Delivered (Simulated)' },
      { id: 'REM-2', type: 'Email', date: '2024-01-22', label: 'Second Warning (Urgent)', status: 'Delivered (Simulated)' },
      { id: 'REM-3', type: 'Notification', date: '2024-03-05', label: 'Automated Account Hold Alert', status: 'Dispatched (Simulated)' },
    ]
  },
  {
    id: 'DUN-002',
    invoiceId: 'INV-2024-008',
    customer: 'Vertex Labs',
    customerId: 'C003',
    invoiceAmount: 32000,
    paidAmount: 0,
    outstandingAmount: 32000,
    dueDate: '2024-02-15',
    daysOverdue: 44,
    recoveryStatus: 'Reminder Sent',
    failureReason: 'Recurring AutoPay Mandate Failed',
    retryCount: 2,
    lastReminderDate: '2024-02-22',
    nextReminderDate: '2024-03-08',
    paymentPromiseDate: null,
    notes: 'Second cycle failed. Recommended card updater.',
    remindersHistory: [
      { id: 'REM-4', type: 'Email', date: '2024-02-18', label: 'Payment Failed Notification', status: 'Delivered (Simulated)' },
      { id: 'REM-5', type: 'Email', date: '2024-02-22', label: 'AutoPay Mandate Retry Warning', status: 'Delivered (Simulated)' },
    ]
  },
  {
    id: 'DUN-003',
    invoiceId: 'INV-2024-006',
    customer: 'Orion Fintech',
    customerId: 'C007',
    invoiceAmount: 22000,
    paidAmount: 10000,
    outstandingAmount: 12000,
    dueDate: '2024-02-15',
    daysOverdue: 44,
    recoveryStatus: 'Partially Recovered',
    failureReason: 'Partial Corporate RTGS Settlement',
    retryCount: 1,
    lastReminderDate: '2024-02-28',
    nextReminderDate: '2024-03-10',
    paymentPromiseDate: '2024-03-15',
    notes: 'Paid ₹10,000 on Feb 28. Balance promised by March 15.',
    remindersHistory: [
      { id: 'REM-6', type: 'Email', date: '2024-02-18', label: 'Overdue Statement', status: 'Delivered (Simulated)' },
      { id: 'REM-7', type: 'Email', date: '2024-02-28', label: 'Partial Payment Receipt & Balance Notice', status: 'Delivered (Simulated)' },
    ]
  },
  {
    id: 'DUN-004',
    invoiceId: 'INV-2024-009',
    customer: 'DataPulse Analytics',
    customerId: 'C005',
    invoiceAmount: 15000,
    paidAmount: 0,
    outstandingAmount: 15000,
    dueDate: '2024-02-28',
    daysOverdue: 31,
    recoveryStatus: 'Payment Promise',
    failureReason: 'Corporate Card Expired',
    retryCount: 2,
    lastReminderDate: '2024-03-02',
    nextReminderDate: '2024-03-09',
    paymentPromiseDate: '2024-03-12',
    notes: 'Finance controller confirmed replacement corporate card issued.',
    remindersHistory: [
      { id: 'REM-8', type: 'Email', date: '2024-03-02', label: 'Expired Payment Method Alert', status: 'Delivered (Simulated)' },
    ]
  },
  {
    id: 'DUN-005',
    invoiceId: 'INV-2024-010',
    customer: 'ByteForge',
    customerId: 'C008',
    invoiceAmount: 3800,
    paidAmount: 3800,
    outstandingAmount: 0,
    dueDate: '2024-02-01',
    daysOverdue: 0,
    recoveryStatus: 'Recovered',
    failureReason: 'Initial Gateway Timeout',
    retryCount: 2,
    lastReminderDate: '2024-02-04',
    nextReminderDate: null,
    paymentPromiseDate: null,
    notes: 'Recovered in full via automated secondary retry.',
    remindersHistory: [
      { id: 'REM-9', type: 'Email', date: '2024-02-03', label: 'Payment Retry Triggered', status: 'Delivered (Simulated)' },
      { id: 'REM-10', type: 'Email', date: '2024-02-04', label: 'Settlement Confirmation', status: 'Delivered (Simulated)' },
    ]
  }
];

const getStoredCases = () => {
  try {
    const data = localStorage.getItem(DUNNING_STORAGE_KEY);
    return data ? JSON.parse(data) : INITIAL_CASES;
  } catch {
    return INITIAL_CASES;
  }
};

const setStoredCases = (cases) => {
  localStorage.setItem(DUNNING_STORAGE_KEY, JSON.stringify(cases));
};

export const dunningService = {
  getCases: async () => {
    return getStoredCases();
  },

  getCaseById: async (id) => {
    const cases = getStoredCases();
    return cases.find(c => c.id === id) || null;
  },

  getStats: async () => {
    const cases = getStoredCases();
    const activeCases = cases.filter(c => c.recoveryStatus !== 'Recovered');
    const recoveredCases = cases.filter(c => c.recoveryStatus === 'Recovered');

    const totalOverdueInvoices = activeCases.length;
    const totalOutstandingAmount = activeCases.reduce((sum, c) => sum + (c.outstandingAmount || 0), 0);
    const uniqueOverdueCustomers = new Set(activeCases.map(c => c.customer)).size;
    const amountRecovered = cases.reduce((sum, c) => sum + (c.paidAmount || 0), 0);
    const totalInvoicedInDunning = cases.reduce((sum, c) => sum + (c.invoiceAmount || 0), 0);
    const recoveryRate = totalInvoicedInDunning > 0
      ? Math.round((amountRecovered / totalInvoicedInDunning) * 100)
      : 88;
    const failedPaymentCount = activeCases.reduce((sum, c) => sum + (c.retryCount || 1), 0);
    const upcomingReminders = activeCases.filter(c => c.nextReminderDate !== null).length;

    return {
      totalOverdueInvoices,
      totalOutstandingAmount,
      uniqueOverdueCustomers,
      amountRecovered,
      recoveryRate,
      failedPaymentCount,
      upcomingReminders,
      totalCasesCount: cases.length
    };
  },

  sendSimulatedReminder: async (caseId, type = 'Email') => {
    const cases = getStoredCases();
    const idx = cases.findIndex(c => c.id === caseId);
    if (idx === -1) return null;

    const current = cases[idx];
    const newReminder = {
      id: `REM-${Date.now().toString().slice(-4)}`,
      type,
      date: new Date().toISOString().split('T')[0],
      label: `Simulated ${type} Overdue Notice`,
      status: 'Delivered (Simulated)'
    };

    const updated = {
      ...current,
      recoveryStatus: current.recoveryStatus === 'Upcoming' ? 'Reminder Sent' : current.recoveryStatus,
      lastReminderDate: newReminder.date,
      remindersHistory: [newReminder, ...(current.remindersHistory || [])]
    };

    cases[idx] = updated;
    setStoredCases(cases);
    return updated;
  },

  setPaymentPromise: async (caseId, promiseDate, notes) => {
    const cases = getStoredCases();
    const idx = cases.findIndex(c => c.id === caseId);
    if (idx === -1) return null;

    const updated = {
      ...cases[idx],
      recoveryStatus: 'Payment Promise',
      paymentPromiseDate: promiseDate,
      notes: notes ? `${cases[idx].notes ? cases[idx].notes + ' • ' : ''}${notes}` : cases[idx].notes
    };

    cases[idx] = updated;
    setStoredCases(cases);
    return updated;
  },

  escalateCase: async (caseId, notes) => {
    const cases = getStoredCases();
    const idx = cases.findIndex(c => c.id === caseId);
    if (idx === -1) return null;

    const updated = {
      ...cases[idx],
      recoveryStatus: 'Escalated',
      notes: notes ? `[ESCALATED]: ${notes}` : `[ESCALATED]: Legal & Credit hold flagged by operations.`
    };

    cases[idx] = updated;
    setStoredCases(cases);
    return updated;
  },

  recordPartialOrFullPayment: async (caseId, paymentAmount, paymentMethod = 'Bank Transfer') => {
    const cases = getStoredCases();
    const idx = cases.findIndex(c => c.id === caseId);
    if (idx === -1) return null;

    const item = cases[idx];
    const newPaid = Number(item.paidAmount || 0) + Number(paymentAmount);
    const newOutstanding = Math.max(0, Number(item.invoiceAmount) - newPaid);
    const isFull = newOutstanding === 0;

    const updated = {
      ...item,
      paidAmount: newPaid,
      outstandingAmount: newOutstanding,
      recoveryStatus: isFull ? 'Recovered' : 'Partially Recovered',
      notes: `${item.notes || ''} • Recorded payment of ₹${Number(paymentAmount).toLocaleString('en-IN')} via ${paymentMethod}.`.trim()
    };

    cases[idx] = updated;
    setStoredCases(cases);

    // Also update invoice and payment services for unified data consistency
    try {
      await paymentService.create({
        invoiceId: item.invoiceId,
        customer: item.customer,
        amount: Number(paymentAmount),
        method: paymentMethod,
        status: 'Success',
        gateway: 'Manual Dunning Settlement'
      });

      if (isFull) {
        // Mark invoice as Paid
        const invoices = await invoiceService.getAll();
        const invIdx = invoices.findIndex(i => i.id === item.invoiceId);
        if (invIdx !== -1) {
          invoices[invIdx].status = 'Paid';
          localStorage.setItem('siq_invoices', JSON.stringify(invoices));
        }
      }
    } catch (e) {
      console.warn('Sync to auxiliary invoice/payment services skipped:', e);
    }

    return updated;
  },

  scheduleReminder: async (caseId, nextDate) => {
    const cases = getStoredCases();
    const idx = cases.findIndex(c => c.id === caseId);
    if (idx === -1) return null;

    cases[idx].nextReminderDate = nextDate;
    setStoredCases(cases);
    return cases[idx];
  },

  cancelScheduledReminder: async (caseId) => {
    const cases = getStoredCases();
    const idx = cases.findIndex(c => c.id === caseId);
    if (idx === -1) return null;

    cases[idx].nextReminderDate = null;
    setStoredCases(cases);
    return cases[idx];
  },

  getRules: () => {
    try {
      const data = localStorage.getItem(DUNNING_RULES_KEY);
      return data ? JSON.parse(data) : DEFAULT_RULES;
    } catch {
      return DEFAULT_RULES;
    }
  },

  saveRules: (newRules) => {
    localStorage.setItem(DUNNING_RULES_KEY, JSON.stringify(newRules));
    return newRules;
  }
};

export default dunningService;
