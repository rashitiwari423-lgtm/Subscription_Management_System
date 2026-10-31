// SubscriptIQ Mock Data

export const customers = [
  { id: 'C001', name: 'Nexus Dynamics', email: 'billing@nexusdynamics.com', plan: 'Enterprise', status: 'Active', mrr: 45000, since: '2023-01-15', seats: 120, industry: 'Technology' },
  { id: 'C002', name: 'Apex Global Solutions', email: 'finance@apexglobal.com', plan: 'Business', status: 'Active', mrr: 18500, since: '2023-03-22', seats: 45, industry: 'Finance' },
  { id: 'C003', name: 'Vertex Labs', email: 'admin@vertexlabs.io', plan: 'Enterprise', status: 'At Risk', mrr: 32000, since: '2022-11-08', seats: 80, industry: 'Biotech' },
  { id: 'C004', name: 'CloudStream Inc', email: 'ops@cloudstream.com', plan: 'Starter', status: 'Active', mrr: 4200, since: '2024-01-10', seats: 12, industry: 'SaaS' },
  { id: 'C005', name: 'DataPulse Analytics', email: 'it@datapulse.ai', plan: 'Business', status: 'Churned', mrr: 0, since: '2023-06-01', seats: 0, industry: 'Data' },
  { id: 'C006', name: 'Meridian Corp', email: 'billing@meridiancorp.com', plan: 'Enterprise', status: 'Active', mrr: 55000, since: '2022-08-20', seats: 200, industry: 'Consulting' },
  { id: 'C007', name: 'Orion Fintech', email: 'admin@orionfintech.in', plan: 'Business', status: 'Active', mrr: 22000, since: '2023-09-15', seats: 55, industry: 'Fintech' },
  { id: 'C008', name: 'ByteForge', email: 'tech@byteforge.io', plan: 'Starter', status: 'Active', mrr: 3800, since: '2024-02-28', seats: 10, industry: 'Technology' },
];

export const subscriptions = [
  { id: 'SUB001', customerId: 'C001', customer: 'Nexus Dynamics', plan: 'Enterprise Pro', status: 'Active', mrr: 45000, arr: 540000, startDate: '2023-01-15', renewalDate: '2025-01-15', seats: 120, usagePercent: 78 },
  { id: 'SUB002', customerId: 'C002', customer: 'Apex Global Solutions', plan: 'Business Plus', status: 'Active', mrr: 18500, arr: 222000, startDate: '2023-03-22', renewalDate: '2025-03-22', seats: 45, usagePercent: 62 },
  { id: 'SUB003', customerId: 'C003', customer: 'Vertex Labs', plan: 'Enterprise', status: 'At Risk', mrr: 32000, arr: 384000, startDate: '2022-11-08', renewalDate: '2024-11-08', seats: 80, usagePercent: 31 },
  { id: 'SUB004', customerId: 'C004', customer: 'CloudStream Inc', plan: 'Starter', status: 'Active', mrr: 4200, arr: 50400, startDate: '2024-01-10', renewalDate: '2025-01-10', seats: 12, usagePercent: 90 },
  { id: 'SUB005', customerId: 'C006', customer: 'Meridian Corp', plan: 'Enterprise Ultimate', status: 'Active', mrr: 55000, arr: 660000, startDate: '2022-08-20', renewalDate: '2025-08-20', seats: 200, usagePercent: 85 },
  { id: 'SUB006', customerId: 'C007', customer: 'Orion Fintech', plan: 'Business', status: 'Active', mrr: 22000, arr: 264000, startDate: '2023-09-15', renewalDate: '2025-09-15', seats: 55, usagePercent: 71 },
  { id: 'SUB007', customerId: 'C008', customer: 'ByteForge', plan: 'Starter', status: 'Trial', mrr: 0, arr: 0, startDate: '2024-02-28', renewalDate: '2024-03-28', seats: 10, usagePercent: 45 },
];

