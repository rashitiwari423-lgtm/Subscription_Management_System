import React, { useState } from 'react';
import {
  TrendingUp,
  Users,
  RefreshCw,
  AlertTriangle,
  ArrowUpRight,
  ArrowDownRight,
  Sparkles,
  CheckCircle2,
  Calendar,
  ChevronRight,
  Receipt,
  CreditCard,
  DollarSign
} from 'lucide-react';
import {
  AreaChart,
  Area,
  XAxis,
  YAxis,
  Tooltip,
  ResponsiveContainer,
  CartesianGrid
} from 'recharts';
import { subscriptions, aiInsights } from '../../../data/mockData';
import { useNavigate } from 'react-router-dom';

const REVENUE_DATA = [
  { month: 'Apr', mrr: 15.1, arr: 182 },
  { month: 'May', mrr: 15.9, arr: 191 },
  { month: 'Jun', mrr: 16.5, arr: 198 },
  { month: 'Jul', mrr: 17.0, arr: 205 },
  { month: 'Aug', mrr: 17.6, arr: 212 },
  { month: 'Sep', mrr: 18.2, arr: 219 },
  { month: 'Oct', mrr: 18.7, arr: 224 },
];

export default function AdminDashboard() {
  const navigate = useNavigate();

  return (
    <div className="space-y-6">
      {/* Page Title & Operational Status */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3">
        <div>
          <h1 className="text-xl font-bold text-on-surface">Financial & Subscription Overview</h1>
          <p className="text-xs text-on-surface-variant mt-0.5">
            Key operational metrics, ARR growth trajectory, and urgent collection actions.
          </p>
        </div>
        <div className="flex items-center gap-2">
          <button
            onClick={() => navigate('/admin/quotations/new')}
            className="px-3 py-1.5 border border-outline-variant/40 rounded-lg bg-surface-container-lowest text-xs font-semibold text-on-surface hover:bg-surface-container"
          >
            Create Quotation
          </button>
          <button
            onClick={() => navigate('/admin/dunning')}
            className="px-3.5 py-1.5 rounded-lg bg-rose-700 hover:bg-rose-800 text-white text-xs font-semibold flex items-center gap-1.5 shadow-sm"
          >
            <AlertTriangle className="w-3.5 h-3.5" />
            <span>Manage Overdue Arrears</span>
          </button>
        </div>
      </div>

      {/* Part B: 4 High-Signal Core KPI Cards (Clear & uncluttered) */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="bg-surface-container-lowest p-5 rounded-2xl border border-outline-variant/40 shadow-elevation-1">
          <div className="flex items-center justify-between text-on-surface-variant text-xs mb-2">
            <span className="font-bold uppercase tracking-wider text-[10px]">Monthly Recurring Revenue</span>
            <span className="inline-flex items-center text-emerald-700 bg-emerald-50 px-1.5 py-0.5 rounded text-[10px] font-semibold">
              <TrendingUp className="w-3 h-3 mr-0.5" /> +12.4% MoM
            </span>
          </div>
          <div className="text-2xl font-extrabold text-on-surface font-tabular">₹18,72,500</div>
          <div className="text-[11px] text-on-surface-variant mt-1">₹2.24 Cr Run-Rate (ARR)</div>
        </div>

        <div className="bg-surface-container-lowest p-5 rounded-2xl border border-outline-variant/40 shadow-elevation-1">
          <div className="flex items-center justify-between text-on-surface-variant text-xs mb-2">
            <span className="font-bold uppercase tracking-wider text-[10px]">Active Subscriptions</span>
            <span className="inline-flex items-center text-emerald-700 bg-emerald-50 px-1.5 py-0.5 rounded text-[10px] font-semibold">
              +38 This Month
            </span>
          </div>
          <div className="text-2xl font-extrabold text-on-surface font-tabular">1,247</div>
          <div className="text-[11px] text-on-surface-variant mt-1">98.2% Account Retention</div>
        </div>

        <div className="bg-surface-container-lowest p-5 rounded-2xl border border-outline-variant/40 shadow-elevation-1">
          <div className="flex items-center justify-between text-on-surface-variant text-xs mb-2">
            <span className="font-bold uppercase tracking-wider text-[10px]">Overdue Arrears</span>
            <span className="inline-flex items-center text-rose-700 bg-rose-50 px-1.5 py-0.5 rounded text-[10px] font-semibold">
              Needs Attention
            </span>
          </div>
          <div className="text-2xl font-extrabold text-rose-700 font-tabular">₹91,000</div>
          <div className="text-[11px] text-on-surface-variant mt-1">4 Overdue Client Invoices</div>
        </div>

        <div className="bg-surface-container-lowest p-5 rounded-2xl border border-outline-variant/40 shadow-elevation-1">
          <div className="flex items-center justify-between text-on-surface-variant text-xs mb-2">
            <span className="font-bold uppercase tracking-wider text-[10px]">Dunning Recovery Rate</span>
            <span className="inline-flex items-center text-emerald-700 bg-emerald-50 px-1.5 py-0.5 rounded text-[10px] font-semibold">
              Automated Retries
            </span>
          </div>
          <div className="text-2xl font-extrabold text-emerald-700 font-tabular">88.4%</div>
          <div className="text-[11px] text-on-surface-variant mt-1">₹3,24,000 Recovered</div>
        </div>
      </div>

      {/* Main Charts Row */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* ARR & MRR Velocity Curve (8 Cols) */}
        <div className="lg:col-span-8 bg-surface-container-lowest p-5 rounded-2xl border border-outline-variant/40 shadow-elevation-1">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h2 className="text-sm font-bold text-on-surface">Recurring Revenue Growth (MRR in ₹ Lakhs)</h2>
              <p className="text-xs text-on-surface-variant">Consistent expansion across Enterprise and Mid-Market tiers</p>
            </div>
            <span className="text-[10px] font-mono font-bold bg-primary/10 text-primary px-2.5 py-1 rounded-full">
              Q4 RUN-RATE: ₹2.24 CR
            </span>
          </div>

          <div className="h-60 w-full">
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={REVENUE_DATA} margin={{ top: 10, right: 10, left: 0, bottom: 0 }}>
                <defs>
                  <linearGradient id="colorMRR" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#4f46e5" stopOpacity={0.35}/>
                    <stop offset="95%" stopColor="#4f46e5" stopOpacity={0.0}/>
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e2e8f0" />
                <XAxis dataKey="month" tick={{ fontSize: 11, fill: '#64748b' }} axisLine={{ stroke: '#cbd5e1' }} />
                <YAxis tick={{ fontSize: 11, fill: '#64748b' }} tickFormatter={(val) => `₹${val}L`} axisLine={{ stroke: '#cbd5e1' }} />
                <Tooltip
                  formatter={(val) => [`₹${val} Lakhs`, 'MRR']}
                  contentStyle={{ backgroundColor: '#ffffff', borderRadius: '8px', border: '1px solid #cbd5e1', fontSize: '12px' }}
                />
                <Area type="monotone" dataKey="mrr" stroke="#4f46e5" strokeWidth={2.5} fillOpacity={1} fill="url(#colorMRR)" />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Plan Distribution Breakdown (4 Cols) */}
        <div className="lg:col-span-4 bg-surface-container-lowest p-5 rounded-2xl border border-outline-variant/40 shadow-elevation-1 flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between mb-1">
              <h2 className="text-sm font-bold text-on-surface">Revenue by Plan Tier</h2>
              <span className="text-[10px] font-mono text-outline">PORTFOLIO WEIGHT</span>
            </div>
            <p className="text-xs text-on-surface-variant mb-4">Subscriber tier breakdown</p>

            <div className="space-y-3.5">
              <div>
                <div className="flex justify-between text-xs font-semibold mb-1">
                  <span className="text-on-surface">Enterprise Sentinel</span>
                  <span className="text-primary font-mono">54% (₹10.1L)</span>
                </div>
                <div className="w-full bg-surface-container h-2 rounded-full overflow-hidden">
                  <div className="bg-primary h-full rounded-full" style={{ width: '54%' }}></div>
                </div>
              </div>

              <div>
                <div className="flex justify-between text-xs font-semibold mb-1">
                  <span className="text-on-surface">Growth Scale</span>
                  <span className="text-secondary font-mono">32% (₹6.0L)</span>
                </div>
                <div className="w-full bg-surface-container h-2 rounded-full overflow-hidden">
                  <div className="bg-secondary h-full rounded-full" style={{ width: '32%' }}></div>
                </div>
              </div>

              <div>
                <div className="flex justify-between text-xs font-semibold mb-1">
                  <span className="text-on-surface">Starter Pro</span>
                  <span className="text-tertiary font-mono">14% (₹2.6L)</span>
                </div>
                <div className="w-full bg-surface-container h-2 rounded-full overflow-hidden">
                  <div className="bg-tertiary-container h-full rounded-full" style={{ width: '14%' }}></div>
                </div>
              </div>
            </div>
          </div>

          <div className="mt-4 pt-3 border-t border-surface-container flex items-center justify-between text-xs">
            <span className="text-on-surface-variant text-[11px]">Looking to optimize pricing?</span>
            <button
              onClick={() => navigate('/admin/plans')}
              className="text-primary font-bold text-[11px] hover:underline flex items-center gap-0.5"
            >
              <span>Manage Plans</span>
              <ChevronRight className="w-3 h-3" />
            </button>
          </div>
        </div>
      </div>

      {/* Prioritized Action Queues */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* Critical Overdue Accounts (8 Cols) */}
        <div className="lg:col-span-8 bg-surface-container-lowest rounded-2xl border border-outline-variant/40 shadow-elevation-1 overflow-hidden">
          <div className="p-4 border-b border-surface-container flex items-center justify-between">
            <div>
              <h2 className="text-sm font-bold text-on-surface">Immediate Overdue Invoices Requiring Follow-Up</h2>
              <p className="text-xs text-on-surface-variant">Linked to automated dunning &amp; recovery workflows</p>
            </div>
            <button
              onClick={() => navigate('/admin/dunning')}
              className="text-xs text-rose-700 font-bold hover:underline"
            >
              Open Dunning Console &rarr;
            </button>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-surface-container-low text-on-surface-variant uppercase font-semibold text-[10px] tracking-wider border-b border-outline-variant/20">
                <tr>
                  <th className="p-3">Customer Entity</th>
                  <th className="p-3">Invoice Ref</th>
                  <th className="p-3">Outstanding Amount</th>
                  <th className="p-3">Days Overdue</th>
                  <th className="p-3 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-surface-container-low">
                {[
                  { customer: 'Vertex Labs', inv: 'INV-2024-003', amount: 32000, days: 74, status: 'Escalated' },
                  { customer: 'Orion Fintech', inv: 'INV-2024-006', amount: 12000, days: 44, status: 'Partially Recovered' },
                  { customer: 'DataPulse Analytics', inv: 'INV-2024-009', amount: 15000, days: 31, status: 'Payment Promise' },
                  { customer: 'Vertex Labs (Q2)', inv: 'INV-2024-008', amount: 32000, days: 44, status: 'Reminder Sent' },
                ].map((row, idx) => (
                  <tr key={idx} className="hover:bg-surface-container-low/50">
                    <td className="p-3 font-semibold text-on-surface">{row.customer}</td>
                    <td className="p-3 font-mono text-primary font-bold">{row.inv}</td>
                    <td className="p-3 font-mono font-bold text-rose-700">₹{row.amount.toLocaleString('en-IN')}</td>
                    <td className="p-3 font-mono text-on-surface-variant">{row.days} Days</td>
                    <td className="p-3 text-right">
                      <button
                        onClick={() => navigate('/admin/dunning')}
                        className="px-2.5 py-1 bg-surface-container hover:bg-surface-container-high rounded text-xs font-semibold text-primary"
                      >
                        Action Case
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>

        {/* AI Heuristics Signals (4 Cols) */}
        <div className="lg:col-span-4 bg-surface-container-lowest rounded-2xl border border-outline-variant/40 shadow-elevation-1 p-5 space-y-3">
          <div className="flex items-center justify-between">
            <h2 className="text-sm font-bold text-on-surface flex items-center gap-1.5">
              <Sparkles className="w-4 h-4 text-primary" />
              <span>AI Revenue Recommendations</span>
            </h2>
            <span className="text-[10px] font-mono text-primary font-bold bg-primary/10 px-2 py-0.5 rounded-full">
              LIVE SIGNALS
            </span>
          </div>

          <div className="space-y-2.5 text-xs">
            <div className="p-3 rounded-xl bg-surface-container-low border border-surface-container">
              <div className="font-bold text-on-surface">High Churn Propensity: Vertex Labs</div>
              <p className="text-[11px] text-on-surface-variant mt-0.5">
                Seat utilization plummeted to 31%. Send automated retention quote.
              </p>
              <button
                onClick={() => navigate('/admin/quotations/new')}
                className="mt-2 text-primary font-bold text-[11px] hover:underline flex items-center gap-0.5"
              >
                <span>Draft Retention Proposal</span>
                <ChevronRight className="w-3 h-3" />
              </button>
            </div>

            <div className="p-3 rounded-xl bg-surface-container-low border border-surface-container">
              <div className="font-bold text-on-surface">Seat Saturation: CloudStream Inc</div>
              <p className="text-[11px] text-on-surface-variant mt-0.5">
                Reached 90% seat limit on Starter plan. Upsell to Growth tier.
              </p>
              <button
                onClick={() => navigate('/admin/quotations/new')}
                className="mt-2 text-primary font-bold text-[11px] hover:underline flex items-center gap-0.5"
              >
                <span>Issue Expansion Quote</span>
                <ChevronRight className="w-3 h-3" />
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
