import React, { useState } from 'react';
import { RefreshCw, Receipt, CreditCard, ShieldCheck, Download, ExternalLink, ArrowUpRight } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { subscriptions, invoices, payments } from '../../data/mockData';

export default function CustomerPortal() {
  const { user } = useAuth();
  const [activeTab, setActiveTab] = useState('overview');

  const mySub = subscriptions[0]; // Nexus Dynamics
  const myInvoices = invoices.filter(i => i.customer === 'Nexus Dynamics');
  const myPayments = payments.filter(p => p.customer === 'Nexus Dynamics');

  return (
    <div className="space-y-6">
      {/* Welcome Banner */}
      <div className="bg-gradient-to-r from-primary to-primary-container p-6 rounded-2xl text-white shadow-elevation-2 flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
        <div>
          <span className="text-[11px] font-mono uppercase bg-white/20 px-2 py-0.5 rounded-full text-white">
            ENTERPRISE BILLING TENANT
          </span>
          <h1 className="text-xl font-bold mt-2">Welcome back, {user?.name || 'Rahul Gupta'}</h1>
          <p className="text-xs text-white/80 mt-1">
            Manage your organization's subscription tier, add team seat licenses, and access GST-compliant invoices.
          </p>
        </div>
        <div className="text-right">
          <div className="text-[10px] text-white/70 font-mono">NEXT BILLING RUN</div>
          <div className="text-lg font-bold font-mono text-white">15 Jan 2025</div>
          <div className="text-[11px] text-white/90">Amount: ₹45,000 / mo</div>
        </div>
      </div>

      {/* Tabs */}
      <div className="flex border-b border-surface-container gap-4">
        {[
          { id: 'overview', label: 'Subscription Overview' },
          { id: 'invoices', label: 'Tax Invoices' },
          { id: 'payment-methods', label: 'Payment Rail & Bank' }
        ].map(t => (
          <button
            key={t.id}
            onClick={() => setActiveTab(t.id)}
            className={`pb-3 text-xs font-semibold border-b-2 transition-all ${
              activeTab === t.id
                ? 'border-primary text-primary'
                : 'border-transparent text-on-surface-variant hover:text-on-surface'
            }`}
          >
            {t.label}
          </button>
        ))}
      </div>

      {activeTab === 'overview' && (
        <div className="space-y-6">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div className="p-5 bg-surface-container-lowest border border-outline-variant/40 rounded-xl space-y-2">
              <span className="text-[10px] uppercase font-mono text-outline">CURRENT PLAN</span>
              <div className="text-xl font-bold text-on-surface">{mySub?.plan || 'Enterprise Pro'}</div>
              <p className="text-xs text-emerald-700 font-semibold">Active & Auto-Renewing</p>
            </div>
            <div className="p-5 bg-surface-container-lowest border border-outline-variant/40 rounded-xl space-y-2">
              <span className="text-[10px] uppercase font-mono text-outline">PROVISIONED SEATS</span>
              <div className="text-xl font-bold text-on-surface">{mySub?.seats || 120} Seats</div>
              <p className="text-xs text-on-surface-variant">{mySub?.usagePercent || 78}% Actively utilized</p>
            </div>
            <div className="p-5 bg-surface-container-lowest border border-outline-variant/40 rounded-xl space-y-2">
              <span className="text-[10px] uppercase font-mono text-outline">MONTHLY RECURRING</span>
              <div className="text-xl font-bold text-primary">₹{(mySub?.mrr || 45000).toLocaleString('en-IN')}</div>
              <p className="text-xs text-on-surface-variant">Includes 18% IGST Tax</p>
            </div>
          </div>

          <div className="p-5 bg-surface-container-lowest border border-outline-variant/40 rounded-xl space-y-3">
            <div className="flex items-center justify-between">
              <h2 className="text-sm font-bold text-on-surface">Seat License Utilization</h2>
              <span className="text-xs text-primary font-semibold cursor-pointer hover:underline">Add More Seats +</span>
            </div>
            <div className="w-full bg-surface-container h-2 rounded-full overflow-hidden">
              <div className="bg-primary h-full rounded-full" style={{ width: `${mySub?.usagePercent || 78}%` }}></div>
            </div>
            <div className="flex justify-between text-xs text-on-surface-variant font-mono">
              <span>94 of 120 seats assigned</span>
              <span>26 seats available</span>
            </div>
          </div>
        </div>
      )}

      {activeTab === 'invoices' && (
        <div className="bg-surface-container-lowest border border-outline-variant/40 rounded-xl shadow-elevation-1 overflow-hidden">
          <table className="w-full text-left text-xs">
            <thead className="bg-surface-container-low text-on-surface-variant font-semibold">
              <tr>
                <th className="p-3">Invoice Number</th>
                <th className="p-3">Billing Period</th>
                <th className="p-3">Total Amount</th>
                <th className="p-3">Status</th>
                <th className="p-3 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-surface-container">
              {myInvoices.map(inv => (
                <tr key={inv.id} className="hover:bg-surface-container-low/50">
                  <td className="p-3 font-mono font-bold text-primary">{inv.id}</td>
                  <td className="p-3 font-mono">{inv.date}</td>
                  <td className="p-3 font-mono font-bold">₹{inv.amount.toLocaleString('en-IN')}</td>
                  <td className="p-3">
                    <span className="px-2 py-0.5 rounded-full text-[10px] font-semibold bg-emerald-50 text-emerald-700">
                      {inv.status}
                    </span>
                  </td>
                  <td className="p-3 text-right">
                    <button
                      onClick={() => alert(`Downloaded invoice ${inv.id}`)}
                      className="text-primary hover:underline font-semibold"
                    >
                      Download PDF
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {activeTab === 'payment-methods' && (
        <div className="bg-surface-container-lowest p-6 rounded-2xl border border-outline-variant/40 shadow-elevation-1 space-y-4 max-w-xl">
          <h2 className="text-sm font-bold text-on-surface">Default Corporate Payment Rail</h2>
          <div className="p-4 rounded-xl border border-primary/30 bg-primary/5 flex items-center justify-between">
            <div className="flex items-center gap-3">
              <CreditCard className="w-6 h-6 text-primary" />
              <div>
                <div className="text-xs font-bold text-on-surface">HDFC Corporate Banking e-NACH</div>
                <div className="text-[11px] text-on-surface-variant font-mono">Mandate ID: RAZOR_AUTOPAY_8812</div>
              </div>
            </div>
            <span className="text-[10px] font-semibold text-emerald-700 bg-emerald-50 px-2 py-0.5 rounded-full">
              AUTO-DEBIT ACTIVE
            </span>
          </div>
        </div>
      )}
    </div>
  );
}

