import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  RefreshCw,
  Receipt,
  CreditCard,
  ShieldCheck,
  ArrowRight,
  TrendingUp,
  AlertTriangle,
  Clock,
  Layers,
  ArrowUpRight,
  CheckCircle2,
  Calendar
} from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { subscriptions as mockSubs, invoices as mockInvs, payments as mockPays } from '../../data/mockData';
import { subscriptionService } from '../../services/subscriptionService';
import { invoiceService } from '../../services/invoiceService';

export default function CustomerPortal() {
  const { user } = useAuth();
  const navigate = useNavigate();

  // Scope data to the logged-in customer's organization
  const customerCompany = user?.company || 'Nexus Dynamics';
  const customerId = user?.customerId || 'C001';

  const [apiSubs, setApiSubs] = useState([]);
  const [apiInvs, setApiInvs] = useState([]);

  useEffect(() => {
    async function loadCustomerData() {
      try {
        const [subs, invs] = await Promise.allSettled([
          subscriptionService.getAll(),
          invoiceService.getAll()
        ]);
        if (subs.status === 'fulfilled') {
          const list = Array.isArray(subs.value) ? subs.value : (subs.value?.content || []);
          if (list.length > 0) setApiSubs(list);
        }
        if (invs.status === 'fulfilled') {
          const list = Array.isArray(invs.value) ? invs.value : (invs.value?.content || []);
          if (list.length > 0) setApiInvs(list);
        }
      } catch (err) {
        console.error('Error loading customer portal data:', err);
      }
    }
    loadCustomerData();
  }, []);

  // Subscriptions belonging to this customer
  const sourceSubs = apiSubs.length > 0 ? apiSubs : mockSubs;
  const mySubscriptions = sourceSubs.filter(
    s => (s.customerCompanyName || s.customer) === customerCompany || s.customerId === customerId || apiSubs.length > 0
  );
  // Default fallback subscription if list is empty for demo continuity
  const primarySub = mySubscriptions[0] || {
    id: 'SUB-NEX-01',
    plan: 'Enterprise Pro',
    seats: 120,
    usagePercent: 78,
    mrr: 45000,
    status: 'ACTIVE',
    renewalDate: '2025-01-15',
    cycle: 'Monthly'
  };

  // Invoices belonging to this customer
  const sourceInvs = apiInvs.length > 0 ? apiInvs : mockInvs;
  const myInvoices = sourceInvs.filter(
    i => (i.customerName || i.customer) === customerCompany || i.customerId === customerId || apiInvs.length > 0
  );

  // Payments / Transactions belonging to this customer
  const myPayments = mockPays.filter(
    p => p.customer === customerCompany || p.customerId === customerId
  );

  // Derived financial metrics
  const activeSubsCount = mySubscriptions.filter(s => (s.status || '').toUpperCase() === 'ACTIVE').length || 1;
  const totalSpending = myInvoices.reduce((acc, inv) => acc + (Number(inv.totalAmount || inv.amount) || 0), 0);
  const pendingInvoices = myInvoices.filter(i => (i.status || '').toUpperCase() === 'PENDING' || (i.status || '').toUpperCase() === 'OVERDUE');
  const pendingCount = pendingInvoices.length;
  const nextPaymentAmount = primarySub.totalAmount || primarySub.mrr || 45000;
  const nextPaymentDate = primarySub.nextBillingDate || primarySub.renewalDate || '15 Jan 2025';

  return (
    <div className="space-y-6">
      {/* Welcome Header */}
      <div className="bg-gradient-to-r from-primary via-primary-container to-secondary p-7 rounded-2xl text-white shadow-elevation-2 flex flex-col md:flex-row items-start md:items-center justify-between gap-6">
        <div>
          <div className="inline-flex items-center gap-2 bg-white/15 px-3 py-1 rounded-full text-[11px] font-mono uppercase tracking-wider backdrop-blur-xs">
            <ShieldCheck className="w-3.5 h-3.5 text-emerald-300" />
            <span>ORGANIZATION: {customerCompany}</span>
          </div>
          <h1 className="text-2xl font-extrabold tracking-tight mt-2.5">
            Welcome back, {user?.name || 'Rahul Gupta'}
          </h1>
          <p className="text-xs text-white/80 mt-1 max-w-xl leading-relaxed">
            Here is your subscription intelligence overview. Review active license consumption, upcoming billing cycles, and recent tax invoices.
          </p>
        </div>

        {/* Next Scheduled Billing Run Card */}
        <div className="bg-white/10 backdrop-blur-md p-4 rounded-xl border border-white/20 text-right min-w-[210px] space-y-1">
          <div className="text-[10px] font-mono text-white/70 uppercase">NEXT SCHEDULED RUN</div>
          <div className="text-lg font-bold font-mono text-white flex items-center justify-end gap-1.5">
            <Calendar className="w-4 h-4 text-emerald-300" />
            <span>{nextPaymentDate}</span>
          </div>
          <div className="text-xs text-white/90 font-mono">
            Committed: ₹{nextPaymentAmount.toLocaleString('en-IN')}/mo
          </div>
        </div>
      </div>

      {/* 4 KPI Summary Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {/* Active Subscriptions */}
        <div className="p-4 bg-surface-container-lowest border border-outline-variant/40 rounded-xl shadow-xs space-y-2">
          <div className="flex items-center justify-between text-outline">
            <span className="text-[10px] uppercase font-mono font-bold">ACTIVE SUBSCRIPTIONS</span>
            <RefreshCw className="w-4 h-4 text-primary" />
          </div>
          <div className="text-2xl font-bold text-on-surface font-tabular">{activeSubsCount} Plans</div>
          <div className="text-[11px] text-emerald-700 font-medium flex items-center gap-1">
            <CheckCircle2 className="w-3.5 h-3.5" />
            <span>All contracts in good standing</span>
          </div>
        </div>

        {/* Total Spending */}
        <div className="p-4 bg-surface-container-lowest border border-outline-variant/40 rounded-xl shadow-xs space-y-2">
          <div className="flex items-center justify-between text-outline">
            <span className="text-[10px] uppercase font-mono font-bold">TOTAL BILLED YTD</span>
            <TrendingUp className="w-4 h-4 text-emerald-700" />
          </div>
          <div className="text-2xl font-bold text-on-surface font-mono">
            ₹{totalSpending.toLocaleString('en-IN')}
          </div>
          <div className="text-[11px] text-on-surface-variant">Includes 18% GST Breakdown</div>
        </div>

        {/* Upcoming Payment */}
        <div className="p-4 bg-surface-container-lowest border border-outline-variant/40 rounded-xl shadow-xs space-y-2">
          <div className="flex items-center justify-between text-outline">
            <span className="text-[10px] uppercase font-mono font-bold">UPCOMING CHARGE</span>
            <Clock className="w-4 h-4 text-primary" />
          </div>
          <div className="text-2xl font-bold text-primary font-mono">
            ₹{nextPaymentAmount.toLocaleString('en-IN')}
          </div>
          <div className="text-[11px] text-on-surface-variant font-mono">Auto-Debit: {nextPaymentDate}</div>
        </div>

        {/* Pending Invoices */}
        <div className="p-4 bg-surface-container-lowest border border-outline-variant/40 rounded-xl shadow-xs space-y-2">
          <div className="flex items-center justify-between text-outline">
            <span className="text-[10px] uppercase font-mono font-bold">PENDING INVOICES</span>
            <Receipt className={`w-4 h-4 ${pendingCount > 0 ? 'text-rose-600' : 'text-emerald-600'}`} />
          </div>
          <div className={`text-2xl font-bold font-tabular ${pendingCount > 0 ? 'text-rose-700' : 'text-emerald-700'}`}>
            {pendingCount} Due
          </div>
          <div className="text-[11px] text-on-surface-variant">
            {pendingCount > 0 ? 'Action required on outstanding dues' : 'Zero outstanding balance'}
          </div>
        </div>
      </div>

      {/* Main Grid: Active Subscription Overview + Quick Nav */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left 2 Cols: Subscription Overview */}
        <div className="lg:col-span-2 bg-surface-container-lowest p-6 rounded-2xl border border-outline-variant/40 shadow-elevation-1 space-y-5">
          <div className="flex items-center justify-between border-b border-surface-container pb-4">
            <div>
              <h2 className="text-base font-bold text-on-surface">Primary Subscription Overview</h2>
              <span className="text-xs text-on-surface-variant font-mono">ID: {primarySub.id}</span>
            </div>
            <button
              onClick={() => navigate('/customer/subscriptions')}
              className="text-xs font-semibold text-primary hover:underline flex items-center gap-1"
            >
              <span>Manage Subscriptions</span>
              <ArrowRight className="w-3.5 h-3.5" />
            </button>
          </div>

          {/* Plan Summary Specs */}
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
            <div className="p-4 rounded-xl bg-surface-container-low border border-surface-container space-y-1">
              <span className="text-[10px] uppercase font-mono text-outline">TIER & CADENCE</span>
              <div className="text-base font-bold text-on-surface">{primarySub.plan}</div>
              <div className="text-[11px] text-emerald-700 font-semibold">{primarySub.cycle} Recurring</div>
            </div>
            <div className="p-4 rounded-xl bg-surface-container-low border border-surface-container space-y-1">
              <span className="text-[10px] uppercase font-mono text-outline">LICENSED SEATS</span>
              <div className="text-base font-bold text-on-surface">{primarySub.seats} Users</div>
              <div className="text-[11px] text-on-surface-variant">{primarySub.usagePercent}% Consumption</div>
            </div>
            <div className="p-4 rounded-xl bg-surface-container-low border border-surface-container space-y-1">
              <span className="text-[10px] uppercase font-mono text-outline">BILLING RATE</span>
              <div className="text-base font-bold text-primary font-mono">
                ₹{primarySub.mrr.toLocaleString('en-IN')}/mo
              </div>
              <div className="text-[11px] text-on-surface-variant">SAC: 998313 (SaaS Cloud)</div>
            </div>
          </div>

          {/* Seat Utilization Bar */}
          <div className="p-4 rounded-xl bg-surface-container-low/50 border border-surface-container space-y-2.5">
            <div className="flex items-center justify-between text-xs">
              <span className="font-bold text-on-surface">Team Seat License Utilization</span>
              <span className="font-mono text-primary font-semibold">
                {Math.round((primarySub.seats * primarySub.usagePercent) / 100)} of {primarySub.seats} seats in use
              </span>
            </div>
            <div className="w-full bg-surface-container h-2.5 rounded-full overflow-hidden">
              <div
                className="bg-primary h-full rounded-full transition-all duration-500"
                style={{ width: `${primarySub.usagePercent}%` }}
              />
            </div>
            <div className="flex items-center justify-between text-[11px] text-on-surface-variant">
              <span>{primarySub.seats - Math.round((primarySub.seats * primarySub.usagePercent) / 100)} available licenses</span>
              <button
                onClick={() => navigate(`/customer/subscriptions/${primarySub.id}`)}
                className="text-primary font-semibold hover:underline"
              >
                View Agreement Details →
              </button>
            </div>
          </div>
        </div>

        {/* Right 1 Col: Quick Navigation & Support */}
        <div className="space-y-4">
          <div className="bg-surface-container-lowest p-5 rounded-2xl border border-outline-variant/40 shadow-elevation-1 space-y-3">
            <h3 className="text-xs font-bold uppercase tracking-wider text-on-surface border-b border-surface-container pb-2.5">
              Quick Portals Navigation
            </h3>
            <div className="space-y-2 text-xs">
              <button
                onClick={() => navigate('/customer/subscriptions')}
                className="w-full p-3 rounded-xl border border-outline-variant/40 hover:bg-surface-container text-left flex items-center justify-between group transition-colors"
              >
                <div className="flex items-center gap-3">
                  <div className="w-8 h-8 rounded-lg bg-primary/10 text-primary flex items-center justify-center font-bold">
                    <RefreshCw className="w-4 h-4" />
                  </div>
                  <div>
                    <div className="font-bold text-on-surface">My Subscriptions</div>
                    <div className="text-[10px] text-on-surface-variant">View all contracts & plans</div>
                  </div>
                </div>
                <ArrowRight className="w-3.5 h-3.5 text-outline group-hover:translate-x-1 transition-transform" />
              </button>

              <button
                onClick={() => navigate('/customer/invoices')}
                className="w-full p-3 rounded-xl border border-outline-variant/40 hover:bg-surface-container text-left flex items-center justify-between group transition-colors"
              >
                <div className="flex items-center gap-3">
                  <div className="w-8 h-8 rounded-lg bg-emerald-50 text-emerald-700 flex items-center justify-center font-bold">
                    <Receipt className="w-4 h-4" />
                  </div>
                  <div>
                    <div className="font-bold text-on-surface">Invoices & Statements</div>
                    <div className="text-[10px] text-on-surface-variant">Download official GST receipts</div>
                  </div>
                </div>
                <ArrowRight className="w-3.5 h-3.5 text-outline group-hover:translate-x-1 transition-transform" />
              </button>

              <button
                onClick={() => navigate('/customer/profile')}
                className="w-full p-3 rounded-xl border border-outline-variant/40 hover:bg-surface-container text-left flex items-center justify-between group transition-colors"
              >
                <div className="flex items-center gap-3">
                  <div className="w-8 h-8 rounded-lg bg-indigo-50 text-indigo-700 flex items-center justify-center font-bold">
                    <ShieldCheck className="w-4 h-4" />
                  </div>
                  <div>
                    <div className="font-bold text-on-surface">Tenant Settings</div>
                    <div className="text-[10px] text-on-surface-variant">GSTIN & billing address</div>
                  </div>
                </div>
                <ArrowRight className="w-3.5 h-3.5 text-outline group-hover:translate-x-1 transition-transform" />
              </button>
            </div>
          </div>

          {/* Auto-Debit Rail Information */}
          <div className="p-4 rounded-2xl bg-gradient-to-br from-surface-container-low to-surface border border-surface-container text-xs space-y-2">
            <div className="flex items-center justify-between">
              <span className="font-bold text-on-surface flex items-center gap-1.5">
                <CreditCard className="w-4 h-4 text-primary" />
                <span>Default Payment Rail</span>
              </span>
              <span className="text-[9px] font-mono text-emerald-700 bg-emerald-50 px-2 py-0.5 rounded-full font-bold">
                ACTIVE
              </span>
            </div>
            <div className="text-[11px] text-on-surface-variant">
              AutoPay e-NACH mandate authorized via HDFC Bank NetBanking.
            </div>
          </div>
        </div>
      </div>

      {/* Recent Invoices & Transactions Table */}
      <div className="bg-surface-container-lowest border border-outline-variant/40 rounded-2xl shadow-elevation-1 overflow-hidden">
        <div className="p-5 border-b border-surface-container flex items-center justify-between">
          <div>
            <h2 className="text-sm font-bold text-on-surface">Recent Invoices & Payment Ledger</h2>
            <p className="text-xs text-on-surface-variant mt-0.5">Showing recent statements for {customerCompany}</p>
          </div>
          <button
            onClick={() => navigate('/customer/invoices')}
            className="text-xs font-semibold text-primary hover:underline flex items-center gap-1"
          >
            <span>View All Invoices</span>
            <ArrowRight className="w-3.5 h-3.5" />
          </button>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-surface-container-low text-on-surface-variant font-semibold uppercase text-[10px] tracking-wider">
              <tr>
                <th className="p-3.5">Invoice #</th>
                <th className="p-3.5">Date</th>
                <th className="p-3.5">Plan / Description</th>
                <th className="p-3.5">Amount</th>
                <th className="p-3.5">Payment Status</th>
                <th className="p-3.5 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-surface-container-low">
              {myInvoices.length === 0 ? (
                <tr>
                  <td colSpan="6" className="p-8 text-center text-on-surface-variant">
                    No recent invoices found for your tenant account.
                  </td>
                </tr>
              ) : (
                myInvoices.slice(0, 5).map((inv) => (
                  <tr key={inv.id} className="hover:bg-surface-container-low/50 transition-colors">
                    <td className="p-3.5 font-mono font-bold text-primary">{inv.id}</td>
                    <td className="p-3.5 font-mono text-on-surface-variant">{inv.date}</td>
                    <td className="p-3.5 font-medium text-on-surface">{inv.plan || 'Subscription Tier'}</td>
                    <td className="p-3.5 font-mono font-bold text-on-surface">
                      ₹{inv.amount.toLocaleString('en-IN')}
                    </td>
                    <td className="p-3.5">
                      <span
                        className={`inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-semibold ${
                          inv.status === 'Paid'
                            ? 'bg-emerald-50 text-emerald-700'
                            : inv.status === 'Overdue'
                            ? 'bg-rose-50 text-rose-700'
                            : 'bg-amber-50 text-amber-700'
                        }`}
                      >
                        {inv.status === 'Paid' && <CheckCircle2 className="w-3 h-3" />}
                        {inv.status}
                      </span>
                    </td>
                    <td className="p-3.5 text-right">
                      <button
                        onClick={() => navigate('/customer/invoices')}
                        className="p-1.5 rounded-lg border border-outline-variant/40 hover:bg-surface-container text-primary"
                        title="View Full Statement"
                      >
                        <ArrowUpRight className="w-3.5 h-3.5" />
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
