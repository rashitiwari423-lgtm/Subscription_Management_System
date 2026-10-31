import React, { useState, useEffect } from 'react';
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
import { useNavigate } from 'react-router-dom';
import { analyticsService } from '../../../services/analyticsService';
import { invoiceService } from '../../../services/invoiceService';
import { aiService } from '../../../services/aiService';

export default function AdminDashboard() {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [summary, setSummary] = useState(null);
  const [revenueHistory, setRevenueHistory] = useState([]);
  const [overdueInvoices, setOverdueInvoices] = useState([]);
  const [insights, setInsights] = useState([]);
  const [planDistribution, setPlanDistribution] = useState([]);

  const fetchDashboardData = async () => {
    setLoading(true);
    setError(null);
    try {
      const [dashData, revData, invData, insightData, plansData] = await Promise.allSettled([
        analyticsService.getDashboard(),
        analyticsService.getRevenue(),
        invoiceService.getAll({ status: 'OVERDUE' }),
        aiService.getInsights({ size: 3 }),
        analyticsService.getPlans(),
      ]);

      if (dashData.status === 'fulfilled') {
        setSummary(dashData.value);
      }
      if (revData.status === 'fulfilled' && revData.value?.monthlyTrend) {
        setRevenueHistory(revData.value.monthlyTrend);
      } else if (revData.status === 'fulfilled' && Array.isArray(revData.value)) {
        setRevenueHistory(revData.value);
      }
      if (invData.status === 'fulfilled') {
        setOverdueInvoices(invData.value || []);
      }
      if (insightData.status === 'fulfilled') {
        setInsights(insightData.value || []);
      }
      if (plansData.status === 'fulfilled') {
        setPlanDistribution(plansData.value || []);
      }
    } catch (err) {
      setError(err.userMessage || 'Unable to load dashboard data');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDashboardData();
  }, []);

  if (loading) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[400px] space-y-3">
        <RefreshCw className="w-8 h-8 text-primary animate-spin" />
        <p className="text-xs text-on-surface-variant font-medium">Loading real-time financial telemetry...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="p-6 bg-error-container/20 border border-error/30 rounded-2xl text-center space-y-3">
        <AlertTriangle className="w-8 h-8 text-error mx-auto" />
        <h2 className="text-sm font-bold text-on-surface">Unable to load dashboard data</h2>
        <p className="text-xs text-on-surface-variant">{error}</p>
        <button
          onClick={fetchDashboardData}
          className="px-4 py-2 bg-primary text-white text-xs font-semibold rounded-lg hover:bg-primary-container"
        >
          Retry
        </button>
      </div>
    );
  }

  const revenue = summary?.revenue || {};
  const subs = summary?.subscriptions || {};
  const customers = summary?.customers || {};
  const payments = summary?.payments || {};
  const churn = summary?.churn || {};
  const opt = summary?.optimization || {};

  const formatCurr = (val) => {
    const num = Number(val || 0);
    return `₹${num.toLocaleString('en-IN', { maximumFractionDigits: 2 })}`;
  };

  return (
    <div className="space-y-6">
      {/* Page Title & Operational Status */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3">
        <div>
          <h1 className="text-xl font-bold text-on-surface">Financial &amp; Subscription Overview</h1>
          <p className="text-xs text-on-surface-variant mt-0.5">
            Key operational metrics, ARR growth trajectory, and real-time backend analytics.
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
            onClick={() => navigate('/admin/invoices')}
            className="px-3.5 py-1.5 rounded-lg bg-rose-700 hover:bg-rose-800 text-white text-xs font-semibold flex items-center gap-1.5 shadow-sm"
          >
            <AlertTriangle className="w-3.5 h-3.5" />
            <span>Manage Invoices</span>
          </button>
        </div>
      </div>

      {/* Core KPI Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="bg-surface-container-lowest p-5 rounded-2xl border border-outline-variant/40 shadow-elevation-1">
          <div className="flex items-center justify-between text-on-surface-variant text-xs mb-2">
            <span className="font-bold uppercase tracking-wider text-[10px]">Monthly Recurring Revenue</span>
            <span className="inline-flex items-center text-emerald-700 bg-emerald-50 px-1.5 py-0.5 rounded text-[10px] font-semibold">
              <TrendingUp className="w-3 h-3 mr-0.5" /> {revenue.revenueGrowthRate ? `${revenue.revenueGrowthRate > 0 ? '+' : ''}${revenue.revenueGrowthRate}%` : 'Stable'}
            </span>
          </div>
          <div className="text-2xl font-extrabold text-on-surface font-tabular">
            {formatCurr(subs.monthlyRecurringRevenue || revenue.totalBilledRevenue)}
          </div>
          <div className="text-[11px] text-on-surface-variant mt-1">
            Total Billed: {formatCurr(revenue.totalBilledRevenue)}
          </div>
        </div>

        <div className="bg-surface-container-lowest p-5 rounded-2xl border border-outline-variant/40 shadow-elevation-1">
          <div className="flex items-center justify-between text-on-surface-variant text-xs mb-2">
            <span className="font-bold uppercase tracking-wider text-[10px]">Active Subscriptions</span>
            <span className="inline-flex items-center text-emerald-700 bg-emerald-50 px-1.5 py-0.5 rounded text-[10px] font-semibold">
              {customers.totalCustomers || 0} Total Customers
            </span>
          </div>
          <div className="text-2xl font-extrabold text-on-surface font-tabular">
            {subs.activeSubscriptions || 0}
          </div>
          <div className="text-[11px] text-on-surface-variant mt-1">
            Total Subscriptions: {subs.totalSubscriptions || 0} • Paused: {subs.pausedSubscriptions || 0}
          </div>
        </div>

        <div className="bg-surface-container-lowest p-5 rounded-2xl border border-outline-variant/40 shadow-elevation-1">
          <div className="flex items-center justify-between text-on-surface-variant text-xs mb-2">
            <span className="font-bold uppercase tracking-wider text-[10px]">Overdue Invoices</span>
            <span className={`inline-flex items-center px-1.5 py-0.5 rounded text-[10px] font-semibold ${
              (revenue.overdueRevenue || 0) > 0 ? 'text-rose-700 bg-rose-50' : 'text-emerald-700 bg-emerald-50'
            }`}>
              {(revenue.overdueRevenue || 0) > 0 ? 'Needs Attention' : 'Healthy'}
            </span>
          </div>
          <div className="text-2xl font-extrabold text-rose-700 font-tabular">
            {formatCurr(revenue.overdueRevenue || 0)}
          </div>
          <div className="text-[11px] text-on-surface-variant mt-1">
            Outstanding: {formatCurr(revenue.outstandingRevenue || 0)}
          </div>
        </div>

        <div className="bg-surface-container-lowest p-5 rounded-2xl border border-outline-variant/40 shadow-elevation-1">
          <div className="flex items-center justify-between text-on-surface-variant text-xs mb-2">
            <span className="font-bold uppercase tracking-wider text-[10px]">Potential Savings</span>
            <span className="inline-flex items-center text-primary bg-primary/10 px-1.5 py-0.5 rounded text-[10px] font-semibold">
              AI Optimization
            </span>
          </div>
          <div className="text-2xl font-extrabold text-primary font-tabular">
            {formatCurr(opt.potentialMonthlySavings || 0)}
          </div>
          <div className="text-[11px] text-on-surface-variant mt-1">
            {opt.unusedResources || 0} unused resources detected
          </div>
        </div>
      </div>

      {/* Main Charts Row */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* Revenue Velocity Curve */}
        <div className="lg:col-span-8 bg-surface-container-lowest p-5 rounded-2xl border border-outline-variant/40 shadow-elevation-1">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h2 className="text-sm font-bold text-on-surface">Revenue Growth &amp; Monthly Trends</h2>
              <p className="text-xs text-on-surface-variant">Deterministic historical billed &amp; collected totals</p>
            </div>
            <span className="text-[10px] font-mono font-bold bg-primary/10 text-primary px-2.5 py-1 rounded-full">
              COLLECTED: {formatCurr(payments.totalCollected || revenue.totalPaidRevenue)}
            </span>
          </div>

          <div className="h-60 w-full">
            {revenueHistory.length > 0 ? (
              <ResponsiveContainer width="100%" height="100%">
                <AreaChart data={revenueHistory} margin={{ top: 10, right: 10, left: 0, bottom: 0 }}>
                  <defs>
                    <linearGradient id="colorMRR" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor="#4f46e5" stopOpacity={0.35}/>
                      <stop offset="95%" stopColor="#4f46e5" stopOpacity={0.0}/>
                    </linearGradient>
                  </defs>
                  <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e2e8f0" />
                  <XAxis dataKey="month" tick={{ fontSize: 11, fill: '#64748b' }} axisLine={{ stroke: '#cbd5e1' }} />
                  <YAxis tick={{ fontSize: 11, fill: '#64748b' }} axisLine={{ stroke: '#cbd5e1' }} />
                  <Tooltip
                    formatter={(val) => [formatCurr(val), 'Revenue']}
                    contentStyle={{ backgroundColor: '#ffffff', borderRadius: '8px', border: '1px solid #cbd5e1', fontSize: '12px' }}
                  />
                  <Area type="monotone" dataKey="totalBilled" stroke="#4f46e5" strokeWidth={2.5} fillOpacity={1} fill="url(#colorMRR)" />
                </AreaChart>
              </ResponsiveContainer>
            ) : (
              <div className="flex flex-col items-center justify-center h-full text-on-surface-variant text-xs">
                <Calendar className="w-8 h-8 opacity-40 mb-1" />
                <span>Revenue trends will populate as billing cycles execute</span>
              </div>
            )}
          </div>
        </div>

        {/* Plan Distribution Breakdown */}
        <div className="lg:col-span-4 bg-surface-container-lowest p-5 rounded-2xl border border-outline-variant/40 shadow-elevation-1 flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between mb-1">
              <h2 className="text-sm font-bold text-on-surface">Revenue by Plan</h2>
              <span className="text-[10px] font-mono text-outline">PORTFOLIO</span>
            </div>
            <p className="text-xs text-on-surface-variant mb-4">Live active plans performance</p>

            {planDistribution.length > 0 ? (
              <div className="space-y-3.5">
                {planDistribution.slice(0, 4).map((plan, i) => (
                  <div key={i}>
                    <div className="flex justify-between text-xs font-semibold mb-1">
                      <span className="text-on-surface truncate">{plan.planName || `Plan ${plan.planId}`}</span>
                      <span className="text-primary font-mono">{formatCurr(plan.revenue || 0)}</span>
                    </div>
                    <div className="w-full bg-surface-container h-2 rounded-full overflow-hidden">
                      <div
                        className="bg-primary h-full rounded-full"
                        style={{ width: `${Math.min(100, (plan.activeSubscriptions || 1) * 25)}%` }}
                      ></div>
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <div className="text-center py-8 text-xs text-on-surface-variant">
                No active plan subscriptions yet
              </div>
            )}
          </div>

          <div className="mt-4 pt-3 border-t border-surface-container flex items-center justify-between text-xs">
            <span className="text-on-surface-variant text-[11px]">Configure pricing tiers:</span>
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
        {/* Critical Overdue Invoices */}
        <div className="lg:col-span-8 bg-surface-container-lowest rounded-2xl border border-outline-variant/40 shadow-elevation-1 overflow-hidden">
          <div className="p-4 border-b border-surface-container flex items-center justify-between">
            <div>
              <h2 className="text-sm font-bold text-on-surface">Overdue Invoices Requiring Follow-Up</h2>
              <p className="text-xs text-on-surface-variant">Real invoices past payment maturity</p>
            </div>
            <button
              onClick={() => navigate('/admin/invoices')}
              className="text-xs text-rose-700 font-bold hover:underline"
            >
              View Invoices &rarr;
            </button>
          </div>

          <div className="overflow-x-auto">
            {overdueInvoices.length > 0 ? (
              <table className="w-full text-left text-xs">
                <thead className="bg-surface-container-low text-on-surface-variant uppercase font-semibold text-[10px] tracking-wider border-b border-outline-variant/20">
                  <tr>
                    <th className="p-3">Invoice Number</th>
                    <th className="p-3">Customer ID</th>
                    <th className="p-3">Balance Amount</th>
                    <th className="p-3">Due Date</th>
                    <th className="p-3 text-right">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-surface-container-low">
                  {overdueInvoices.map((inv) => (
                    <tr key={inv.id} className="hover:bg-surface-container-low/50">
                      <td className="p-3 font-mono text-primary font-bold">{inv.invoiceNumber}</td>
                      <td className="p-3 font-semibold text-on-surface">Customer #{inv.customerId}</td>
                      <td className="p-3 font-mono font-bold text-rose-700">{formatCurr(inv.balanceAmount)}</td>
                      <td className="p-3 font-mono text-on-surface-variant">{inv.dueDate}</td>
                      <td className="p-3 text-right">
                        <button
                          onClick={() => navigate(`/admin/invoices/${inv.id}`)}
                          className="px-2.5 py-1 bg-surface-container hover:bg-surface-container-high rounded text-xs font-semibold text-primary"
                        >
                          View Invoice
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            ) : (
              <div className="p-8 text-center text-xs text-on-surface-variant">
                <CheckCircle2 className="w-8 h-8 text-emerald-600 mx-auto mb-2 opacity-60" />
                <span>No overdue invoices at this time. All collections are up to date!</span>
              </div>
            )}
          </div>
        </div>

        {/* AI Recommendations */}
        <div className="lg:col-span-4 bg-surface-container-lowest rounded-2xl border border-outline-variant/40 shadow-elevation-1 p-5 space-y-3">
          <div className="flex items-center justify-between">
            <h2 className="text-sm font-bold text-on-surface flex items-center gap-1.5">
              <Sparkles className="w-4 h-4 text-primary" />
              <span>AI Command Center Insights</span>
            </h2>
            <button
              onClick={() => navigate('/admin/ai-command-center')}
              className="text-[10px] font-mono text-primary font-bold hover:underline"
            >
              VIEW ALL
            </button>
          </div>

          <div className="space-y-2.5 text-xs">
            {insights.length > 0 ? (
              insights.map((ins) => (
                <div key={ins.id} className="p-3 rounded-xl bg-surface-container-low border border-surface-container">
                  <div className="flex items-center justify-between mb-1">
                    <span className="font-bold text-on-surface">{ins.title}</span>
                    <span className="text-[10px] font-semibold text-rose-700 bg-rose-50 px-1.5 py-0.5 rounded">
                      {ins.severity}
                    </span>
                  </div>
                  <p className="text-[11px] text-on-surface-variant line-clamp-2">
                    {ins.description}
                  </p>
                  <button
                    onClick={() => navigate(`/admin/ai-command-center/insights/${ins.id}`)}
                    className="mt-2 text-primary font-bold text-[11px] hover:underline flex items-center gap-0.5"
                  >
                    <span>Inspect Insight</span>
                    <ChevronRight className="w-3 h-3" />
                  </button>
                </div>
              ))
            ) : (
              <div className="p-6 text-center text-xs text-on-surface-variant">
                <span>No urgent AI anomalies detected. Ask a question in the AI Command Center to inspect telemetry.</span>
                <button
                  onClick={() => navigate('/admin/ai-command-center')}
                  className="mt-3 block mx-auto px-3 py-1 bg-primary text-white text-xs rounded-lg font-semibold"
                >
                  Open AI Chat
                </button>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