export const invoices = [
  { id: 'INV-2024-001', customerId: 'C001', customer: 'Nexus Dynamics', amount: 45000, status: 'Paid', date: '2024-01-01', dueDate: '2024-01-15', paidDate: '2024-01-10' },
  { id: 'INV-2024-002', customerId: 'C002', customer: 'Apex Global Solutions', amount: 18500, status: 'Paid', date: '2024-01-01', dueDate: '2024-01-15', paidDate: '2024-01-12' },
  { id: 'INV-2024-003', customerId: 'C003', customer: 'Vertex Labs', amount: 32000, status: 'Overdue', date: '2024-01-01', dueDate: '2024-01-15', paidDate: null },
  { id: 'INV-2024-004', customerId: 'C004', customer: 'CloudStream Inc', amount: 4200, status: 'Paid', date: '2024-01-01', dueDate: '2024-01-15', paidDate: '2024-01-08' },
  { id: 'INV-2024-005', customerId: 'C006', customer: 'Meridian Corp', amount: 55000, status: 'Paid', date: '2024-01-01', dueDate: '2024-01-15', paidDate: '2024-01-05' },
  { id: 'INV-2024-006', customerId: 'C007', customer: 'Orion Fintech', amount: 22000, status: 'Pending', date: '2024-02-01', dueDate: '2024-02-15', paidDate: null },
  { id: 'INV-2024-007', customerId: 'C001', customer: 'Nexus Dynamics', amount: 45000, status: 'Paid', date: '2024-02-01', dueDate: '2024-02-15', paidDate: '2024-02-10' },
  { id: 'INV-2024-008', customerId: 'C003', customer: 'Vertex Labs', amount: 32000, status: 'Overdue', date: '2024-02-01', dueDate: '2024-02-15', paidDate: null },
];

export const payments = [
  { id: 'PAY-001', invoiceId: 'INV-2024-001', customer: 'Nexus Dynamics', amount: 45000, method: 'Bank Transfer', status: 'Success', date: '2024-01-10', gateway: 'Razorpay' },
  { id: 'PAY-002', invoiceId: 'INV-2024-002', customer: 'Apex Global Solutions', amount: 18500, method: 'Credit Card', status: 'Success', date: '2024-01-12', gateway: 'Stripe' },
  { id: 'PAY-003', invoiceId: 'INV-2024-003', customer: 'Vertex Labs', amount: 32000, method: 'UPI', status: 'Failed', date: '2024-01-16', gateway: 'Razorpay', failureReason: 'Insufficient funds' },
  { id: 'PAY-004', invoiceId: 'INV-2024-004', customer: 'CloudStream Inc', amount: 4200, method: 'Credit Card', status: 'Success', date: '2024-01-08', gateway: 'Stripe' },
  { id: 'PAY-005', invoiceId: 'INV-2024-005', customer: 'Meridian Corp', amount: 55000, method: 'Bank Transfer', status: 'Success', date: '2024-01-05', gateway: 'Razorpay' },
];

export const aiInsights = [
  {
    id: 'AI001',
    type: 'Revenue Alert',
    severity: 'high',
    title: 'Revenue Growth Alert',
    description: 'MRR increased by 12.4% this month. 3 new Enterprise clients added. Projected ARR target on track.',
    metric: '+12.4% MRR',
    metricValue: 224000,
    affectedAccounts: ['Nexus Dynamics', 'Meridian Corp'],
    recommendation: 'Expand upsell outreach to Business tier accounts',
    createdAt: '2024-03-01T09:00:00Z',
    actionable: true,
  },
  {
    id: 'AI002',
    type: 'Churn Risk',
    severity: 'critical',
    title: 'Churn Risk Detected',
    description: '8 accounts showing significant disengagement signals over the last 21 days. Feature usage dropped 60%.',
    metric: '8 At-Risk Accounts',
    metricValue: 8,
    affectedAccounts: ['Vertex Labs', 'DataPulse Analytics', 'ByteForge'],
    recommendation: 'Trigger automated re-engagement campaign immediately',
    createdAt: '2024-03-02T10:30:00Z',
    actionable: true,
  },
  {
    id: 'AI003',
    type: 'Renewal Opportunity',
    severity: 'medium',
    title: 'Renewal Opportunity',
    description: '14 subscriptions expiring within 30 days. Combined ARR at stake: ₹84L. Early renewal incentive recommended.',
    metric: '14 Renewals Due',
    metricValue: 14,
    affectedAccounts: ['Apex Global', 'CloudStream Inc'],
    recommendation: 'Offer 10% loyalty discount for early renewal',
    createdAt: '2024-03-03T08:15:00Z',
    actionable: true,
  },
  {
    id: 'AI004',
    type: 'Cost Optimization',
    severity: 'low',
    title: 'Cost Optimization Opportunity',
    description: '12 unused licenses detected across 5 enterprise accounts. Monthly overpayment estimated at ₹2.4L.',
    metric: '12 Unused Licenses',
    metricValue: 12,
    affectedAccounts: ['Nexus Dynamics', 'Meridian Corp', 'Orion Fintech'],
    recommendation: 'Downgrade unused seats to lower tier or reassign',
    createdAt: '2024-03-04T11:00:00Z',
    actionable: true,
  },
];

