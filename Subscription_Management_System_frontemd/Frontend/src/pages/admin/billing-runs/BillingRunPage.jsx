import React, { useState } from 'react';
import { Play, CheckCircle2, AlertTriangle, ArrowLeft, RefreshCw, Clock } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { billingService } from '../../../services/billingService';

export default function BillingRunPage() {
  const navigate = useNavigate();
  const [executing, setExecuting] = useState(false);
  const [error, setError] = useState('');
  const [latestSummary, setLatestSummary] = useState(null);
  const [runHistory, setRunHistory] = useState([
    {
      id: 'BR-INIT-2026',
      date: new Date().toISOString().split('T')[0],
      subscriptionsChecked: 1,
      invoicesCreated: 1,
      overdueMarked: 0,
      status: 'COMPLETED'
    }
  ]);

  const handleExecute = async () => {
    if (!window.confirm('Are you sure you want to run recurring billing? This will process due subscriptions and mark overdue invoices.')) {
      return;
    }

    try {
      setExecuting(true);
      setError('');
      const summary = await billingService.triggerRecurringBilling();
      setLatestSummary(summary);
      setRunHistory(prev => [
        {
          id: `BR-RUN-${Date.now().toString().slice(-6)}`,
          date: new Date().toISOString().split('T')[0],
          subscriptionsChecked: summary.subscriptionsChecked,
          invoicesCreated: summary.invoicesCreated,
          overdueMarked: summary.overdueInvoicesUpdated,
          status: summary.failed > 0 ? 'PARTIAL' : 'COMPLETED'
        },
        ...prev
      ]);
    } catch (err) {
      setError(err?.response?.data?.message || err?.message || 'Recurring billing execution failed');
    } finally {
      setExecuting(false);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div className="flex items-center gap-3">
          <button
            onClick={() => navigate('/admin/invoices')}
            className="p-1.5 rounded-lg border border-outline-variant/40 hover:bg-surface-container"
          >
            <ArrowLeft className="w-4 h-4" />
          </button>
          <div>
            <h1 className="text-xl font-bold text-on-surface">Billing Run Execution & Batch Invoicing</h1>
            <p className="text-xs text-on-surface-variant">
              Orchestrate automated recurring billing runs, invoice generation, and status reconciliations.
            </p>
          </div>
        </div>
        <button
          disabled={executing}
          onClick={handleExecute}
          className="px-4 py-2 bg-primary hover:bg-primary-container text-white text-xs font-semibold rounded-lg flex items-center gap-1.5 shadow-sm transition-all"
        >
          <Play className={`w-3.5 h-3.5 fill-current ${executing ? 'animate-spin' : ''}`} />
          <span>{executing ? 'Executing Batch Run...' : 'Execute Immediate Billing Run'}</span>
        </button>
      </div>

      {error && (
        <div className="p-3 bg-error-container/20 border border-error/30 text-error rounded-xl text-xs flex justify-between">
          <span>{error}</span>
          <button onClick={() => setError('')} className="font-bold">×</button>
        </div>
      )}

      {latestSummary && (
        <div className="p-5 bg-surface-container-lowest border border-emerald-200 rounded-2xl shadow-elevation-1 space-y-3">
          <div className="flex items-center gap-2 text-emerald-800 font-bold text-sm">
            <CheckCircle2 className="w-5 h-5 text-emerald-600" />
            <span>Recurring Billing Run Completed</span>
          </div>
          <div className="grid grid-cols-2 sm:grid-cols-5 gap-3 pt-2">
            <div className="p-3 bg-surface-container/30 rounded-xl">
              <div className="text-[10px] uppercase font-mono text-outline">Checked</div>
              <div className="text-lg font-bold text-on-surface mt-1">{latestSummary.subscriptionsChecked}</div>
            </div>
            <div className="p-3 bg-surface-container/30 rounded-xl">
              <div className="text-[10px] uppercase font-mono text-emerald-700">Created Invoices</div>
              <div className="text-lg font-bold text-emerald-700 mt-1">{latestSummary.invoicesCreated}</div>
            </div>
            <div className="p-3 bg-surface-container/30 rounded-xl">
              <div className="text-[10px] uppercase font-mono text-rose-700">Marked Overdue</div>
              <div className="text-lg font-bold text-rose-700 mt-1">{latestSummary.overdueInvoicesUpdated}</div>
            </div>
            <div className="p-3 bg-surface-container/30 rounded-xl">
              <div className="text-[10px] uppercase font-mono text-outline">Skipped</div>
              <div className="text-lg font-bold text-on-surface mt-1">{latestSummary.alreadyProcessed}</div>
            </div>
            <div className="p-3 bg-surface-container/30 rounded-xl">
              <div className="text-[10px] uppercase font-mono text-rose-700">Failed</div>
              <div className="text-lg font-bold text-on-surface mt-1">{latestSummary.failed}</div>
            </div>
          </div>
        </div>
      )}

      <div className="bg-surface-container-lowest border border-outline-variant/40 rounded-xl shadow-elevation-1 overflow-hidden">
        <div className="p-4 border-b border-surface-container-low font-bold text-xs text-on-surface">
          Execution Audit Log
        </div>
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-surface-container-low text-on-surface-variant uppercase text-[10px] tracking-wider border-b border-outline-variant/20 font-semibold">
              <tr>
                <th className="p-3.5">Batch Run ID</th>
                <th className="p-3.5">Execution Date</th>
                <th className="p-3.5">Subscriptions Checked</th>
                <th className="p-3.5">Invoices Generated</th>
                <th className="p-3.5">Overdue Reconciled</th>
                <th className="p-3.5">Status</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-surface-container-low">
              {runHistory.map((r) => (
                <tr key={r.id} className="hover:bg-surface-container-low/60">
                  <td className="p-3.5 font-mono font-bold text-primary">{r.id}</td>
                  <td className="p-3.5 font-mono text-on-surface-variant">{r.date}</td>
                  <td className="p-3.5 font-mono">{r.subscriptionsChecked}</td>
                  <td className="p-3.5 font-mono font-bold text-emerald-700">{r.invoicesCreated} Invoices</td>
                  <td className="p-3.5 font-mono text-rose-700">{r.overdueMarked}</td>
                  <td className="p-3.5">
                    <span className="px-2 py-0.5 rounded-full text-[10px] font-semibold bg-emerald-50 text-emerald-700 border border-emerald-200">
                      {r.status}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
