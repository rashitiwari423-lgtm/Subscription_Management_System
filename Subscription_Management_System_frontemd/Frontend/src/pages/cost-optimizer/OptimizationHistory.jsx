import React, { useState, useEffect } from 'react';
import { ArrowLeft, TrendingDown, CheckCircle2, RefreshCw, AlertTriangle } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { optimizationService } from '../../services/optimizationService';

export default function OptimizationHistory() {
  const navigate = useNavigate();
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const loadHistory = async () => {
    try {
      setLoading(true);
      setError('');
      const data = await optimizationService.getHistory();
      const list = Array.isArray(data) ? data : (data?.content || []);
      setHistory(list);
    } catch (err) {
      setError(err?.response?.data?.message || err?.message || 'Failed to load optimization history');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadHistory();
  }, []);

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-3">
          <button
            onClick={() => navigate('/admin/cost-optimizer')}
            className="p-1.5 rounded-lg border border-outline-variant/40 hover:bg-surface-container"
          >
            <ArrowLeft className="w-4 h-4" />
          </button>
          <div>
            <h1 className="text-xl font-bold text-on-surface">Optimization Realization History</h1>
            <p className="text-xs text-on-surface-variant">Cumulative savings achieved through automated down-scaling policies.</p>
          </div>
        </div>
        <button
          onClick={loadHistory}
          className="p-2 rounded-lg border border-outline-variant/40 hover:bg-surface-container text-on-surface-variant"
          title="Refresh History"
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

      <div className="bg-surface-container-lowest border border-outline-variant/40 rounded-xl shadow-elevation-1 overflow-hidden">
        <table className="w-full text-left text-xs">
          <thead className="bg-surface-container-low text-on-surface-variant uppercase text-[10px] font-semibold">
            <tr>
              <th className="p-3.5">Optimization ID</th>
              <th className="p-3.5">Customer / Subscription</th>
              <th className="p-3.5">Recommendation</th>
              <th className="p-3.5">Monthly Savings</th>
              <th className="p-3.5">Timestamp</th>
              <th className="p-3.5">Status</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-surface-container">
            {loading ? (
              <tr>
                <td colSpan="6" className="p-8 text-center text-on-surface-variant">
                  <RefreshCw className="w-5 h-5 animate-spin mx-auto mb-2 text-primary" />
                  <span>Loading optimization history...</span>
                </td>
              </tr>
            ) : history.length === 0 ? (
              <tr>
                <td colSpan="6" className="p-8 text-center text-on-surface-variant">
                  No optimization history records logged yet.
                </td>
              </tr>
            ) : (
              history.map((h) => (
                <tr key={h.id} className="hover:bg-surface-container-low/50">
                  <td className="p-3.5 font-mono font-bold text-primary">OPT-{h.id}</td>
                  <td className="p-3.5 font-bold text-on-surface">
                    {h.customerCompanyName || `Customer #${h.customerId}`} ({h.subscriptionNumber || `SUB-${h.subscriptionId}`})
                  </td>
                  <td className="p-3.5 text-on-surface">{h.proposedAction || h.recommendation}</td>
                  <td className="p-3.5 font-mono text-emerald-700 font-bold">
                    ₹{Number(h.potentialMonthlySavings || 0).toLocaleString('en-IN')}/mo
                  </td>
                  <td className="p-3.5 font-mono text-on-surface-variant">
                    {h.createdAt ? new Date(h.createdAt).toLocaleDateString() : 'N/A'}
                  </td>
                  <td className="p-3.5">
                    <span className="px-2 py-0.5 rounded-full text-[10px] font-semibold bg-emerald-50 text-emerald-700 border border-emerald-200">
                      {h.status}
                    </span>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