export const costRecommendations = [
  {
    id: 'CR001',
    type: 'UNUSED LICENSE',
    customer: 'Nexus Dynamics',
    subscription: 'Enterprise Pro - 120 Seats',
    currentCost: 45000,
    recommendedCost: 38000,
    savings: 7000,
    description: '34 seats unused for 45+ days. Recommend downgrade to 86 active seats.',
    status: 'pending',
    priority: 'high',
  },
  {
    id: 'CR002',
    type: 'DUPLICATE',
    customer: 'Meridian Corp',
    subscription: 'Zoom + Teams Overlap',
    currentCost: 12000,
    recommendedCost: 6000,
    savings: 6000,
    description: 'Both Zoom Business and MS Teams detected in the same dept. Consolidate to one.',
    status: 'pending',
    priority: 'medium',
  },
  {
    id: 'CR003',
    type: 'PLAN DOWNGRADE',
    customer: 'CloudStream Inc',
    subscription: 'Business Plus - 45 Seats',
    currentCost: 18500,
    recommendedCost: 12000,
    savings: 6500,
    description: 'Usage telemetry shows only Starter features used. Downgrade saves 35%.',
    status: 'pending',
    priority: 'medium',
  },
  {
    id: 'CR004',
    type: 'OVER-PROVISIONED',
    customer: 'Orion Fintech',
    subscription: 'Enterprise - 55 Seats',
    currentCost: 22000,
    recommendedCost: 16500,
    savings: 5500,
    description: 'Storage allocation 3x above actual usage. Rightsize storage tier.',
    status: 'applied',
    priority: 'low',
  },
  {
    id: 'CR005',
    type: 'UNUSED LICENSE',
    customer: 'Vertex Labs',
    subscription: 'Enterprise - 80 Seats',
    currentCost: 32000,
    recommendedCost: 24000,
    savings: 8000,
    description: '28 seats with zero logins in 60 days. Immediate action recommended.',
    status: 'pending',
    priority: 'high',
  },
];

export const auditLogs = [
  { id: 'AL001', timestamp: '2024-03-05T14:32:10Z', user: 'admin@subscriptiq.com', action: 'Invoice Generated', resource: 'INV-2024-001', ip: '192.168.1.10', status: 'Success', details: 'Batch invoice run for March cycle' },
  { id: 'AL002', timestamp: '2024-03-05T13:15:22Z', user: 'rahul.sharma@subscriptiq.com', action: 'Subscription Updated', resource: 'SUB003', ip: '10.0.0.45', status: 'Success', details: 'Plan changed from Enterprise to Business' },
  { id: 'AL003', timestamp: '2024-03-05T12:08:05Z', user: 'unknown', action: 'Login Failed', resource: 'Auth', ip: '203.45.67.89', status: 'Failed', details: 'Invalid credentials - 3rd attempt' },
  { id: 'AL004', timestamp: '2024-03-05T11:44:00Z', user: 'priya.k@subscriptiq.com', action: 'Customer Created', resource: 'C009', ip: '10.0.0.12', status: 'Success', details: 'New Enterprise customer onboarded' },
  { id: 'AL005', timestamp: '2024-03-05T10:30:18Z', user: 'admin@subscriptiq.com', action: 'Settings Changed', resource: 'OrgSettings', ip: '192.168.1.10', status: 'Success', details: 'Tax config updated for GST 18%' },
  { id: 'AL006', timestamp: '2024-03-05T09:22:44Z', user: 'api_key_prod', action: 'API Call', resource: '/api/v2/subscriptions', ip: '52.66.1.200', status: 'Success', details: 'GET /subscriptions - 200 OK' },
  { id: 'AL007', timestamp: '2024-03-05T08:15:30Z', user: 'admin@subscriptiq.com', action: 'Export Data', resource: 'Invoices CSV', ip: '192.168.1.10', status: 'Success', details: 'Exported 1,243 invoices to CSV' },
];

