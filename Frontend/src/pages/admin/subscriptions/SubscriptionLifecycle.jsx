import React, { useState } from 'react';
import { RefreshCw, Plus, Search, Filter, ShieldCheck, AlertCircle, ArrowUpRight } from 'lucide-react';
import { subscriptions as initialSubscriptions } from '../../../data/mockData';
import { useNavigate } from 'react-router-dom';

export default function SubscriptionLifecycle() {
  const [subList, setSubList] = useState(initialSubscriptions || []);
  const [filterStatus, setFilterStatus] = useState('ALL');
  const navigate = useNavigate();

  const filtered = subList.filter(s => filterStatus === 'ALL' || s.status === filterStatus);

  return (
    <div className="space-y-5">
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-xl font-bold text-on-surface">Subscription Lifecycle Orchestration</h1>
          <p className="text-xs text-on-surface-variant">
            Monitor real-time renewals, trial conversions, contract proration, and seat saturation.
          </p>
        </div>
        <button
          onClick={() => navigate('/admin/subscriptions/new')}
          className="px-3.5 py-2 rounded-lg bg-primary hover:bg-primary-container text-white text-xs font-semibold flex items-center gap-1.5 shadow-sm"
        >
          <Plus className="w-3.5 h-3.5" />
          <span>Provision Subscription</span>
        </button>
      </div>

      <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
        <div className="p-3.5 bg-surface-container-lowest border border-outline-variant/40 rounded-xl">
          <div className="text-[10px] uppercase font-mono text-outline">ACTIVE PORTFOLIOS</div>
          <div className="text-xl font-bold text-on-surface mt-1">{subList.length} Contracts</div>
        </div>
        <div className="p-3.5 bg-surface-container-lowest border border-outline-variant/40 rounded-xl">
          <div className="text-[10px] uppercase font-mono text-emerald-700">TOTAL MONTHLY MRR</div>
          <div className="text-xl font-bold text-emerald-700 mt-1 font-tabular">
            ₹{subList.reduce((acc, s) => acc + (s.mrr || 0), 0).toLocaleString('en-IN')}
          </div>
        </div>
        <div className="p-3.5 bg-surface-container-lowest border border-outline-variant/40 rounded-xl">
          <div className="text-[10px] uppercase font-mono text-primary">AGGREGATE ARR</div>
          <div className="text-xl font-bold text-primary mt-1 font-tabular">
            ₹{subList.reduce((acc, s) => acc + (s.arr || 0), 0).toLocaleString('en-IN')}
          </div>
        </div>
        <div className="p-3.5 bg-surface-container-lowest border border-outline-variant/40 rounded-xl">
          <div className="text-[10px] uppercase font-mono text-amber-700">AT RISK CHURN</div>
          <div className="text-xl font-bold text-amber-700 mt-1">
            {subList.filter(s => s.status === 'At Risk').length} Accounts
          </div>
        </div>
      </div>

      <div className="bg-surface-container-lowest border border-outline-variant/40 rounded-xl shadow-elevation-1 overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-surface-container-low text-on-surface-variant uppercase text-[10px] tracking-wider border-b border-outline-variant/20 font-semibold">
              <tr>
                <th className="p-3.5">Subscription ID</th>
                <th className="p-3.5">Customer Account</th>
                <th className="p-3.5">Plan Level</th>
                <th className="p-3.5">MRR</th>
                <th className="p-3.5">ARR Contract</th>
                <th className="p-3.5">Next Renewal</th>
                <th className="p-3.5">Seat Saturation</th>
                <th className="p-3.5">Status</th>
                <th className="p-3.5 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-surface-container-low">
              {filtered.map((sub) => (
                <tr
                  key={sub.id}
                  onClick={() => navigate(`/admin/subscriptions/${sub.id}`)}
                  className="hover:bg-surface-container-low/60 cursor-pointer transition-colors"
                >
                  <td className="p-3.5 font-mono font-bold text-primary">{sub.id}</td>
                  <td className="p-3.5 font-bold text-on-surface">{sub.customer}</td>
                  <td className="p-3.5">{sub.plan}</td>
                  <td className="p-3.5 font-mono font-semibold">₹{sub.mrr.toLocaleString('en-IN')}</td>
                  <td className="p-3.5 font-mono text-on-surface-variant">₹{sub.arr.toLocaleString('en-IN')}</td>
                  <td className="p-3.5 font-mono text-on-surface-variant">{sub.renewalDate}</td>
                  <td className="p-3.5">
                    <div className="flex items-center gap-2">
                      <div className="w-16 bg-surface-container h-1.5 rounded-full overflow-hidden">
                        <div
                          className={`h-full rounded-full ${sub.usagePercent < 50 ? 'bg-amber-500' : 'bg-emerald-500'}`}
                          style={{ width: `${sub.usagePercent}%` }}
                        ></div>
                      </div>
                      <span className="text-[11px] font-mono">{sub.usagePercent}%</span>
                    </div>
                  </td>
                  <td className="p-3.5">
                    <span className={`px-2 py-0.5 rounded-full text-[10px] font-semibold ${
                      sub.status === 'Active' ? 'bg-emerald-50 text-emerald-700' :
                      sub.status === 'At Risk' ? 'bg-amber-50 text-amber-700' : 'bg-surface-container text-on-surface-variant'
                    }`}>
                      {sub.status}
                    </span>
                  </td>
                  <td className="p-3.5 text-right" onClick={(e) => e.stopPropagation()}>
                    <button
                      onClick={() => navigate(`/admin/subscriptions/${sub.id}`)}
                      className="px-2.5 py-1 bg-surface-container hover:bg-surface-container-high rounded text-xs font-semibold text-primary"
                    >
                      Lifecycle 360
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

