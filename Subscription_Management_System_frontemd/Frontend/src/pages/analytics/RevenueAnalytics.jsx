import React, { useState, useEffect } from 'react';
import { AreaChart, Area, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid, BarChart, Bar } from 'recharts';
import { RefreshCw, TrendingUp, AlertTriangle } from 'lucide-react';
import { analyticsService } from '../../services/analyticsService';

export default function RevenueAnalytics() {
  const [revenue, setRevenue] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const loadData = async () => {
    try {
      setLoading(true);
      setError('');
      const data = await analyticsService.getRevenue();
      setRevenue(data);
    } catch (err) {
      setError(err?.response?.data?.message || err?.message || 'Failed to load revenue analytics');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const chartData = (revenue?.revenueByMonth || []).map(m => ({
    month: m.month || m.period,
    billed: Number(m.billedAmount || m.revenue || 0),
    paid: Number(m.paidAmount || 0)
  }));

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-bold text-on-surface">Revenue Expansion & MRR Velocity</h1>
          <p className="text-xs text-on-surface-variant">Recurring revenue trajectories, billed vs collected reconciliation, and annualized run-rate.</p>
        </div>
        <button
          onClick={loadData}
          className="p-2 rounded-lg border border-outline-variant/40 hover:bg-surface-container text-on-surface-variant"
          title="Refresh Data"
        >
          <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
        </button>
      </div>

      {error && (
        <div className="p-3 bg-error-container/20 border border-error/30 text-error rounded-xl text-xs flex justify-between">
          <span>{error}</span>
          <button onClick={() => setError('')} className="font-bold">×</button>
        </div>
      )}

      {/* KPI Tiles */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
        <div className="p-4 bg-surface-container-lowest border border-outline-variant/40 rounded-xl">
          <div className="text-[10px] uppercase font-mono text-outline">TOTAL BILLED</div>
          <div className="text-xl font-bold text-on-surface mt-1">
            ₹{Number(revenue?.totalBilledRevenue || 0).toLocaleString('en-IN')}
          </div>
          <div className="text-[10px] text-on-surface-variant mt-0.5">Gross invoiced volume</div>
        </div>
        <div className="p-4 bg-surface-container-lowest border border-outline-variant/40 rounded-xl">
          <div className="text-[10px] uppercase font-mono text-emerald-700">COLLECTED REVENUE</div>
          <div className="text-xl font-bold text-emerald-700 mt-1 font-mono">
            ₹{Number(revenue?.totalPaidRevenue || 0).toLocaleString('en-IN')}
          </div>
          <div className="text-[10px] text-emerald-600 mt-0.5">Settled to bank ledger</div>
        </div>
        <div className="p-4 bg-surface-container-lowest border border-outline-variant/40 rounded-xl">
          <div className="text-[10px] uppercase font-mono text-rose-700">OVERDUE REVENUE</div>
          <div className="text-xl font-bold text-rose-700 mt-1 font-mono">
            ₹{Number(revenue?.overdueRevenue || 0).toLocaleString('en-IN')}
          </div>
          <div className="text-[10px] text-rose-600 mt-0.5">Payment arrears</div>
        </div>
        <div className="p-4 bg-surface-container-lowest border border-outline-variant/40 rounded-xl">
          <div className="text-[10px] uppercase font-mono text-primary">GROWTH VELOCITY</div>
          <div className="text-xl font-bold text-primary mt-1">
            {revenue?.revenueGrowthRate != null ? `${revenue.revenueGrowthRate}%` : 'N/A'}
          </div>
          <div className="text-[10px] text-on-surface-variant mt-0.5">Period over period</div>
        </div>
      </div>

      <div className="bg-surface-container-lowest p-6 rounded-2xl border border-outline-variant/40 shadow-elevation-1">
        <h2 className="text-sm font-bold text-on-surface mb-4">Monthly Revenue Invoiced vs Collected (₹)</h2>
        <div className="h-64 w-full">
          {loading ? (
            <div className="flex items-center justify-center h-full text-xs text-on-surface-variant">
              <RefreshCw className="w-5 h-5 animate-spin mr-2 text-primary" /> Loading trend...
            </div>
          ) : chartData.length > 0 ? (
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={chartData}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e2e8f0" />
                <XAxis dataKey="month" tick={{ fontSize: 11 }} />
                <YAxis tick={{ fontSize: 11 }} />
                <Tooltip formatter={v => [`₹${Number(v).toLocaleString('en-IN')}`]} />
                <Area type="monotone" dataKey="billed" stroke="#4f46e5" fill="#4f46e5" fillOpacity={0.2} strokeWidth={2} name="Billed Amount" />
                <Area type="monotone" dataKey="paid" stroke="#059669" fill="#059669" fillOpacity={0.2} strokeWidth={2} name="Paid Amount" />
              </AreaChart>
            </ResponsiveContainer>
          ) : (
            <div className="flex items-center justify-center h-full text-xs text-on-surface-variant">
              No monthly revenue data available.
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