export const dunningData = {
  recoveryStats: {
    recovered: 4520000,
    successRate: 67,
    avgRecoveryDays: 8,
    totalAttempts: 145,
    pendingRecovery: 1850000,
  },
  failedPayments: [
    { id: 'FP001', customer: 'Vertex Labs', amount: 32000, failureReason: 'Insufficient Funds', attempts: 2, nextRetry: '2024-03-08', lastAttempt: '2024-03-05', status: 'Retrying' },
    { id: 'FP002', customer: 'DataPulse Analytics', amount: 15000, failureReason: 'Card Expired', attempts: 3, nextRetry: '2024-03-10', lastAttempt: '2024-03-06', status: 'Card Update Required' },
    { id: 'FP003', customer: 'ByteForge', amount: 3800, failureReason: 'Bank Decline', attempts: 1, nextRetry: '2024-03-07', lastAttempt: '2024-03-04', status: 'Retrying' },
    { id: 'FP004', customer: 'Orion Fintech', amount: 22000, failureReason: 'Do Not Honor', attempts: 4, nextRetry: null, lastAttempt: '2024-03-05', status: 'Escalated' },
    { id: 'FP005', customer: 'Nexus Dynamics', amount: 45000, failureReason: 'Timeout', attempts: 1, nextRetry: '2024-03-06', lastAttempt: '2024-03-05', status: 'Retrying' },
  ],
};

export const billingRuns = [
  { id: 'BR001', date: '2024-03-01', invoicesGenerated: 142, totalAmount: 1872000, status: 'Completed', duration: '4m 32s', executedBy: 'System (Auto)' },
  { id: 'BR002', date: '2024-02-01', invoicesGenerated: 138, totalAmount: 1820000, status: 'Completed', duration: '4m 18s', executedBy: 'System (Auto)' },
  { id: 'BR003', date: '2024-01-01', invoicesGenerated: 135, totalAmount: 1795000, status: 'Completed', duration: '5m 01s', executedBy: 'admin@subscriptiq.com' },
  { id: 'BR004', date: '2023-12-01', invoicesGenerated: 130, totalAmount: 1740000, status: 'Completed', duration: '3m 55s', executedBy: 'System (Auto)' },
  { id: 'BR005', date: '2023-11-01', invoicesGenerated: 125, totalAmount: 1680000, status: 'Completed', duration: '4m 10s', executedBy: 'System (Auto)' },
];

export const revenueData = [
  { month: 'Apr', arr: 18200000, mrr: 1516667, newMrr: 280000, churnedMrr: 45000 },
  { month: 'May', arr: 19100000, mrr: 1591667, newMrr: 320000, churnedMrr: 38000 },
  { month: 'Jun', arr: 19800000, mrr: 1650000, newMrr: 295000, churnedMrr: 52000 },
  { month: 'Jul', arr: 20500000, mrr: 1708333, newMrr: 310000, churnedMrr: 41000 },
  { month: 'Aug', arr: 21200000, mrr: 1766667, newMrr: 350000, churnedMrr: 35000 },
  { month: 'Sep', arr: 21900000, mrr: 1825000, newMrr: 380000, churnedMrr: 48000 },
  { month: 'Oct', arr: 22400000, mrr: 1866667, newMrr: 290000, churnedMrr: 60000 },
  { month: 'Nov', arr: 22900000, mrr: 1908333, newMrr: 320000, churnedMrr: 42000 },
  { month: 'Dec', arr: 23100000, mrr: 1925000, newMrr: 250000, churnedMrr: 55000 },
  { month: 'Jan', arr: 23500000, mrr: 1958333, newMrr: 310000, churnedMrr: 38000 },
  { month: 'Feb', arr: 23900000, mrr: 1991667, newMrr: 340000, churnedMrr: 44000 },
  { month: 'Mar', arr: 22400000, mrr: 1872000, newMrr: 420000, churnedMrr: 32000 },
];

