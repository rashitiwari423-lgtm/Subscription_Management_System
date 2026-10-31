import React, { useState, useEffect } from 'react';
import { Layers, Plus, CheckCircle2, ArrowRight, RefreshCw, AlertTriangle } from 'lucide-react';
import { planService } from '../../../services/planService';
import { useNavigate } from 'react-router-dom';

export default function PlanManagement() {
  const [planList, setPlanList] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  const loadPlans = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await planService.getAll();
      setPlanList(data || []);
    } catch (err) {
      setError(err.userMessage || 'Failed to load subscription plans');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadPlans();
  }, []);

  const formatCurr = (val, curr = 'INR') => `₹${Number(val || 0).toLocaleString('en-IN', { maximumFractionDigits: 2 })}`;

  return (
    <div className="space-y-5">
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-xl font-bold text-on-surface">Plan Management &amp; Billing Rules</h1>
          <p className="text-xs text-on-surface-variant">
            Define pricing tiers, trial grace periods, billing cycles, and subscription terms.
          </p>
        </div>
        <div className="flex items-center gap-2">
          <button
            onClick={loadPlans}
            className="p-2 border border-outline-variant/40 rounded-lg hover:bg-surface-container text-on-surface-variant"
            title="Refresh"
          >
            <RefreshCw className="w-3.5 h-3.5" />
          </button>
          <button
            onClick={() => navigate('/admin/plans/new')}
            className="px-3.5 py-2 rounded-lg bg-primary hover:bg-primary-container text-white text-xs font-semibold flex items-center gap-1.5 shadow-sm"
          >
            <Plus className="w-3.5 h-3.5" />
            <span>Create New Plan</span>
          </button>
        </div>
      </div>

      {loading ? (
        <div className="p-12 text-center text-xs text-on-surface-variant flex items-center justify-center gap-2">
          <RefreshCw className="w-4 h-4 animate-spin text-primary" />
          <span>Loading plans...</span>
        </div>
      ) : error ? (
        <div className="p-6 text-center space-y-2 bg-error-container/20 rounded-xl border border-error/30">
          <AlertTriangle className="w-6 h-6 text-error mx-auto" />
          <p className="text-xs text-error font-medium">{error}</p>
          <button onClick={loadPlans} className="px-3 py-1 bg-primary text-white text-xs rounded font-semibold">
            Retry
          </button>
        </div>
      ) : planList.length === 0 ? (
        <div className="p-12 bg-surface-container-lowest border border-outline-variant/40 rounded-xl text-center text-xs text-on-surface-variant">
          No subscription plans configured yet. Click "Create New Plan" above to create one.
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
          {planList.map((plan) => (
            <div key={plan.id} className="bg-surface-container-lowest p-6 rounded-2xl border border-outline-variant/40 shadow-elevation-1 flex flex-col justify-between space-y-4">
              <div>
                <div className="flex items-center justify-between">
                  <span className="text-[10px] uppercase font-mono font-bold text-primary px-2 py-0.5 rounded-full bg-primary/10">
                    {plan.billingCycle}
                  </span>
                  <span className={`text-[10px] font-semibold px-2 py-0.5 rounded-full ${
                    plan.status === 'ACTIVE' ? 'bg-emerald-50 text-emerald-700' : 'bg-surface-container text-on-surface-variant'
                  }`}>
                    {plan.status}
                  </span>
                </div>

                <h2 className="text-lg font-bold text-on-surface mt-2">{plan.name}</h2>
                <div className="mt-2 flex items-baseline gap-1">
                  <span className="text-2xl font-extrabold text-on-surface font-tabular">
                    {formatCurr(plan.price, plan.currency)}
                  </span>
                  <span className="text-xs text-on-surface-variant font-medium">/{plan.billingCycle?.toLowerCase()}</span>
                </div>

                <div className="mt-4 pt-4 border-t border-surface-container space-y-2">
                  <div className="text-[11px] font-semibold text-on-surface uppercase tracking-wider">Plan Terms:</div>
                  <div className="flex items-center gap-2 text-xs text-on-surface-variant">
                    <CheckCircle2 className="w-3.5 h-3.5 text-primary flex-shrink-0" />
                    <span>Variant ID: #{plan.productVariantId}</span>
                  </div>
                  <div className="flex items-center gap-2 text-xs text-on-surface-variant">
                    <CheckCircle2 className="w-3.5 h-3.5 text-primary flex-shrink-0" />
                    <span>Trial Grace Period: {plan.trialDays || 0} days</span>
                  </div>
                  {plan.description && (
                    <div className="flex items-start gap-2 text-xs text-on-surface-variant">
                      <CheckCircle2 className="w-3.5 h-3.5 text-primary flex-shrink-0 mt-0.5" />
                      <span className="line-clamp-2">{plan.description}</span>
                    </div>
                  )}
                </div>
              </div>

              <div className="pt-4 border-t border-surface-container">
                <button
                  onClick={() => navigate(`/admin/plans/${plan.id}`)}
                  className="w-full py-2 bg-surface-container hover:bg-surface-container-high text-on-surface text-xs font-semibold rounded-lg transition-colors flex items-center justify-center gap-1.5"
                >
                  <span>View &amp; Edit Plan Details</span>
                  <ArrowRight className="w-3.5 h-3.5" />
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
