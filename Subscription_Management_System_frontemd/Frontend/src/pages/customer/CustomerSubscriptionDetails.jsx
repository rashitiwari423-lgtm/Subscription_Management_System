import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft, RefreshCw, CheckCircle2, ShieldCheck, AlertTriangle } from 'lucide-react';
import { subscriptionService } from '../../services/subscriptionService';

export default function CustomerSubscriptionDetails() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [sub, setSub] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    async function loadSub() {
      try {
        setLoading(true);
        setError('');
        const data = await subscriptionService.getById(id);
        setSub(data);
      } catch (err) {
        setError(err?.response?.data?.message || err?.message || 'Failed to load subscription details');
      } finally {
        setLoading(false);
      }
    }
    if (id) loadSub();
  }, [id]);

  if (loading) {
    return (
      <div className="flex flex-col items-center justify-center p-12 text-center">
        <RefreshCw className="w-8 h-8 animate-spin text-primary mb-2" />
        <p className="text-xs text-on-surface-variant font-medium">Loading subscription agreement...</p>
      </div>
    );
  }

  if (error && !sub) {
    return (
      <div className="p-8 text-center space-y-4">
        <div className="inline-flex p-3 rounded-full bg-error-container text-error">
          <AlertTriangle className="w-6 h-6" />
        </div>
        <p className="text-sm font-medium text-error">{error}</p>
        <button
          onClick={() => navigate('/customer/subscriptions')}
          className="px-4 py-2 bg-surface-container text-xs font-semibold rounded-lg text-on-surface"
        >
          Back to Subscriptions
        </button>
      </div>
    );
  }

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
          <p className="text-xs text-on-surface-variant font-mono">
            {sub.subscriptionNumber || `SUB-${sub.id}`} • {sub.planName}
          </p>
        </div>
      </div>

      <div className="bg-surface-container-lowest p-6 rounded-2xl border border-outline-variant/40 shadow-elevation-1 space-y-4">
        <div className="flex items-center justify-between border-b border-surface-container pb-4">
          <div>
            <span className="text-[10px] uppercase font-mono text-outline">COMMITTED RECURRING CHARGE</span>
            <div className="text-2xl font-extrabold text-primary font-mono mt-0.5">
              ₹{Number(sub.totalAmount || 0).toLocaleString('en-IN')}/{sub.billingCycle?.toLowerCase() || 'cycle'}
            </div>
          </div>
          <span className="px-2.5 py-1 rounded-full text-xs font-semibold bg-emerald-50 text-emerald-700 border border-emerald-200">
            {sub.status}
          </span>
        </div>

        <div className="grid grid-cols-2 gap-4 text-xs">
          <div>
            <span className="text-outline uppercase text-[10px]">Contracted Plan Tier</span>
            <div className="font-bold text-on-surface mt-0.5">{sub.planName}</div>
          </div>
          <div>
            <span className="text-outline uppercase text-[10px]">Committed Seats / Units</span>
            <div className="font-bold text-on-surface font-mono mt-0.5">{sub.quantity || 1} Units</div>
          </div>
          <div>
            <span className="text-outline uppercase text-[10px]">Next Recurring Renewal</span>
            <div className="font-mono text-on-surface-variant mt-0.5">{sub.nextBillingDate || 'N/A'}</div>
          </div>
          <div>
            <span className="text-outline uppercase text-[10px]">Contract Start Date</span>
            <div className="font-mono text-on-surface-variant mt-0.5">{sub.startDate || 'N/A'}</div>
          </div>
        </div>
      </div>
    </div>
  );
}