export const churnData = [
  { month: 'Apr', rate: 2.8, accounts: 4 },
  { month: 'May', rate: 2.4, accounts: 3 },
  { month: 'Jun', rate: 3.1, accounts: 5 },
  { month: 'Jul', rate: 2.6, accounts: 4 },
  { month: 'Aug', rate: 2.0, accounts: 3 },
  { month: 'Sep', rate: 2.7, accounts: 4 },
  { month: 'Oct', rate: 3.5, accounts: 6 },
  { month: 'Nov', rate: 2.3, accounts: 3 },
  { month: 'Dec', rate: 3.0, accounts: 5 },
  { month: 'Jan', rate: 2.1, accounts: 3 },
  { month: 'Feb', rate: 2.5, accounts: 4 },
  { month: 'Mar', rate: 1.8, accounts: 2 },
];

export const staffMembers = [
  { id: 'STF001', name: 'Aarav Patel', email: 'aarav@subscriptiq.com', role: 'Account Manager', status: 'Active', assignedCustomers: 18, department: 'Customer Success', phone: '+91 98765 43210' },
  { id: 'STF002', name: 'Neha Sharma', email: 'neha@subscriptiq.com', role: 'Support Agent', status: 'Active', assignedCustomers: 45, department: 'Support', phone: '+91 98765 43211' },
  { id: 'STF003', name: 'Rohan Verma', email: 'rohan@subscriptiq.com', role: 'Sales Rep', status: 'Active', assignedCustomers: 12, department: 'Sales', phone: '+91 98765 43212' },
  { id: 'STF004', name: 'Ananya Iyer', email: 'ananya@subscriptiq.com', role: 'Billing Specialist', status: 'Active', assignedCustomers: 60, department: 'Finance Ops', phone: '+91 98765 43213' }
];

export const products = [
  { id: 'PROD-01', name: 'SubscriptIQ Core Engine', code: 'SIQ-CORE', category: 'Platform', status: 'Active', variantsCount: 3, basePrice: 15000, description: 'Central subscription orchestration & automated proration engine' },
  { id: 'PROD-02', name: 'AI Revenue Command Sentinel', code: 'SIQ-SENTINEL', category: 'Intelligence', status: 'Active', variantsCount: 2, basePrice: 28000, description: 'Predictive churn anomaly detector and seat usage monitor' },
  { id: 'PROD-03', name: 'Smart Dunning & Recovery Suite', code: 'SIQ-DUNNING', category: 'Billing', status: 'Active', variantsCount: 2, basePrice: 12000, description: 'Automated payment retry sequences and WhatsApp/Email recovery hooks' },
  { id: 'PROD-04', name: 'B2B CPQ Quotation Studio', code: 'SIQ-CPQ', category: 'Sales', status: 'Active', variantsCount: 2, basePrice: 20000, description: 'Custom contract negotiation, tier pricing, and instant PDF quoting' }
];

