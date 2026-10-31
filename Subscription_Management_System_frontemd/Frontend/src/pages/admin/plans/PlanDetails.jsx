import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft, Layers, CheckCircle2, RefreshCw, AlertTriangle, Edit3 } from 'lucide-react';
import { planService } from '../../../services/planService';

export default function PlanDetails() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [plan, setPlan] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    setLoading(true);
    planService.getById(id)
      .then(setPlan)
      .catch((err) => setError(err.userMessage || 'Plan not found'))
      .finally(() => setLoading(false));
  }, [id]);

  if (loading) {
    return (
      <div className="flex items-center justify-center p-12 text-xs text-on-surface-variant gap-2">
        <RefreshCw className="w-4 h-4 animate-spin text-primary" />
        <span>Loading plan details...</span>
      </div>
    );
  }

  if (error || !plan) {
    return (
      <div className="p-8 text-center text-xs text-error space-y-3">
        <AlertTriangle className="w-8 h-8 mx-auto" />
        <p>{error || 'Plan not found'}</p>
        <button
          onClick={() => navigate('/admin/plans')}
          className="px-3 py-1.5 bg-primary text-white rounded text-xs font-semibold"
        >
          Back to Plans
        </button>
      </div>
    );
  }

  const formatCurr = (val, curr = 'INR') => `₹${Number(val || 0).toLocaleString('en-IN', { maximumFractionDigits: 2 })}`;

  return (
    <div className="max-w-3xl mx-auto space-y-6">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-3">
          <button
            onClick={() => navigate('/admin/plans')}
            className="p-1.5 rounded-lg border border-outline-variant/40 hover:bg-surface-container"
          >
            <ArrowLeft className="w-4 h-4" />
          </button>
          <div>
            <div className="flex items-center gap-2">
              <h1 className="text-xl font-bold text-on-surface">{plan.name}</h1>
              <span className={`px-2 py-0.5 rounded-full text-[10px] font-semibold ${
                plan.status === 'ACTIVE' ? 'bg-emerald-50 text-emerald-700' : 'bg-surface-container text-on-surface-variant'
              }`}>
                {plan.status}
              </span>
            </div>
            <p className="text-xs text-on-surface-variant font-mono">Plan ID: #{plan.id} • Variant: #{plan.productVariantId}</p>
          </div>
        </div>

        <button
          onClick={() => navigate(`/admin/plans/${plan.id}/edit`)}
          className="px-3.5 py-1.5 bg-primary hover:bg-primary-container text-white text-xs font-semibold rounded-lg flex items-center gap-1.5 shadow-sm"
        >
          <Edit3 className="w-3.5 h-3.5" />
          <span>Edit Plan</span>
        </button>
      </div>

      <div className="bg-surface-container-lowest p-6 rounded-2xl border border-outline-variant/40 shadow-elevation-1 space-y-4">
        <div className="grid grid-cols-2 sm:grid-cols-3 gap-4 text-xs">
          <div>
            <span className="text-outline uppercase text-[10px]">Price &amp; Cadence</span>
            <div className="font-bold text-on-surface font-mono text-xl mt-0.5">
              {formatCurr(plan.price, plan.currency)} / {plan.billingCycle?.toLowerCase()}
            </div>
          </div>
          <div>
            <span className="text-outline uppercase text-[10px]">Trial Grace Period</span>
            <div className="font-bold text-primary font-mono text-xl mt-0.5">
              {plan.trialDays || 0} Days
            </div>
          </div>
          <div>
            <span className="text-outline uppercase text-[10px]">Currency</span>
            <div className="font-bold text-on-surface text-xl mt-0.5 font-mono">
              {plan.currency || 'INR'}
            </div>
          </div>
        </div>

        <div className="pt-4 border-t border-surface-container">
          <span className="text-outline uppercase text-[10px] font-bold">Plan Description</span>
          <p className="text-xs text-on-surface-variant mt-1">
            {plan.description || 'No description provided for this plan.'}
          </p>
        </div>
      </div>
    </div>
  );
}
