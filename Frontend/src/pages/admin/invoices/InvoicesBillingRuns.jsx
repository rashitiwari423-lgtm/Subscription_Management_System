import React, { useState } from 'react';
import { Receipt, Search, Filter, Download, ArrowUpRight, CheckCircle2, Clock } from 'lucide-react';
import { invoices as initialInvoices } from '../../../data/mockData';
import { useNavigate } from 'react-router-dom';

export default function InvoicesBillingRuns() {
  const [invoiceList, setInvoiceList] = useState(initialInvoices || []);
  const [statusFilter, setStatusFilter] = useState('ALL');
  const navigate = useNavigate();

  const filtered = invoiceList.filter(i => statusFilter === 'ALL' || i.status === statusFilter);

  return (
    <div className="space-y-5">
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-xl font-bold text-on-surface">Invoices & Billing Execution</h1>
          <p className="text-xs text-on-surface-variant">
            Automated invoice generation, GST tax breakdown, and payment status tracking.
          </p>
        </div>
        <div className="flex items-center gap-2">
          <button
            onClick={() => navigate('/admin/billing-runs')}
            className="px-3.5 py-2 rounded-lg bg-primary hover:bg-primary-container text-white text-xs font-semibold flex items-center gap-1.5 shadow-sm"
          >
            <span>Execute Batch Run</span>
            <ArrowUpRight className="w-3.5 h-3.5" />
          </button>
        </div>
      </div>

      <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
        <div className="p-3.5 bg-surface-container-lowest border border-outline-variant/40 rounded-xl">
          <div className="text-[10px] uppercase font-mono text-outline">TOTAL INVOICED</div>
          <div className="text-xl font-bold text-on-surface mt-1">₹{invoiceList.reduce((acc, i) => acc + i.amount, 0).toLocaleString('en-IN')}</div>
        </div>
        <div className="p-3.5 bg-surface-container-lowest border border-outline-variant/40 rounded-xl">
          <div className="text-[10px] uppercase font-mono text-emerald-700">COLLECTED (PAID)</div>
          <div className="text-xl font-bold text-emerald-700 mt-1">
            ₹{invoiceList.filter(i => i.status === 'Paid').reduce((acc, i) => acc + i.amount, 0).toLocaleString('en-IN')}
          </div>
        </div>
        <div className="p-3.5 bg-surface-container-lowest border border-outline-variant/40 rounded-xl">
          <div className="text-[10px] uppercase font-mono text-rose-700">OVERDUE ARREARS</div>
          <div className="text-xl font-bold text-rose-700 mt-1">
            ₹{invoiceList.filter(i => i.status === 'Overdue').reduce((acc, i) => acc + i.amount, 0).toLocaleString('en-IN')}
          </div>
        </div>
        <div className="p-3.5 bg-surface-container-lowest border border-outline-variant/40 rounded-xl">
          <div className="text-[10px] uppercase font-mono text-amber-700">PENDING RUNS</div>
          <div className="text-xl font-bold text-amber-700 mt-1">
            ₹{invoiceList.filter(i => i.status === 'Pending').reduce((acc, i) => acc + i.amount, 0).toLocaleString('en-IN')}
          </div>
        </div>
      </div>

      <div className="bg-surface-container-lowest border border-outline-variant/40 rounded-xl shadow-elevation-1 overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-surface-container-low text-on-surface-variant uppercase text-[10px] tracking-wider border-b border-outline-variant/20 font-semibold">
              <tr>
                <th className="p-3.5">Invoice #</th>
                <th className="p-3.5">Billed Customer</th>
                <th className="p-3.5">Invoice Amount</th>
                <th className="p-3.5">Issued Date</th>
                <th className="p-3.5">Due Date</th>
                <th className="p-3.5">Status</th>
                <th className="p-3.5 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-surface-container-low">
              {filtered.map((inv) => (
                <tr
                  key={inv.id}
                  onClick={() => navigate(`/admin/invoices/${inv.id}`)}
                  className="hover:bg-surface-container-low/60 cursor-pointer transition-colors"
                >
                  <td className="p-3.5 font-mono font-bold text-primary">{inv.id}</td>
                  <td className="p-3.5 font-bold text-on-surface">{inv.customer}</td>
                  <td className="p-3.5 font-mono font-bold text-on-surface">
                    ₹{inv.amount.toLocaleString('en-IN')}
                  </td>
                  <td className="p-3.5 font-mono text-on-surface-variant">{inv.date}</td>
                  <td className="p-3.5 font-mono text-on-surface-variant">{inv.dueDate}</td>
                  <td className="p-3.5">
                    <span className={`px-2 py-0.5 rounded-full text-[10px] font-semibold ${
                      inv.status === 'Paid' ? 'bg-emerald-50 text-emerald-700' :
                      inv.status === 'Overdue' ? 'bg-rose-50 text-rose-700' : 'bg-amber-50 text-amber-700'
                    }`}>
                      {inv.status}
                    </span>
                  </td>
                  <td className="p-3.5 text-right" onClick={(e) => e.stopPropagation()}>
                    <button
                      onClick={() => navigate(`/admin/invoices/${inv.id}`)}
                      className="px-2.5 py-1 bg-surface-container hover:bg-surface-container-high rounded text-xs font-semibold text-primary"
                    >
                      Tax Invoice
                    </button>
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

