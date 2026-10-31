import React, { useState } from 'react';
import { CalendarCheck, Play, CheckCircle2, Clock, ArrowUpRight } from 'lucide-react';
import { billingRuns as initialRuns } from '../../../data/mockData';

export default function BillingRunPage() {
  const [runs, setRuns] = useState(initialRuns || []);
  const [executing, setExecuting] = useState(false);

  const handleExecute = () => {
    setExecuting(true);
    setTimeout(() => {
      const newRun = {
        id: `BR-2024-${runs.length + 1}`,
        date: new Date().toISOString().split('T')[0],
        totalInvoices: 34,
        totalAmount: 1850000,
        status: 'Completed',
        successRate: '100%'
      };
      setRuns(prev => [newRun, ...prev]);
      setExecuting(false);
      alert('Batch billing run executed successfully. 34 invoices generated.');
    }, 1200);
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-xl font-bold text-on-surface">Billing Run Execution & Batch Invoicing</h1>
          <p className="text-xs text-on-surface-variant">
            Orchestrate automated end-of-month batch invoice generation and debit scheduling.
          </p>
        </div>
        <button
          disabled={executing}
          onClick={handleExecute}
          className="px-4 py-2 bg-primary hover:bg-primary-container text-white text-xs font-semibold rounded-lg flex items-center gap-1.5 shadow-sm transition-all"
        >
          <Play className="w-3.5 h-3.5 fill-current" />
          <span>{executing ? 'Executing Batch Run...' : 'Execute Immediate Billing Run'}</span>
        </button>
      </div>

      <div className="bg-surface-container-lowest border border-outline-variant/40 rounded-xl shadow-elevation-1 overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-surface-container-low text-on-surface-variant uppercase text-[10px] tracking-wider border-b border-outline-variant/20 font-semibold">
              <tr>
                <th className="p-3.5">Batch Run ID</th>
                <th className="p-3.5">Execution Date</th>
                <th className="p-3.5">Invoices Generated</th>
                <th className="p-3.5">Gross Batched Amount</th>
                <th className="p-3.5">Status</th>
                <th className="p-3.5">Reconciliation Rate</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-surface-container-low">
              {runs.map((r) => (
                <tr key={r.id} className="hover:bg-surface-container-low/60">
                  <td className="p-3.5 font-mono font-bold text-primary">{r.id}</td>
                  <td className="p-3.5 font-mono text-on-surface-variant">{r.date}</td>
                  <td className="p-3.5 font-mono">{r.totalInvoices} Invoices</td>
                  <td className="p-3.5 font-mono font-bold text-on-surface">₹{r.totalAmount.toLocaleString('en-IN')}</td>
                  <td className="p-3.5">
                    <span className="px-2 py-0.5 rounded-full text-[10px] font-semibold bg-emerald-50 text-emerald-700">
                      {r.status}
                    </span>
                  </td>
                  <td className="p-3.5 font-mono text-emerald-700 font-bold">{r.successRate}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}

