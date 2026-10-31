import React, { useState, useEffect } from 'react';
import { AreaChart, Area, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid, BarChart, Bar } from 'recharts';
import { RefreshCw, CreditCard, CheckCircle2, AlertTriangle } from 'lucide-react';
import { analyticsService } from '../../services/analyticsService';

export default function PaymentsAnalytics() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const loadData = async () => {
    try {
      setLoading(true);
      setError('');
      const res = await analyticsService.getPayments();
      setData(res);
    } catch (err) {
      setError(err?.response?.data?.message || err?.message || 'Failed to load payments analytics');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const chartData = (data?.monthlyPaymentCollection || []).map(m => ({
    month: m.month || m.period,
    amount: Number(m.amount || m.collectedAmount || 0)
  }));

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-bold text-on-surface">Payment Settlement Analytics</h1>
          <p className="text-xs text-on-surface-variant">Reconciliation telemetry, success rates across gateways, and collections ledger.</p>
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
          <div className="text-[10px] uppercase font-mono text-outline">TOTAL COLLECTED</div>
          <div className="text-xl font-bold text-emerald-700 mt-1 font-mono">
            ₹{Number(data?.totalCollected || 0).toLocaleString('en-IN')}
          </div>
          <div className="text-[10px] text-emerald-600 mt-0.5">Successful bank receipts</div>
        </div>
        <div className="p-4 bg-surface-container-lowest border border-outline-variant/40 rounded-xl">
          <div className="text-[10px] uppercase font-mono text-outline">PAYMENT ATTEMPTS</div>
          <div className="text-xl font-bold text-on-surface mt-1">{data?.totalPayments ?? 0} Transactions</div>
          <div className="text-[10px] text-on-surface-variant mt-0.5">Attempted checkouts</div>
        </div>
        <div className="p-4 bg-surface-container-lowest border border-outline-variant/40 rounded-xl">
          <div className="text-[10px] uppercase font-mono text-outline">SUCCESS RATE</div>
          <div className="text-xl font-bold text-primary mt-1">
            {data?.paymentSuccessRate != null ? `${data.paymentSuccessRate}%` : 'N/A'}
          </div>
          <div className="text-[10px] text-emerald-600 mt-0.5">Zero gateway drop-off</div>
        </div>
        <div className="p-4 bg-surface-container-lowest border border-outline-variant/40 rounded-xl">
          <div className="text-[10px] uppercase font-mono text-rose-700">OVERDUE ARREARS</div>
          <div className="text-xl font-bold text-rose-700 mt-1 font-mono">
            ₹{Number(data?.overdueInvoiceAmount || 0).toLocaleString('en-IN')}
          </div>
          <div className="text-[10px] text-rose-600 mt-0.5">Unpaid overdue balances</div>
        </div>
      </div>

      <div className="bg-surface-container-lowest p-6 rounded-2xl border border-outline-variant/40 shadow-elevation-1">
        <h2 className="text-sm font-bold text-on-surface mb-4">Monthly Payment Collections (₹)</h2>
        <div className="h-64 w-full">
          {chartData.length > 0 ? (
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={chartData}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e2e8f0" />
                <XAxis dataKey="month" tick={{ fontSize: 11 }} />
                <YAxis tick={{ fontSize: 11 }} />
                <Tooltip formatter={v => [`₹${Number(v).toLocaleString('en-IN')}`, 'Collections']} />
                <Bar dataKey="amount" fill="#10b981" radius={[4, 4, 0, 0]} name="Collected (₹)" />
              </BarChart>
            </ResponsiveContainer>
          ) : (
            <div className="flex items-center justify-center h-full text-xs text-on-surface-variant">
              No monthly collections data recorded.
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
