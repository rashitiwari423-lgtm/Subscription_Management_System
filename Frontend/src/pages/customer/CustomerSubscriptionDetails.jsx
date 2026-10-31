import React from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft, RefreshCw, CheckCircle2, ShieldCheck } from 'lucide-react';
import { subscriptions } from '../../data/mockData';

export default function CustomerSubscriptionDetails() {
  const { id } = useParams();
  const navigate = useNavigate();
  const sub = subscriptions[0];

  return (
    <div className="max-w-3xl mx-auto space-y-6">
      <div className="flex items-center gap-3">
        <button
          onClick={() => navigate('/customer/subscriptions')}
          className="p-1.5 rounded-lg border border-outline-variant/40 hover:bg-surface-container"
        >
          <ArrowLeft className="w-4 h-4" />
        </button>
        <div>
          <h1 className="text-xl font-bold text-on-surface">Subscription Agreement Details</h1>
          <p className="text-xs text-on-surface-variant font-mono">{sub.id} • {sub.plan}</p>
        </div>
      </div>

      <div className="bg-surface-container-lowest p-6 rounded-2xl border border-outline-variant/40 shadow-elevation-1 space-y-4">
        <div className="flex items-center justify-between border-b border-surface-container pb-4">
          <div>
            <span className="text-[10px] uppercase font-mono text-outline">COMMITTED RECURRING CHARGE</span>
            <div className="text-2xl font-extrabold text-primary font-mono mt-0.5">₹{sub.mrr.toLocaleString('en-IN')}/mo</div>
          </div>
          <span className="px-2.5 py-1 rounded-full text-xs font-semibold bg-emerald-50 text-emerald-700">
            {sub.status} &amp; Certified
          </span>
        </div>

        <div className="grid grid-cols-2 gap-4 text-xs">
          <div>
            <span className="text-outline uppercase text-[10px]">Contracted Plan Tier</span>
            <div className="font-bold text-on-surface mt-0.5">{sub.plan}</div>
          </div>
          <div>
            <span className="text-outline uppercase text-[10px]">Active Seat Allocations</span>
            <div className="font-bold text-on-surface font-mono mt-0.5">{sub.seats} Licenses</div>
          </div>
          <div>
            <span className="text-outline uppercase text-[10px]">Renewal Schedule</span>
            <div className="font-mono text-on-surface-variant mt-0.5">{sub.renewalDate}</div>
          </div>
          <div>
            <span className="text-outline uppercase text-[10px]">Statutory Compliance</span>
            <div className="text-emerald-700 font-semibold mt-0.5">IndAS 115 / GST Recognized</div>
          </div>
        </div>
      </div>
    </div>
  );
}
