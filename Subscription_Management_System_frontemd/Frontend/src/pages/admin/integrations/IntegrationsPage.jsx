import React, { useState } from 'react';
import { Webhook, CheckCircle2, XCircle, ArrowUpRight, Zap, RefreshCw } from 'lucide-react';

const INTEGRATIONS = [
  { id: 'razorpay', name: 'Razorpay AutoPay & Route', category: 'Payment Rail', status: 'Connected', desc: 'Direct Indian UPI, NetBanking, and recurring mandate collection' },
  { id: 'stripe', name: 'Stripe Billing & Elements', category: 'Payment Rail', status: 'Connected', desc: 'Multi-currency international card settlement' },
  { id: 'zoho', name: 'Zoho Books GST Sync', category: 'Accounting ERP', status: 'Connected', desc: 'Real-time e-invoicing and GSTR-1 automated filing sync' },
  { id: 'slack', name: 'Slack Intelligence Bot', category: 'Alerts', status: 'Connected', desc: 'Real-time notifications for large quotes, renewals, and churn signals' },
  { id: 'quickbooks', name: 'QuickBooks Online', category: 'Accounting ERP', status: 'Available', desc: 'Synchronize chart of accounts, credit notes, and ledgers' },
  { id: 'hubspot', name: 'HubSpot CRM Deal Sync', category: 'Sales CPQ', status: 'Available', desc: 'Auto-sync accepted CPQ quotations into deal pipeline' }
];

export default function IntegrationsPage() {
  const [connectors, setConnectors] = useState(INTEGRATIONS);

  const toggleStatus = (id) => {
    setConnectors(prev => prev.map(c => {
      if (c.id === id) {
        return { ...c, status: c.status === 'Connected' ? 'Available' : 'Connected' };
      }
      return c;
    }));
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-xl font-bold text-on-surface">Gateways & Financial Connectors</h1>
        <p className="text-xs text-on-surface-variant">
          Connect payment aggregators, accounting ERPs, and automated sales CRM pipelines.
        </p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {connectors.map((c) => (
          <div key={c.id} className="bg-surface-container-lowest p-5 rounded-2xl border border-outline-variant/40 shadow-elevation-1 space-y-3 flex flex-col justify-between">
            <div>
              <div className="flex items-center justify-between">
                <span className="text-[10px] font-mono uppercase text-outline">{c.category}</span>
                <span className={`px-2 py-0.5 rounded-full text-[10px] font-semibold ${
                  c.status === 'Connected' ? 'bg-emerald-50 text-emerald-700' : 'bg-surface-container text-on-surface-variant'
                }`}>
                  {c.status}
                </span>
              </div>
              <h3 className="font-bold text-sm text-on-surface mt-2">{c.name}</h3>
              <p className="text-xs text-on-surface-variant mt-1 leading-relaxed">{c.desc}</p>
            </div>

            <div className="pt-3 border-t border-surface-container flex items-center justify-between">
              <span className="text-[10px] font-mono text-outline">API v1.4 LIVE</span>
              <button
                onClick={() => toggleStatus(c.id)}
                className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-all ${
                  c.status === 'Connected'
                    ? 'border border-outline-variant/40 text-on-surface hover:bg-surface-container'
                    : 'bg-primary text-white hover:bg-primary-container shadow-xs'
                }`}
              >
                {c.status === 'Connected' ? 'Configure Gateway' : 'Connect Rail'}
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

