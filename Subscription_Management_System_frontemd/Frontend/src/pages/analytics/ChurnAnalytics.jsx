import React, { useState, useEffect } from 'react';
import { AreaChart, Area, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid, BarChart, Bar } from 'recharts';
import { RefreshCw, TrendingDown, AlertTriangle } from 'lucide-react';
import { analyticsService } from '../../services/analyticsService';

export default function ChurnAnalytics() {
  const [churn, setChurn] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const loadData = async () => {
    try {
      setLoading(true);
      setError('');
      const data = await analyticsService.getChurn();
      setChurn(data);
    } catch (err) {
      setError(err?.response?.data?.message || err?.message || 'Failed to load churn analytics');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const chartData = (churn?.churnByMonth || []).map(m => ({
    month: m.month || m.period,
    rate: m.churnRate != null ? Number(m.churnRate) : 0,
    count: m.churnedCount || 0
  }));

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-bold text-on-surface">Client Churn & Attrition Analysis</h1>
          <p className="text-xs text-on-surface-variant">Retention telemetry, contract cancellations, and attrition rates.</p>
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
          <div className="text-[10px] uppercase font-mono text-outline">NET CHURN RATE</div>
          <div className="text-xl font-bold text-rose-700 mt-1">
            {churn?.churnRate != null ? `${churn.churnRate}%` : '0%'}
          </div>
          <div className="text-[10px] text-on-surface-variant mt-0.5">Calculated attrition rate</div>
        </div>
        <div className="p-4 bg-surface-container-lowest border border-outline-variant/40 rounded-xl">
          <div className="text-[10px] uppercase font-mono text-outline">TOTAL CHURNED</div>
          <div className="text-xl font-bold text-on-surface mt-1">{churn?.totalChurned ?? 0} Accounts</div>
          <div className="text-[10px] text-on-surface-variant mt-0.5">Terminated contracts</div>
        </div>
        <div className="p-4 bg-surface-container-lowest border border-outline-variant/40 rounded-xl">
          <div className="text-[10px] uppercase font-mono text-outline">EXPIRED CONTRACTS</div>
          <div className="text-xl font-bold text-amber-700 mt-1">{churn?.expiredSubscriptions ?? 0} Accounts</div>
          <div className="text-[10px] text-on-surface-variant mt-0.5">Non-renewed contracts</div>
        </div>
        <div className="p-4 bg-surface-container-lowest border border-outline-variant/40 rounded-xl">
          <div className="text-[10px] uppercase font-mono text-outline">CLOSED PREMATURELY</div>
          <div className="text-xl font-bold text-rose-700 mt-1">{churn?.closedSubscriptions ?? 0} Accounts</div>
          <div className="text-[10px] text-on-surface-variant mt-0.5">Voluntary cancellations</div>
        </div>
      </div>

      <div className="bg-surface-container-lowest p-6 rounded-2xl border border-outline-variant/40 shadow-elevation-1">
        <h2 className="text-sm font-bold text-on-surface mb-4">Monthly Attrition Rate (%)</h2>
        <div className="h-64 w-full">
          {chartData.length > 0 ? (
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={chartData}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e2e8f0" />
                <XAxis dataKey="month" tick={{ fontSize: 11 }} />
                <YAxis tick={{ fontSize: 11 }} />
                <Tooltip formatter={v => [`${v}%`, 'Churn Rate']} />
                <Area type="monotone" dataKey="rate" stroke="#ef4444" fill="#ef4444" fillOpacity={0.2} strokeWidth={2} name="Churn Rate" />
              </AreaChart>
            </ResponsiveContainer>
          ) : (
            <div className="flex items-center justify-center h-full text-xs text-on-surface-variant">
              No monthly churn telemetry recorded.
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
