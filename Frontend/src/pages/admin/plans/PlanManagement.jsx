import React, { useState } from 'react';
import { Layers, Plus, CheckCircle2, ArrowRight } from 'lucide-react';
import { plans as initialPlans } from '../../../data/mockData';
import { useNavigate } from 'react-router-dom';

export default function PlanManagement() {
  const [planList, setPlanList] = useState(initialPlans || []);
  const navigate = useNavigate();

  return (
    <div className="space-y-5">
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-xl font-bold text-on-surface">Plan Management & Billing Rules</h1>
          <p className="text-xs text-on-surface-variant">
            Define pricing tiers, trial grace periods, feature matrices, and proration schedules.
          </p>
        </div>
        <button
          onClick={() => navigate('/admin/plans/new')}
          className="px-3.5 py-2 rounded-lg bg-primary hover:bg-primary-container text-white text-xs font-semibold flex items-center gap-1.5 shadow-sm"
        >
          <Plus className="w-3.5 h-3.5" />
          <span>Create New Plan</span>
        </button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
        {planList.map((plan) => (
          <div key={plan.id} className="bg-surface-container-lowest p-6 rounded-2xl border border-outline-variant/40 shadow-elevation-1 flex flex-col justify-between space-y-4">
            <div>
              <div className="flex items-center justify-between">
                <span className="text-[10px] uppercase font-mono font-bold text-primary px-2 py-0.5 rounded-full bg-primary/10">
                  {plan.tier}
                </span>
                <span className="text-[10px] text-emerald-700 font-semibold">{plan.status}</span>
              </div>

              <h2 className="text-lg font-bold text-on-surface mt-2">{plan.name}</h2>
              <div className="mt-2 flex items-baseline gap-1">
                <span className="text-2xl font-extrabold text-on-surface font-tabular">
                  ₹{plan.price.toLocaleString('en-IN')}
                </span>
                <span className="text-xs text-on-surface-variant font-medium">/{plan.billingCycle.toLowerCase()}</span>
              </div>

              <div className="mt-4 pt-4 border-t border-surface-container space-y-2">
                <div className="text-[11px] font-semibold text-on-surface uppercase tracking-wider">Plan Entitlements:</div>
                {plan.features.map((feat, i) => (
                  <div key={i} className="flex items-center gap-2 text-xs text-on-surface-variant">
                    <CheckCircle2 className="w-3.5 h-3.5 text-primary flex-shrink-0" />
                    <span>{feat}</span>
                  </div>
                ))}
              </div>
            </div>

            <div className="pt-4 border-t border-surface-container">
              <div className="flex items-center justify-between text-xs text-on-surface-variant mb-3">
                <span>Active Subscribers:</span>
                <span className="font-bold font-mono text-on-surface">{plan.subscribers} Orgs</span>
              </div>
              <button
                onClick={() => navigate(`/admin/plans/${plan.id}`)}
                className="w-full py-2 bg-surface-container hover:bg-surface-container-high text-on-surface text-xs font-semibold rounded-lg transition-colors flex items-center justify-center gap-1.5"
              >
                <span>Edit Billing Rules</span>
                <ArrowRight className="w-3.5 h-3.5" />
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

