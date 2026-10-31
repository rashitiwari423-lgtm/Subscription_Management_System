import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft, Building, Mail, Phone, RefreshCw, Receipt, CreditCard, Sparkles, CheckCircle2 } from 'lucide-react';
import { customerService } from '../../../services/customerService';
import { subscriptions, invoices, payments } from '../../../data/mockData';

export default function CustomerDetails() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [customer, setCustomer] = useState(null);
  const [activeTab, setActiveTab] = useState('subscriptions');

  useEffect(() => {
    customerService.getById(id).then(c => {
      setCustomer(c || {
        id,
        name: 'Nexus Dynamics',
        email: 'billing@nexusdynamics.com',
        plan: 'Enterprise',
        status: 'Active',
        mrr: 45000,
        since: '2023-01-15',
        seats: 120,
        industry: 'Technology'
      });
    });
  }, [id]);

  if (!customer) return <div className="p-8 text-xs text-on-surface-variant">Loading customer profile...</div>;

  return (
    <div className="space-y-6">
      {/* Header Profile Bar */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-3">
          <button
            onClick={() => navigate('/admin/customers')}
            className="p-1.5 rounded-lg border border-outline-variant/40 hover:bg-surface-container text-on-surface-variant"
          >
            <ArrowLeft className="w-4 h-4" />
          </button>
          <div>
            <div className="flex items-center gap-2">
              <h1 className="text-xl font-bold text-on-surface">{customer.name}</h1>
              <span className="px-2 py-0.5 rounded-full text-[10px] font-semibold bg-emerald-50 text-emerald-700">
                {customer.status}
              </span>
            </div>
            <p className="text-xs text-on-surface-variant font-mono">{customer.email} • ID: {customer.id}</p>
          </div>
        </div>

        <div className="flex items-center gap-2">
          <button
            onClick={() => navigate('/admin/quotations/new')}
            className="px-3 py-1.5 rounded-lg bg-surface-container border border-outline-variant/40 text-xs font-semibold text-on-surface hover:bg-surface-container-high"
          >
            Generate Quote
          </button>
          <button
            onClick={() => navigate(`/admin/customers/${customer.id}/edit`)}
            className="px-3.5 py-1.5 rounded-lg bg-primary hover:bg-primary-container text-white text-xs font-semibold shadow-sm"
          >
            Edit Profile
          </button>
        </div>
      </div>

      {/* 360 Financial Metrics */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
        <div className="p-4 bg-surface-container-lowest border border-outline-variant/40 rounded-xl">
          <div className="text-[10px] font-mono uppercase text-outline">CURRENT MRR</div>
          <div className="text-xl font-bold text-on-surface mt-1">₹{(customer.mrr || 0).toLocaleString('en-IN')}</div>
          <div className="text-[10px] text-emerald-600 mt-0.5">Recurring billing base</div>
        </div>
        <div className="p-4 bg-surface-container-lowest border border-outline-variant/40 rounded-xl">
          <div className="text-[10px] font-mono uppercase text-outline">ANNUALIZED ARR</div>
          <div className="text-xl font-bold text-primary mt-1">₹{((customer.mrr || 0) * 12).toLocaleString('en-IN')}</div>
          <div className="text-[10px] text-on-surface-variant mt-0.5">Contract value run-rate</div>
        </div>
        <div className="p-4 bg-surface-container-lowest border border-outline-variant/40 rounded-xl">
          <div className="text-[10px] font-mono uppercase text-outline">PROVISIONED SEATS</div>
          <div className="text-xl font-bold text-on-surface mt-1">{customer.seats || 45}</div>
          <div className="text-[10px] text-on-surface-variant mt-0.5">Active licensed users</div>
        </div>
        <div className="p-4 bg-surface-container-lowest border border-outline-variant/40 rounded-xl">
          <div className="text-[10px] font-mono uppercase text-outline">HEALTH SCORE</div>
          <div className="text-xl font-bold text-emerald-700 mt-1">94 / 100</div>
          <div className="text-[10px] text-emerald-600 mt-0.5">Low churn propensity</div>
        </div>
      </div>

      {/* Subscriptions / Invoices Tabs */}
      <div className="bg-surface-container-lowest border border-outline-variant/40 rounded-xl shadow-elevation-1 overflow-hidden">
        <div className="flex border-b border-surface-container px-4">
          {[
            { id: 'subscriptions', label: 'Subscriptions', icon: RefreshCw },
            { id: 'invoices', label: 'Invoices & Receipts', icon: Receipt },
            { id: 'ledger', label: 'Payment Ledger', icon: CreditCard }
          ].map(t => {
            const Icon = t.icon;
            return (
              <button
                key={t.id}
                onClick={() => setActiveTab(t.id)}
                className={`flex items-center gap-2 py-3 px-4 text-xs font-semibold border-b-2 transition-all ${
                  activeTab === t.id
                    ? 'border-primary text-primary'
                    : 'border-transparent text-on-surface-variant hover:text-on-surface'
                }`}
              >
                <Icon className="w-3.5 h-3.5" />
                <span>{t.label}</span>
              </button>
            );
          })}
        </div>

        <div className="p-4">
          {activeTab === 'subscriptions' && (
            <div className="space-y-3">
              <div className="p-3.5 rounded-lg border border-outline-variant/30 bg-surface-container-low flex items-center justify-between">
                <div>
                  <div className="text-xs font-bold text-on-surface">{customer.plan} Sentinel Contract</div>
                  <div className="text-[11px] text-on-surface-variant">Annual committed tier • Renews Jan 2025</div>
                </div>
                <div className="text-right">
                  <div className="text-xs font-mono font-bold text-primary">₹{(customer.mrr || 45000).toLocaleString('en-IN')}/mo</div>
                  <span className="text-[10px] text-emerald-700 font-semibold">Active & Reconciled</span>
                </div>
              </div>
            </div>
          )}

          {activeTab === 'invoices' && (
            <div className="space-y-2">
              {invoices.filter(i => i.customer === customer.name).map(inv => (
                <div key={inv.id} className="p-3 rounded-lg border border-outline-variant/30 flex items-center justify-between text-xs">
                  <div>
                    <span className="font-mono font-bold text-on-surface">{inv.id}</span>
                    <span className="text-on-surface-variant ml-3">Due: {inv.dueDate}</span>
                  </div>
                  <div className="flex items-center gap-3">
                    <span className="font-mono font-bold">₹{inv.amount.toLocaleString('en-IN')}</span>
                    <span className={`px-2 py-0.5 rounded-full text-[10px] font-semibold ${
                      inv.status === 'Paid' ? 'bg-emerald-50 text-emerald-700' : 'bg-rose-50 text-rose-700'
                    }`}>
                      {inv.status}
                    </span>
                  </div>
                </div>
              ))}
            </div>
          )}

          {activeTab === 'ledger' && (
            <div className="space-y-2">
              {payments.filter(p => p.customer === customer.name).map(pay => (
                <div key={pay.id} className="p-3 rounded-lg border border-outline-variant/30 flex items-center justify-between text-xs">
                  <div>
                    <span className="font-mono font-bold text-on-surface">{pay.id}</span>
                    <span className="text-on-surface-variant ml-3">{pay.method} ({pay.gateway})</span>
                  </div>
                  <div className="flex items-center gap-3">
                    <span className="font-mono font-bold text-emerald-700">₹{pay.amount.toLocaleString('en-IN')}</span>
                    <span className="text-emerald-700 font-semibold">{pay.status}</span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