export const plans = [
  { id: 'PLAN-01', name: 'Starter Pro', tier: 'Starter', price: 4999, billingCycle: 'Monthly', subscribers: 42, activeMRR: 209958, status: 'Active', features: ['Up to 500 Subscriptions', 'Stripe & Razorpay Integration', 'Standard Invoicing', 'Email Support'] },
  { id: 'PLAN-02', name: 'Growth Scale', tier: 'Growth', price: 18500, billingCycle: 'Monthly', subscribers: 88, activeMRR: 1628000, status: 'Active', features: ['Up to 5,000 Subscriptions', 'Automated Dunning Workflows', 'Smart Proration Engine', 'Priority Slack Support'] },
  { id: 'PLAN-03', name: 'Enterprise Sentinel', tier: 'Enterprise', price: 45000, billingCycle: 'Monthly', subscribers: 35, activeMRR: 1575000, status: 'Active', features: ['Unlimited Subscriptions', 'AI Churn Sentinel & Command', 'Custom CPQ Quotations', 'Dedicated TAM & Custom SLA'] }
];

export const quotations = [
  { id: 'QT-2024-8891', customer: 'Nexus Dynamics', contact: 'Vikram Malhotra', value: 540000, validUntil: '2024-12-15', status: 'Negotiation', itemsCount: 3, discount: '10%' },
  { id: 'QT-2024-8892', customer: 'Tata Digital Ventures', contact: 'Kavita Nair', value: 890000, validUntil: '2024-12-20', status: 'Sent', itemsCount: 4, discount: '15%' },
  { id: 'QT-2024-8893', customer: 'Zomato Merchant Ops', contact: 'Siddharth Sen', value: 1250000, validUntil: '2024-11-30', status: 'Accepted', itemsCount: 5, discount: '12%' },
  { id: 'QT-2024-8894', customer: 'FreshWorks Cloud unit', contact: 'Deepak Roy', value: 340000, validUntil: '2024-10-15', status: 'Draft', itemsCount: 2, discount: '5%' }
];

export const discounts = [
  { id: 'DSC-01', code: 'DIWALI25', name: 'Festive Growth Bonus', type: 'Percentage', value: 25, status: 'Active', usesCount: 34, maxUses: 100, validUntil: '2024-11-30' },
  { id: 'DSC-02', code: 'ENTERPRISE10', name: 'Annual Commit Perk', type: 'Percentage', value: 10, status: 'Active', usesCount: 19, maxUses: 50, validUntil: '2025-03-31' },
  { id: 'DSC-03', code: 'FLAT50K', name: 'Q4 Migration Credit', type: 'Fixed', value: 50000, status: 'Active', usesCount: 8, maxUses: 20, validUntil: '2024-12-31' }
];

export const taxes = [
  { id: 'TAX-01', name: 'GST (Domestic Intra-State)', code: 'CGST+SGST', rate: 18, jurisdiction: 'India (Same State - 9% CGST + 9% SGST)', hsn: '998313', status: 'Active' },
  { id: 'TAX-02', name: 'GST (Domestic Inter-State)', code: 'IGST', rate: 18, jurisdiction: 'India (Inter-State - 18% IGST)', hsn: '998313', status: 'Active' },
  { id: 'TAX-03', name: 'Export SaaS Exemption (LUT)', code: 'LUT-ZERO', rate: 0, jurisdiction: 'International / Export of Services', hsn: '998313', status: 'Active' },
  { id: 'TAX-04', name: 'Special Economic Zone (SEZ)', code: 'SEZ-ZERO', rate: 0, jurisdiction: 'India SEZ Zero-Rated Supply', hsn: '998313', status: 'Active' }
];

export const notificationsList = [
  { id: 'NOTIF-1', title: 'High Churn Risk Triggered', message: 'Vertex Labs usage dropped by 45% in the last 14 days.', type: 'AI Alert', time: '10m ago', unread: true },
  { id: 'NOTIF-2', title: 'Failed Payment Recovered', message: 'Dunning sequence successfully recovered ₹32,000 for Orion Fintech.', type: 'Payment', time: '1h ago', unread: true },
  { id: 'NOTIF-3', title: 'Quotation Accepted', message: 'Zomato Merchant Ops accepted quotation QT-2024-8893 (₹12.5L ARR).', type: 'Quotation', time: '3h ago', unread: false },
  { id: 'NOTIF-4', title: 'Monthly Billing Run Scheduled', message: 'Batch run #BR-2024-11 will execute at 00:00 UTC.', type: 'System', time: '6h ago', unread: false }
];
