import React, { useState } from 'react';
import { CreditCard, Search, Filter, ShieldCheck, CheckCircle2, XCircle, ArrowUpRight } from 'lucide-react';
import { payments as initialPayments } from '../../../data/mockData';
import { useNavigate } from 'react-router-dom';

export default function PaymentsLedger() {
  const [paymentList, setPaymentList] = useState(initialPayments || []);
  const [filterMethod, setFilterMethod] = useState('ALL');
  const navigate = useNavigate();

  const filtered = paymentList.filter(p => filterMethod === 'ALL' || p.method === filterMethod);

  return (
    <div className="space-y-5">
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-xl font-bold text-on-surface">Payments & Settlement Ledger</h1>
          <p className="text-xs text-on-surface-variant">
            Multi-rail payment settlement telemetry across UPI, Razorpay, Stripe, and RTGS.
          </p>
        </div>
        <button
          onClick={() => navigate('/admin/payments/new')}
          className="px-3.5 py-2 rounded-lg bg-primary hover:bg-primary-container text-white text-xs font-semibold flex items-center gap-1.5 shadow-sm"
        >
          <span>Record Bank Settlement</span>
        </button>
      </div>

      <div className="bg-surface-container-lowest border border-outline-variant/40 rounded-xl shadow-elevation-1 overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-surface-container-low text-on-surface-variant uppercase text-[10px] tracking-wider border-b border-outline-variant/20 font-semibold">
              <tr>
                <th className="p-3.5">Transaction ID</th>
                <th className="p-3.5">Invoice Ref</th>
                <th className="p-3.5">Customer Name</th>
                <th className="p-3.5">Amount Settled</th>
                <th className="p-3.5">Rail Method</th>
                <th className="p-3.5">Gateway Provider</th>
                <th className="p-3.5">Settlement Date</th>
                <th className="p-3.5">Status</th>
                <th className="p-3.5 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-surface-container-low">
              {filtered.map((pay) => (
                <tr
                  key={pay.id}
                  onClick={() => navigate(`/admin/payments/${pay.id}`)}
                  className="hover:bg-surface-container-low/60 cursor-pointer transition-colors"
                >
                  <td className="p-3.5 font-mono font-bold text-primary">{pay.id}</td>
                  <td className="p-3.5 font-mono text-on-surface-variant">{pay.invoiceId}</td>
                  <td className="p-3.5 font-bold text-on-surface">{pay.customer}</td>
                  <td className="p-3.5 font-mono font-bold text-on-surface">
                    ₹{pay.amount.toLocaleString('en-IN')}
                  </td>
                  <td className="p-3.5">{pay.method}</td>
                  <td className="p-3.5 font-mono text-[11px] text-on-surface-variant">{pay.gateway}</td>
                  <td className="p-3.5 font-mono text-on-surface-variant">{pay.date}</td>
                  <td className="p-3.5">
                    <span className={`px-2 py-0.5 rounded-full text-[10px] font-semibold ${
                      pay.status === 'Success' ? 'bg-emerald-50 text-emerald-700' : 'bg-rose-50 text-rose-700'
                    }`}>
                      {pay.status}
                    </span>
                  </td>
                  <td className="p-3.5 text-right" onClick={(e) => e.stopPropagation()}>
                    <button
                      onClick={() => navigate(`/admin/payments/${pay.id}`)}
                      className="px-2.5 py-1 bg-surface-container hover:bg-surface-container-high rounded text-xs font-semibold text-primary"
                    >
                      Audit
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

