import React, { useState, useEffect } from 'react';
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid, PieChart, Pie, Cell } from 'recharts';
import { RefreshCw, Users, ShieldCheck, AlertTriangle } from 'lucide-react';
import { analyticsService } from '../../services/analyticsService';

export default function SubscriptionAnalytics() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const loadData = async () => {
    try {
      setLoading(true);
      setError('');
      const res = await analyticsService.getSubscriptions();
      setData(res);
    } catch (err) {
      setError(err?.response?.data?.message || err?.message || 'Failed to load subscription analytics');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const chartData = (data?.newSubscriptionsByMonth || []).map(m => ({
    month: m.month || m.period,
    count: m.count || 0
  }));

  const pieData = data ? [
    { name: 'Active', value: Number(data.activeSubscriptions || 0), color: '#10b981' },
    { name: 'Paused', value: Number(data.pausedSubscriptions || 0), color: '#f59e0b' },
    { name: 'Pending', value: Number(data.pendingSubscriptions || 0), color: '#6366f1' },
    { name: 'Closed', value: Number(data.closedSubscriptions || 0), color: '#f43f5e' }
  ].filter(d => d.value > 0) : [];

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-bold text-on-surface">Subscription Lifecycle Analytics</h1>
          <p className="text-xs text-on-surface-variant">Contract distributions, activation velocity, churned contracts, and plan density.</p>
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
          <div className="text-[10px] uppercase font-mono text-outline">TOTAL CONTRACTS</div>
          <div className="text-xl font-bold text-on-surface mt-1">{data?.totalSubscriptions ?? 0}</div>
          <div className="text-[10px] text-on-surface-variant mt-0.5">All created subscriptions</div>
        </div>
        <div className="p-4 bg-surface-container-lowest border border-outline-variant/40 rounded-xl">
          <div className="text-[10px] uppercase font-mono text-emerald-700">ACTIVE CONTRACTS</div>
          <div className="text-xl font-bold text-emerald-700 mt-1">{data?.activeSubscriptions ?? 0}</div>
          <div className="text-[10px] text-emerald-600 mt-0.5">Generating recurring revenue</div>
        </div>
        <div className="p-4 bg-surface-container-lowest border border-outline-variant/40 rounded-xl">
          <div className="text-[10px] uppercase font-mono text-amber-700">PAUSED CONTRACTS</div>
          <div className="text-xl font-bold text-amber-700 mt-1">{data?.pausedSubscriptions ?? 0}</div>
          <div className="text-[10px] text-amber-600 mt-0.5">Billing suspended</div>
        </div>
        <div className="p-4 bg-surface-container-lowest border border-outline-variant/40 rounded-xl">
          <div className="text-[10px] uppercase font-mono text-rose-700">CLOSED / TERMINATED</div>
          <div className="text-xl font-bold text-rose-700 mt-1">{data?.closedSubscriptions ?? 0}</div>
          <div className="text-[10px] text-rose-600 mt-0.5">Historical terminations</div>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-12 gap-6">
        <div className="md:col-span-8 bg-surface-container-lowest p-6 rounded-2xl border border-outline-variant/40 shadow-elevation-1">
          <h2 className="text-sm font-bold text-on-surface mb-4">Monthly New Subscriptions Acquisition</h2>
          <div className="h-64 w-full">
            {chartData.length > 0 ? (
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={chartData}>
                  <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e2e8f0" />
                  <XAxis dataKey="month" tick={{ fontSize: 11 }} />
                  <YAxis tick={{ fontSize: 11 }} />
                  <Tooltip />
                  <Bar dataKey="count" fill="#4f46e5" radius={[4, 4, 0, 0]} name="New Contracts" />
                </BarChart>
              </ResponsiveContainer>
            ) : (
              <div className="flex items-center justify-center h-full text-xs text-on-surface-variant">
                No monthly acquisition data available.
              </div>
            )}
          </div>
        </div>

        <div className="md:col-span-4 bg-surface-container-lowest p-6 rounded-2xl border border-outline-variant/40 shadow-elevation-1 flex flex-col justify-between">
          <h2 className="text-sm font-bold text-on-surface mb-2">Status Distribution</h2>
          <div className="h-48 w-full flex items-center justify-center">
            {pieData.length > 0 ? (
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie data={pieData} dataKey="value" nameKey="name" cx="50%" cy="50%" innerRadius={40} outerRadius={70}>
                    {pieData.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={entry.color} />
                    ))}
                  </Pie>
                  <Tooltip />
                </PieChart>
              </ResponsiveContainer>
            ) : (
              <div className="text-xs text-on-surface-variant">No status data</div>
            )}
          </div>
          <div className="space-y-1 pt-2 border-t border-surface-container text-xs">
            {pieData.map(p => (
              <div key={p.name} className="flex justify-between items-center">
                <span className="flex items-center gap-1.5">
                  <span className="w-2.5 h-2.5 rounded-full" style={{ backgroundColor: p.color }} />
                  {p.name}
                </span>
                <span className="font-mono font-bold text-on-surface">{p.value}</span>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
